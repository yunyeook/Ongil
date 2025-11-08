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
 * 1) 모든 요청에 Authorization 헤더 자동 추가
 * 2) 401 시 RefreshToken으로 갱신
 * 3) 갱신 성공 시 원요청 재시도
 * 4) 갱신 실패 시 토큰 삭제(자동 로그아웃)
 *
 * 상태 코드 처리:
 * - 200: 정상 재발급 → 저장 후 재시도
 * - 400: 잘못된 요청 → 실패 반환
 * - 401: RT 만료/유효X → 토큰 삭제, 재로그인 필요
 * - 403: RT 미등록/탈취 → 토큰 삭제, 재로그인 필요
 * - 409: 재사용 감지 → 토큰 삭제, 재로그인 필요
 * - 429: 레이트 리밋 → 실패 반환
 * - 500: 서버 내부 오류 → 실패 반환
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
        private const val HEADER_RETRY = "X-Retry"

        // 토큰 불필요(완전 공개) 엔드포인트만 포함
        private val NO_AUTH_ENDPOINTS = setOf(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/register"
        )
    }

    @Volatile
    private var isRefreshing = false

    // wait/notifyAll은 java.lang.Object의 메서드 → 명시적으로 Object 사용
    private val refreshLock = java.lang.Object()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        val isRetry = originalRequest.header(HEADER_RETRY) == "true"
        val isLogout = path.startsWith("/api/v1/auth/logout")

        Log.d(TAG, "→ Intercept: path=$path, isRetry=$isRetry, isLogout=$isLogout")

        // (A) 재시도 요청은 절대 refresh 하지 않음 (루프 차단)
        if (isRetry) {
            Log.d(TAG, "Retry request detected. Skip auth & refresh.")
            return chain.proceed(originalRequest)
        }

        // (B) 공개 엔드포인트는 스킵
        if (NO_AUTH_ENDPOINTS.any { path.startsWith(it) }) {
            Log.d(TAG, "Skip auth for public endpoint: $path")
            return chain.proceed(originalRequest)
        }

        // (C) accessToken 조회
        val accessToken = runBlocking { tokenManager.getAccessToken().firstOrNull() }
        if (accessToken.isNullOrEmpty()) {
            Log.w(TAG, "No access token. Proceed without auth for $path")
            return chain.proceed(originalRequest)
        }

        // (D) Authorization 헤더 부착
        val authed = originalRequest.newBuilder()
            .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $accessToken")
            .build()

        // (E) 요청 실행
        val response = chain.proceed(authed)
        Log.d(TAG, "← Response: path=$path, code=${response.code}")

        // (F) 401 처리
        if (response.code == 401) {
            // 로그아웃 요청은 refresh 금지. 응답은 그대로 반환(닫지 않음)
            if (isLogout) {
                Log.d(TAG, "401 on logout. Skip refresh, clear tokens.")
                clearTokens()
                return response
            }

            Log.d(TAG, "401 Unauthorized - attempting token refresh")

            synchronized(refreshLock) {
                // 이미 다른 스레드가 refresh 중이면 대기 → 갱신된 토큰으로 1회 재시도
                if (isRefreshing) {
                    while (isRefreshing) {
                        try {
                            refreshLock.wait(100)
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                    val newAccess = runBlocking { tokenManager.getAccessToken().firstOrNull() }
                    return if (!newAccess.isNullOrEmpty()) {
                        val retried = originalRequest.newBuilder()
                            .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $newAccess")
                            .header(HEADER_RETRY, "true") // 재시도 플래그
                            .build()
                        response.close()
                        chain.proceed(retried)
                    } else {
                        Log.w(TAG, "No new access token after wait. Propagate original 401.")
                        return response
                    }
                }
                isRefreshing = true
            }

            try {
                // refreshToken 조회
                val refreshToken = runBlocking { tokenManager.getRefreshToken().firstOrNull() }
                if (refreshToken.isNullOrEmpty()) {
                    Log.e(TAG, "No refresh token - clearing tokens.")
                    clearTokens()
                    return response // 기존 응답 그대로 전달
                }

                // refresh 호출
                val refreshResult = runBlocking {
                    try {
                        val raw = authApi.refreshToken(RefreshTokenRequest(refreshToken))
                        RefreshResult.Success(raw)
                    } catch (e: retrofit2.HttpException) {
                        val code = e.code()
                        Log.e(TAG, "Refresh HTTP error: $code - ${e.message()}")
                        when (code) {
                            401 -> RefreshResult.NeedReLogin("Refresh Token expired/invalid")
                            403 -> RefreshResult.NeedReLogin("Refresh Token not registered")
                            409 -> RefreshResult.NeedReLogin("Token reuse detected")
                            400 -> RefreshResult.Failed("Bad request (invalid token format)")
                            429 -> RefreshResult.Failed("Rate limit exceeded")
                            500 -> RefreshResult.Failed("Server internal error")
                            else -> RefreshResult.Failed("Unknown HTTP error")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Refresh network error", e)
                        RefreshResult.Failed("Network error")
                    }
                }

                return when (refreshResult) {
                    is RefreshResult.Success -> {
                        // 새 토큰 저장
                        runBlocking {
                            tokenManager.saveTokens(
                                accessToken = refreshResult.response.data.accessToken,
                                refreshToken = refreshResult.response.data.refreshToken
                            )
                        }
                        Log.d(TAG, "Token refresh successful. Retrying original request.")

                        val retried = originalRequest.newBuilder()
                            .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE ${refreshResult.response.data.accessToken}")
                            .header(HEADER_RETRY, "true") // 루프 방지
                            .build()

                        response.close()
                        chain.proceed(retried)
                    }
                    is RefreshResult.NeedReLogin -> {
                        Log.e(TAG, "Re-login required: ${refreshResult.message}")
                        clearTokens()
                        // 기존 응답 그대로 전달 (닫힌 응답 반환 금지)
                        response
                    }
                    is RefreshResult.Failed -> {
                        Log.e(TAG, "Token refresh failed: ${refreshResult.message}")
                        // 기존 응답 그대로 전달
                        response
                    }
                }
            } finally {
                synchronized(refreshLock) {
                    isRefreshing = false
                    refreshLock.notifyAll()
                }
            }
        }

        return response
    }

    private fun clearTokens() {
        runBlocking { tokenManager.clearTokens() }
    }
}
