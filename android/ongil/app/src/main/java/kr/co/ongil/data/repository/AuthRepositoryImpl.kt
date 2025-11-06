package kr.co.ongil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kr.co.ongil.data.datasource.local.preferences.TokenManager
import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.model.auth.LoginRequest
import kr.co.ongil.data.model.auth.LoginResponse
import kr.co.ongil.data.model.auth.LogoutRequest
import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 인증 Repository 구현체
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(phoneNumber: String, password: String): Result<LoginResponse> {
        return try {
            // 실제 API 호출
            val response = authApi.login(
                request = LoginRequest(
                    phoneNumber = phoneNumber,
                    password = password
                )
            )

            // 토큰 저장은 LoginUseCase에서 처리

            Result.success(response)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun logout(): Result<String> {
        // 1) 현재 토큰 상태 확인
        val accessToken = tokenManager.getAccessToken().firstOrNull()
        val refreshToken = tokenManager.getRefreshToken().firstOrNull()

        return try {
            if (!accessToken.isNullOrEmpty()) {
                // 2) accessToken 있을 때만 서버 로그아웃 호출
                //    (Authorization 헤더는 AuthInterceptor가 자동 부착)
                if (refreshToken.isNullOrEmpty()) {
                    // 서버가 바디를 요구한다면 기존처럼 보내고, 아니면 AuthApi.logout() 바디 없는 버전 사용
                    // 여기서는 기존 시그니처 유지
                    authApi.logout(request = LogoutRequest(refreshToken = ""))
                } else {
                    authApi.logout(request = LogoutRequest(refreshToken = refreshToken))
                }
            } else {
                // 3) 토큰 없으면 서버 호출 생략 (Authorization 헤더 못 붙음 → 500 유발 방지)
                //    로그만 남기고 로컬 정리로 진행
                android.util.Log.w("AuthRepositoryImpl", "logout skipped: no access token present")
            }

            // 4) 정상/스킵 모두 여기로: 로컬 세션 정리
            tokenManager.clearTokens()
            Result.success("로그아웃되었습니다.")
        } catch (e: Exception) {
            // 5) 서버 4xx/5xx, 네트워크 오류 → 소프트 처리
            val apiException = ErrorHandler.handleException(e)
            android.util.Log.w("AuthRepositoryImpl", "server logout failed (soft-ignored)", apiException)

            // 그래도 로컬 세션은 반드시 종료
            tokenManager.clearTokens()
            // UX 일관성을 위해 성공으로 취급(원하면 failure 반환으로 바꿔도 됨)
            Result.success("로그아웃되었습니다. (서버와 동기화 실패)")
        }
    }
}
