package kr.co.ongil.data.datasource.remote.interceptor

import android.util.Log
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kr.co.ongil.data.datasource.local.preferences.TokenManager
import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.model.auth.RefreshTokenRequest
import kr.co.ongil.data.model.auth.RefreshTokenResponse
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token Refresh 결과
 */
private sealed class RefreshResult {
    data class Success(val response: RefreshTokenResponse) : RefreshResult()
    data class NeedReLogin(val message: String) : RefreshResult()
    data class Failed(val message: String) : RefreshResult()
}

/**
 * 인증 토큰 자동 갱신 Interceptor
 *
 * 기능:
 * 1. 모든 요청에 자동으로 Authorization 헤더 추가
 * 2. 401 Unauthorized 발생 시 refresh token으로 토큰 갱신
 * 3. 갱신 성공 시 원래 요청 재시도
 * 4. 갱신 실패 시 토큰 삭제 (자동 로그아웃)
 *
 * Token Refresh API 상태 코드 처리:
 * - 200: 정상 재발급 → 토큰 저장 및 요청 재시도
 * - 400: 잘못된 요청 → 재시도 없이 실패
 * - 401: Refresh Token 만료/유효하지 않음 → 토큰 삭제, 재로그인 필요
 * - 403: Refresh Token 미등록 (로그아웃/탈취) → 토큰 삭제, 재로그인 필요
 * - 409: 토큰 재사용 감지 → 토큰 삭제, 재로그인 필요
 * - 429: 레이트 리밋 → 재시도 없이 실패
 * - 500: 서버 내부 오류 → 재시도 없이 실패
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val TOKEN_TYPE = "Bearer"

        // 토큰이 필요 없는 엔드포인트
        private val NO_AUTH_ENDPOINTS = setOf(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/signup"
        )
    }

    @Volatile
    private var isRefreshing = false
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // 로그인, 회원가입, 토큰 갱신 요청은 토큰 불필요
        if (NO_AUTH_ENDPOINTS.any { path.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        // 저장된 accessToken 가져오기
        val accessToken = runBlocking {
            tokenManager.getAccessToken().firstOrNull()
        }

        // accessToken이 없으면 그냥 진행
        if (accessToken.isNullOrEmpty()) {
            Log.w(TAG, "No access token available")
            return chain.proceed(originalRequest)
        }

        // Authorization 헤더 추가
        val requestWithAuth = originalRequest.newBuilder()
            .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $accessToken")
            .build()

        // 요청 실행
        val response = chain.proceed(requestWithAuth)

        // 401 에러 발생 시 토큰 갱신 시도
        if (response.code == 401) {
            response.close()

            Log.d(TAG, "401 Unauthorized - attempting token refresh")

            synchronized(lock) {
                // 다른 스레드가 이미 갱신 중이면 대기
                if (isRefreshing) {
                    // 갱신 완료 대기
                    while (isRefreshing) {
                        try {
                            (lock as Object).wait(100)
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }

                    // 갱신된 토큰으로 재시도
                    val newAccessToken = runBlocking {
                        tokenManager.getAccessToken().firstOrNull()
                    }

                    return if (!newAccessToken.isNullOrEmpty()) {
                        val newRequest = originalRequest.newBuilder()
                            .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $newAccessToken")
                            .build()
                        chain.proceed(newRequest)
                    } else {
                        response
                    }
                }

                isRefreshing = true
            }

            try {
                // refreshToken으로 토큰 갱신
                val refreshToken = runBlocking {
                    tokenManager.getRefreshToken().firstOrNull()
                }

                if (refreshToken.isNullOrEmpty()) {
                    Log.e(TAG, "No refresh token available - clearing tokens")
                    clearTokens()
                    return response
                }

                // 토큰 갱신 API 호출
                val refreshResult = runBlocking {
                    try {
                        val rawResponse = authApi.refreshToken(RefreshTokenRequest(refreshToken))
                        RefreshResult.Success(rawResponse)
                    } catch (e: retrofit2.HttpException) {
                        val code = e.code()
                        Log.e(TAG, "Token refresh HTTP error: $code - ${e.message()}")

                        when (code) {
                            401 -> {
                                // Refresh Token 만료 또는 유효하지 않음 → 재로그인 필요
                                Log.e(TAG, "Refresh token expired or invalid - re-login required")
                                RefreshResult.NeedReLogin("Refresh Token이 만료되었거나 유효하지 않습니다.")
                            }
                            403 -> {
                                // Refresh Token이 서버에 등록되어 있지 않음 → 로그아웃/탈취
                                Log.e(TAG, "Refresh token not registered - possible token theft")
                                RefreshResult.NeedReLogin("Refresh Token이 서버에 등록되어 있지 않습니다.")
                            }
                            409 -> {
                                // 토큰 재사용 감지 → 전 토큰 전면 폐기
                                Log.e(TAG, "Token reuse detected - all tokens invalidated")
                                RefreshResult.NeedReLogin("토큰 재사용이 감지되었습니다. 보안을 위해 로그아웃되었습니다.")
                            }
                            400 -> {
                                // 잘못된 요청 → 재시도 없이 실패
                                Log.e(TAG, "Bad request - invalid token format")
                                RefreshResult.Failed("잘못된 요청입니다. 토큰 형식을 확인해주세요.")
                            }
                            429 -> {
                                // 레이트 리밋 → 재시도 없이 실패
                                Log.w(TAG, "Rate limit exceeded")
                                RefreshResult.Failed("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")
                            }
                            500 -> {
                                // 서버 내부 오류 → 재시도 없이 실패
                                Log.e(TAG, "Server internal error")
                                RefreshResult.Failed("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                            }
                            else -> {
                                Log.e(TAG, "Unknown HTTP error: $code")
                                RefreshResult.Failed("토큰 갱신에 실패했습니다.")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Token refresh network error", e)
                        RefreshResult.Failed("네트워크 오류가 발생했습니다.")
                    }
                }

                when (refreshResult) {
                    is RefreshResult.Success -> {
                        // 새 토큰 저장
                        runBlocking {
                            tokenManager.saveTokens(
                                accessToken = refreshResult.response.data.accessToken,
                                refreshToken = refreshResult.response.data.refreshToken
                            )
                        }

                        Log.d(TAG, "Token refresh successful")

                        // 원래 요청 재시도 (새 토큰으로)
                        val newRequest = originalRequest.newBuilder()
                            .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE ${refreshResult.response.data.accessToken}")
                            .build()

                        return chain.proceed(newRequest)
                    }
                    is RefreshResult.NeedReLogin -> {
                        // 재로그인 필요 → 토큰 삭제
                        Log.e(TAG, "Re-login required: ${refreshResult.message}")
                        clearTokens()
                        return response
                    }
                    is RefreshResult.Failed -> {
                        // 재시도 없이 실패
                        Log.e(TAG, "Token refresh failed: ${refreshResult.message}")
                        return response
                    }
                }
            } finally {
                synchronized(lock) {
                    isRefreshing = false
                    (lock as Object).notifyAll()
                }
            }
        }

        return response
    }

    private fun clearTokens() {
        runBlocking {
            tokenManager.clearTokens()
        }
    }
}
