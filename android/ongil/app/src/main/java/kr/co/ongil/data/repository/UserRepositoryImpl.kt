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

/**
 * 사용자 Repository 구현체
 */
class UserRepositoryImpl(
    private val userApi: UserApi? = null,
    private val authApi: AuthApi? = null
    // TODO: TokenManager 추가하여 accessToken 자동으로 가져오기
    // TODO: DI(Hilt/Koin)로 주입하도록 변경
) : UserRepository {

    override suspend fun getMyInfo(): Result<UserDto> {
        return try {
            // 네트워크 지연 시뮬레이션
            delay(500)

            // 하드코딩된 Mock 데이터
            val mockUser = UserDto(
                id = 1,
                name = "홍길동",
                birth = "19980919",
                phoneNumber = "01012341234",
                userType = "PATIENT",
                profileImage = null // 또는 "https://example.com/profile.jpg"
            )

            Result.success(mockUser)

            /* TODO: 실제 API 연동 (위의 하드코딩 부분을 아래로 교체)
            val accessToken = tokenManager.getAccessToken()
            val response = userApi.getMyInfo("Bearer $accessToken")
            Result.success(response.data.user)
            */
        } catch (e: Exception) {
            Result.failure(e)
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

            if (authApi == null) {
                throw IllegalStateException("AuthApi가 주입되지 않았습니다. DI를 통해 주입해주세요.")
            }

            authApi.sendVerificationCode(
                accessToken = accessToken,
                request = SendVerificationRequest(phoneNumber = phoneNumber)
            )

            Result.success(Unit)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun verifyCode(phoneNumber: String, code: String): Result<String> {
        return try {
            // TODO: TokenManager에서 accessToken 가져오기
            val accessToken = "Bearer YOUR_ACCESS_TOKEN"

            if (authApi == null) {
                throw IllegalStateException("AuthApi가 주입되지 않았습니다. DI를 통해 주입해주세요.")
            }

            val response = authApi.verifyCode(
                accessToken = accessToken,
                request = VerifyCodeRequest(phoneNumber = phoneNumber, code = code)
            )

            Result.success(response.data.verificationToken)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            // TODO: TokenManager에서 accessToken 가져오기
            // val accessToken = "Bearer YOUR_ACCESS_TOKEN"

            // if (userApi == null) {
            //     throw IllegalStateException("UserApi가 주입되지 않았습니다. DI를 통해 주입해주세요.")
            // }

            // TODO: 실제 API 엔드포인트 추가 필요
            // 예상: PATCH /api/v1/users/me/password
            // val request = ChangePasswordRequest(currentPassword, newPassword)
            // userApi.changePassword(accessToken, request)

            // 임시: API 명세 나오면 구현
            Result.failure(UnsupportedOperationException("비밀번호 변경 API 명세가 필요합니다."))
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}