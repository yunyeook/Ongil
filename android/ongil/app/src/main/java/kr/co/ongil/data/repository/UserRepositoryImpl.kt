package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
 *
 * 참고: Authorization 헤더는 AuthInterceptor가 TokenManager에서 자동으로 가져와 추가합니다.
 */
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val authApi: AuthApi
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
    }

    override fun getMyInfo(): Flow<Result<UserDto>> = flow {
        try {
            Log.d(TAG, "getMyInfo() - API 호출 시작")
            val response = userApi.getMyInfo()
            Log.d(TAG, "getMyInfo() - API 호출 성공: ${response.data}")
            emit(Result.success(response.data))  // data에 바로 UserDto
        } catch (e: Exception) {
            Log.e(TAG, "getMyInfo() - API 호출 실패", e)
            val apiException = ErrorHandler.handleException(e)
            emit(Result.failure(apiException))
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
            Log.d(TAG, "updateMyInfo() - name: $name, birth: $birth, phoneNumber: $phoneNumber, hasToken: ${verificationToken != null}")

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

            Log.d(TAG, "Calling updateMyInfo API")
            // API 호출
            val updateResponse = userApi.updateMyInfo(
                name = namePart,
                birth = birthPart,
                phoneNumber = phoneNumberPart,
                verificationToken = verificationTokenPart,
                profileImage = profileImagePart
            )
            Log.d(TAG, "updateMyInfo API 성공: ${updateResponse.message}, user: ${updateResponse.data.user}")

            // 응답에서 바로 수정된 사용자 정보 반환
            Result.success(updateResponse.data.user)
        } catch (e: Exception) {
            Log.e(TAG, "updateMyInfo 실패", e)
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun sendVerificationCode(phoneNumber: String): Result<Unit> {
        return try {
            Log.d(TAG, "sendVerificationCode() - API 호출 시작: $phoneNumber")
            authApi.sendVerificationCode(
                request = SendVerificationRequest(phoneNumber = phoneNumber)
            )
            Log.d(TAG, "sendVerificationCode() - API 호출 성공")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendVerificationCode() - API 호출 실패", e)
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun verifyCode(phoneNumber: String, code: String): Result<String> {
        return try {
            val response = authApi.verifyCode(
                request = VerifyCodeRequest(
                    phoneNumber = phoneNumber,
                    verificationCode = code,
                    grants = "PHONE_UPDATE"  // 휴대폰 번호 변경용
                )
            )

            Result.success(response.data.verificationToken)
        } catch (e: Exception) {
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            // 비밀번호 변경 API 호출
            val request = kr.co.ongil.data.model.auth.ChangePasswordRequest(
                oldPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = newPassword // ViewModel에서 이미 검증했으므로 동일한 값 전달
            )

            userApi.changePassword(request = request)

            Result.success(Unit)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}