package kr.co.ongil.domain.repository

import kr.co.ongil.data.model.user.UserDto
import java.io.File

/**
 * 사용자 Repository 인터페이스
 */
interface UserRepository {

    /**
     * 내 정보 조회
     */
    suspend fun getMyInfo(): Result<UserDto>

    /**
     * 내 정보 수정
     * @param name 이름 (선택)
     * @param birth 생년월일 (선택)
     * @param phoneNumber 전화번호 (선택)
     * @param verificationToken 전화번호 인증 토큰 (전화번호 변경 시 필수)
     * @param profileImage 프로필 이미지 파일 (선택)
     */
    suspend fun updateMyInfo(
        name: String? = null,
        birth: String? = null,
        phoneNumber: String? = null,
        verificationToken: String? = null,
        profileImage: File? = null
    ): Result<UserDto>

    /**
     * 전화번호 인증번호 발송
     * @param phoneNumber 전화번호
     */
    suspend fun sendVerificationCode(phoneNumber: String): Result<Unit>

    /**
     * 인증번호 확인
     * @param phoneNumber 전화번호
     * @param code 인증번호
     * @return 인증 성공 시 verificationToken 반환
     */
    suspend fun verifyCode(phoneNumber: String, code: String): Result<String>

    /**
     * 비밀번호 변경
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
}