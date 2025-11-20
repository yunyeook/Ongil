package kr.co.ongil.domain.repository

import kr.co.ongil.data.model.auth.LoginResponse

/**
 * 인증 Repository 인터페이스
 */
interface AuthRepository {

    /**
     * 로그인
     * @param phoneNumber 전화번호
     * @param password 비밀번호
     * @return 로그인 응답 (토큰 및 사용자 정보)
     */
    suspend fun login(phoneNumber: String, password: String): Result<LoginResponse>

    /**
     * 로그아웃
     */
    suspend fun logout(): Result<String>

    /**
     * 회원가입
     * @return 서버 응답 메시지
     */
    suspend fun registerUser(
        name: String,
        birth: String?,
        phoneNumber: String,
        verificationToken: String,
        password: String,
        userType: String,
        profileImagePath: String?
    ): Result<String>
}
