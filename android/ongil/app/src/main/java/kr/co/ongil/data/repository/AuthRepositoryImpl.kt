package kr.co.ongil.data.repository

import kotlinx.coroutines.flow.firstOrNull
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.model.auth.LoginRequest
import kr.co.ongil.data.model.auth.LoginResponse
import kr.co.ongil.data.model.auth.LogoutRequest
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.AuthRepository
import javax.inject.Inject
// 회원가입 관련
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
// 여기까지

/**
 * 인증 Repository 구현체
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDataStoreManager: UserDataStoreManager
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
        val accessToken = userDataStoreManager.getAccessToken().firstOrNull()
        val refreshToken = userDataStoreManager.getRefreshToken().firstOrNull()

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
            userDataStoreManager.clearTokens()
            userDataStoreManager.clearLoginUserId()
            userDataStoreManager.setFcmTokenSynced(false)
            Result.success("로그아웃되었습니다.")
        } catch (e: Exception) {
            // 5) 서버 4xx/5xx, 네트워크 오류 → 소프트 처리
            val apiException = ErrorHandler.handleException(e)
            android.util.Log.w("AuthRepositoryImpl", "server logout failed (soft-ignored)", apiException)

            // 그래도 로컬 세션은 반드시 종료
            userDataStoreManager.clearTokens()
            userDataStoreManager.clearLoginUserId()
            userDataStoreManager.setFcmTokenSynced(false)
            // UX 일관성을 위해 성공으로 취급(원하면 failure 반환으로 바꿔도 됨)
            Result.success("로그아웃되었습니다. (서버와 동기화 실패)")
        }
    }

    // 회원가입
    override suspend fun registerUser(
        name: String,
        birth: String?,
        phoneNumber: String,
        verificationToken: String,
        password: String,
        userType: String,
        profileImagePath: String?
    ): Result<String> {
        return try {
            // client-side normalization
            val digitsPhone = phoneNumber.filter { it.isDigit() }

            val providerBody = "LOCAL".toPlain()
            val nameBody = name.toPlain()
            val birthBody = birth?.toPlain()
            val phoneBody = digitsPhone.toPlain()
            val tokenBody = verificationToken.toPlain()
            val passwordBody = password.toPlain()
            val userTypeBody = userType.toPlain()

            val imagePart = profileImagePath
                ?.takeIf { it.isNotBlank() }
                ?.let { path ->
                    val file = File(path)
                    file.toImagePart(field = "profileImage")
                }

            val response = authApi.registerUser(
                provider = providerBody,
                name = nameBody,
                birth = birthBody,
                phoneNumber = phoneBody,
                verificationToken = tokenBody,
                password = passwordBody,
                userType = userTypeBody,
                profileImage = imagePart
            )

            android.util.Log.d("AuthRepositoryImpl", "registerUser response: message=${response.message}")
            Result.success(response.message)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepositoryImpl", "registerUser error", e)
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }


    // 멀티파트 헬퍼
    private fun String.toPlain(): RequestBody =
        this.toRequestBody("text/plain".toMediaType())

    private fun File.toImagePart(field: String = "profileImage"): MultipartBody.Part =
        MultipartBody.Part.createFormData(
            field,
            this.name,
            this.asRequestBody("image/*".toMediaType())
        )
}
