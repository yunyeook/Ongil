package kr.co.ongil.data.repository

import kotlinx.coroutines.delay
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
    private val authApi: AuthApi
    // TODO: TokenManager 추가
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

            // TODO: 토큰 저장
            // tokenManager.saveTokens(response.data.accessToken, response.data.refreshToken)

            Result.success(response)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun logout(): Result<String> {
        return try {
            // TODO: TokenManager에서 accessToken, refreshToken 가져오기
            val accessToken = "Bearer YOUR_ACCESS_TOKEN"
            val refreshToken = "YOUR_REFRESH_TOKEN"

            val response = authApi.logout(
                accessToken = accessToken,
                request = LogoutRequest(refreshToken = refreshToken)
            )

            // TODO: 로컬 토큰 삭제
            // tokenManager.clearTokens()

            Result.success(response.message)
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}
