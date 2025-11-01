package kr.co.ongil.domain.repository

import kr.co.ongil.data.model.user.UserDto

/**
 * 사용자 Repository 인터페이스
 */
interface UserRepository {

    /**
     * 내 정보 조회
     */
    suspend fun getMyInfo(): Result<UserDto>
}