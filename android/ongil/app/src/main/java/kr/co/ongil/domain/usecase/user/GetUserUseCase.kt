package kr.co.ongil.domain.usecase.user

import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.domain.repository.UserRepository
import javax.inject.Inject

/**
 * 사용자 정보 조회 UseCase
 *
 * 로그인한 사용자의 정보를 조회합니다.
 */
class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * 사용자 정보 조회 실행
     * @return 사용자 정보
     */
    suspend operator fun invoke(): Result<UserDto> {
        return userRepository.getMyInfo()
    }
}
