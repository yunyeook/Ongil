package kr.co.ongil.data.repository

import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.model.auth.SendVerificationRequest
import kr.co.ongil.data.model.auth.VerifyCodeRequest
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.FindPasswordAuthRepository
import javax.inject.Inject

/**
 * 비밀번호 찾기 인증 Repository 구현체
 */
class FindPasswordAuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : FindPasswordAuthRepository {

    override suspend fun sendVerificationCode(phone: String): Result<Unit> {
        return try {
            // 비밀번호 찾기는 로그인 전이므로 빈 토큰 전송
            authApi.sendVerificationCode(
                accessToken = "",
                request = SendVerificationRequest(
                    phoneNumber = phone,
                    grant = "PASSWORD_RESET"  // 비밀번호 찾기용
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun verifyCode(phone: String, code: String): Result<Boolean> {
        return try {
            // 비밀번호 찾기는 로그인 전이므로 빈 토큰 전송
            val response = authApi.verifyCode(
                accessToken = "",
                request = VerifyCodeRequest(
                    phoneNumber = phone,
                    code = code,
                    grant = "PASSWORD_RESET"  // 비밀번호 찾기용
                )
            )
            // verificationToken이 있으면 성공
            Result.success(response.data.verificationToken.isNotEmpty())
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}
