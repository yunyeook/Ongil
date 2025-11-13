package kr.co.ongil.domain.repository

/**
 * 비밀번호 찾기 인증 Repository 인터페이스
 */
interface FindPasswordAuthRepository {
    /**
     * 인증번호 발송
     * @param phone 전화번호
     */
    suspend fun sendVerificationCode(phone: String): Result<Unit>

    /**
     * 인증번호 확인
     * @param phone 전화번호
     * @param code 인증번호
     * @return 인증 성공 여부
     */
    suspend fun verifyCode(phone: String, code: String): Result<Boolean>
}
