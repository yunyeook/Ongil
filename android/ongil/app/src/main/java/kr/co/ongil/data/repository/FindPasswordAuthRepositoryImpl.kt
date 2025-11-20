package kr.co.ongil.data.repository

import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.model.auth.ResetPasswordRequest
import kr.co.ongil.data.model.auth.SendVerificationRequest
import kr.co.ongil.data.model.auth.VerifyCodeRequest
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.FindPasswordAuthRepository
import javax.inject.Inject

/**
 * 비밀번호 찾기 인증 Repository 구현체
 *
 * 참고: 비밀번호 찾기는 로그인 전이므로 Authorization 헤더 불필요
 * AuthInterceptor는 토큰이 없으면 헤더를 추가하지 않습니다.
 */
class FindPasswordAuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : FindPasswordAuthRepository {

    override suspend fun sendVerificationCode(phone: String): Result<Unit> {
        return try {
            authApi.sendVerificationCode(
                request = SendVerificationRequest(phoneNumber = phone)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun verifyCode(phone: String, code: String): Result<String> {
        return try {
            val response = authApi.verifyCode(
                request = VerifyCodeRequest(
                    phoneNumber = phone,
                    verificationCode = code,
                    grants = "PASSWORD_RESET"  // 비밀번호 찾기용
                )
            )
            // verified가 true이면 verificationToken 반환, 아니면 실패
            if (response.data.verified) {
                Result.success(response.data.verificationToken)
            } else {
                Result.failure(Exception("인증번호가 올바르지 않습니다."))
            }
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun resetPassword(
        verificationToken: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        return try {
            authApi.resetPassword(
                request = ResetPasswordRequest(
                    verificationToken = verificationToken,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}
