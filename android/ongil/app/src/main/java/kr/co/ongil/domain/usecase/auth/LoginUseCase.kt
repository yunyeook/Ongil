package kr.co.ongil.domain.usecase.auth

import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.model.auth.LoginResponse
import kr.co.ongil.domain.repository.AuthRepository
import javax.inject.Inject
import kr.co.ongil.data.datasource.wear.WearDataClient

/**
 * 로그인 UseCase
 *
 * 사용자 로그인 처리를 담당합니다.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: UserDataStoreManager,
    private val wearDataClient: WearDataClient
) {
    /**
     * 로그인 실행
     * @param phoneNumber 전화번호
     * @param password 비밀번호
     * @return 로그인 응답 (토큰 및 사용자 정보)
     */
    suspend operator fun invoke(phoneNumber: String, password: String): Result<LoginResponse> {
        // 입력 유효성 검증
        if (phoneNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("전화번호를 입력해주세요."))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("비밀번호를 입력해주세요."))
        }

        // Repository를 통해 로그인 처리
        return authRepository.login(phoneNumber, password).onSuccess { response ->
            // 로그인 성공 시 토큰 및 사용자 ID 저장
            tokenManager.saveTokens(
                accessToken = response.data.accessToken,
                refreshToken = response.data.refreshToken
            )
            // 사용자 ID와 타입 저장
            tokenManager.saveLoginUserId(response.data.user.id.toString())
            tokenManager.saveUserType(response.data.user.userType)
            // 프로필 이미지 저장
            tokenManager.saveProfileImage(
                userId = response.data.user.id.toString(),
                profileImageUrl = response.data.user.profileImage
            )

            //  워치 동기화 로직
            try {
                wearDataClient.syncLoginData(
                    accessToken = response.data.accessToken,
                    refreshToken = response.data.refreshToken,
                    userId = response.data.user.id.toString(),
                    userType = response.data.user.userType,
                    selectedPatientId = null  // 로그인 시점에는 선택 안됨
                )
            } catch (e: Exception) {
                // 워치 동기화 실패해도 로그인은 성공으로 처리
                android.util.Log.e("LoginUseCase", "워치 동기화 실패 (무시)", e)
            }
        }
    }

}
