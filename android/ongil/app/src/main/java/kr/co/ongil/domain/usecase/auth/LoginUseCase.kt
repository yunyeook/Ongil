package kr.co.ongil.domain.usecase.auth

import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.model.auth.LoginResponse
import kr.co.ongil.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 로그인 UseCase
 *
 * 사용자 로그인 처리를 담당합니다.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: UserDataStoreManager
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
            // 로그인 성공 시 토큰 저장
            tokenManager.saveTokens(
                accessToken = response.data.accessToken,
                refreshToken = response.data.refreshToken
            )
        }
    }
}
