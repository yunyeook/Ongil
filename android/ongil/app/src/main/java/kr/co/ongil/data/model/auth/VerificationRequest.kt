package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 전화번호 인증 발송 요청
 * POST /api/v1/phone-verifications
 *
 * 실제 서버 요청:
 * {
 *   "phoneNumber": "01012345678"
 * }
 */
@Serializable
data class SendVerificationRequest(
    val phoneNumber: String
)

/**
 * 인증번호 확인 요청
 * POST /api/v1/phone-verifications/verify
 *
 * 실제 서버 요청:
 * {
 *   "phoneNumber": "01012345678",
 *   "verificationCode": "839201",
 *   "grants": "PASSWORD_RESET"  // 선택: PASSWORD_RESET, RELATIONSHIP, PHONE_UPDATE
 * }
 */
@Serializable
data class VerifyCodeRequest(
    val phoneNumber: String,
    val verificationCode: String,
    val grants: String? = null  // 선택적 필드
)
