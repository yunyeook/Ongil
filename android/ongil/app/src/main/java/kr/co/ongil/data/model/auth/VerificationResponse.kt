package kr.co.ongil.data.model.auth

/**
 * 인증번호 발송 응답
 */
data class SendVerificationResponse(
    val message: String,
    val data: SendVerificationData?
)

data class SendVerificationData(
    val expiresAt: String? = null
)

/**
 * 인증번호 확인 응답
 */
data class VerifyCodeResponse(
    val message: String,
    val data: VerifyCodeData
)

data class VerifyCodeData(
    val verificationToken: String
)
