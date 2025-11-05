package kr.co.ongil.domain.usecase.user

import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.domain.repository.UserRepository
import java.io.File
import javax.inject.Inject

/**
 * 사용자 정보 수정 UseCase
 *
 * 로그인한 사용자의 정보를 수정합니다.
 */
class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * 사용자 정보 수정 실행
     * @param name 이름 (선택)
     * @param birth 생년월일 (선택)
     * @param phoneNumber 전화번호 (선택)
     * @param verificationToken 전화번호 인증 토큰 (전화번호 변경 시 필수)
     * @param profileImage 프로필 이미지 파일 (선택)
     * @return 수정된 사용자 정보
     */
    suspend operator fun invoke(
        name: String? = null,
        birth: String? = null,
        phoneNumber: String? = null,
        verificationToken: String? = null,
        profileImage: File? = null
    ): Result<UserDto> {
        // 전화번호 변경 시 인증 토큰 필수 검증
        if (phoneNumber != null && verificationToken == null) {
            return Result.failure(IllegalArgumentException("전화번호를 변경하려면 인증 토큰이 필요합니다."))
        }

        // Repository를 통해 사용자 정보 수정
        return userRepository.updateMyInfo(
            name = name,
            birth = birth,
            phoneNumber = phoneNumber,
            verificationToken = verificationToken,
            profileImage = profileImage
        )
    }
}
