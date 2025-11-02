package kr.co.ongil.data.model.auth

/**
 * 전화번호 인증 발송 요청
 * POST /api/v1/auth/verification/send
 */
data class SendVerificationRequest(
    val phoneNumber: String,
    val grant: String = "PHONE_UPDATE"
)

/**
 * 인증번호 확인 요청
 * POST /api/v1/auth/verification/verify
 */
data class VerifyCodeRequest(
    val phoneNumber: String,
    val code: String,
    val grant: String = "PHONE_UPDATE"
)
