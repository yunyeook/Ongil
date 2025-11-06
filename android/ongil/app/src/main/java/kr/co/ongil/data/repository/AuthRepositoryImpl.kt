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
        return try {
            // TokenManager에서 저장된 refreshToken 가져오기
            val refreshToken = tokenManager.getRefreshToken().firstOrNull()

            // 토큰이 없는 경우 에러 처리
            if (refreshToken == null) {
                return Result.failure(IllegalStateException("토큰이 없습니다. 로그인이 필요합니다."))
            }

            val response = authApi.logout(
                request = LogoutRequest(refreshToken = refreshToken)
            )

            // 로컬 토큰 삭제
            tokenManager.clearTokens()

            Result.success(response.message)
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}
