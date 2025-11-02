package kr.co.ongil.domain.repository

/**
 * 인증 Repository 인터페이스
 */
interface AuthRepository {

    /**
     * 로그아웃
     */
    suspend fun logout(): Result<String>
}
