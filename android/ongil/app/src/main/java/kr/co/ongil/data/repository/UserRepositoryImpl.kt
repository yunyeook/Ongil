package kr.co.ongil.data.repository

import kotlinx.coroutines.delay
import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.datasource.remote.api.UserApi
import kr.co.ongil.data.model.auth.SendVerificationRequest
import kr.co.ongil.data.model.auth.VerifyCodeRequest
import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.UserRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

/**
 * 사용자 Repository 구현체
 */
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val authApi: AuthApi
    // TODO: TokenManager 추가하여 accessToken 자동으로 가져오기
) : UserRepository {

    override suspend fun getMyInfo(): Result<UserDto> {
        return try {
            // TODO: TokenManager에서 accessToken 가져오기
            val accessToken = "Bearer YOUR_ACCESS_TOKEN"

            val response = userApi.getMyInfo(accessToken)
            Result.success(response.data.user)
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun updateMyInfo(
        name: String?,
        birth: String?,
        phoneNumber: String?,
        verificationToken: String?,
        profileImage: File?
    ): Result<UserDto> {
        return try {
            // TODO: TokenManager에서 accessToken 가져오기
            val accessToken = "Bearer YOUR_ACCESS_TOKEN"

            // API 주입 확인
            if (userApi == null) {
                throw IllegalStateException("UserApi가 주입되지 않았습니다. DI를 통해 주입해주세요.")
            }

            // RequestBody 생성
            val namePart = name?.toRequestBody("text/plain".toMediaTypeOrNull())
            val birthPart = birth?.toRequestBody("text/plain".toMediaTypeOrNull())
            val phoneNumberPart = phoneNumber?.toRequestBody("text/plain".toMediaTypeOrNull())
            val verificationTokenPart = verificationToken?.toRequestBody("text/plain".toMediaTypeOrNull())

            // 프로필 이미지 MultipartBody.Part 생성
            val profileImagePart = profileImage?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profileImage", it.name, requestFile)
            }

            // API 호출
            val response = userApi.updateMyInfo(
                accessToken = accessToken,
                name = namePart,
                birth = birthPart,
                phoneNumber = phoneNumberPart,
                verificationToken = verificationTokenPart,
                profileImage = profileImagePart
            )

            Result.success(response.data.user)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun sendVerificationCode(phoneNumber: String): Result<Unit> {
        return try {
            // TODO: TokenManager에서 accessToken 가져오기
            val accessToken = "Bearer YOUR_ACCESS_TOKEN"

            authApi.sendVerificationCode(
                accessToken = accessToken,
                request = SendVerificationRequest(phoneNumber = phoneNumber)
            )

            Result.success(Unit)
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun verifyCode(phoneNumber: String, code: String): Result<String> {
        return try {
            // TODO: TokenManager에서 accessToken 가져오기
            val accessToken = "Bearer YOUR_ACCESS_TOKEN"

            val response = authApi.verifyCode(
                accessToken = accessToken,
                request = VerifyCodeRequest(phoneNumber = phoneNumber, code = code)
            )

            Result.success(response.data.verificationToken)
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            // TODO: TokenManager에서 accessToken 가져오기
            val accessToken = "Bearer YOUR_ACCESS_TOKEN"

            if (userApi == null) {
                throw IllegalStateException("UserApi가 주입되지 않았습니다. DI를 통해 주입해주세요.")
            }

            // 비밀번호 변경 API 호출
            val request = kr.co.ongil.data.model.auth.ChangePasswordRequest(
                oldPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = newPassword // ViewModel에서 이미 검증했으므로 동일한 값 전달
            )

            userApi.changePassword(
                accessToken = accessToken,
                request = request
            )

            Result.success(Unit)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}