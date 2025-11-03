package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 인증번호 발송 응답
 */
@Serializable
data class SendVerificationResponse(
    val message: String,
    val data: SendVerificationData? = null
)

@Serializable
data class SendVerificationData(
    val expiresAt: String? = null
)

/**
 * 인증번호 확인 응답
 */
@Serializable
data class VerifyCodeResponse(
    val message: String,
    val data: VerifyCodeData
)

@Serializable
data class VerifyCodeData(
    val verificationToken: String
)
