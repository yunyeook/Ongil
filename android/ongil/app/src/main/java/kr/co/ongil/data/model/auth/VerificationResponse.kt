package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 인증번호 발송 응답
 * POST /api/v1/phone-verifications
 *
 * 실제 서버 응답:
 * {
 *   "message": "전화번호 인증 요청이 성공적으로 완료되었습니다.",
 *   "data": ""  ← 빈 문자열
 * }
 */
@Serializable
data class SendVerificationResponse(
    val message: String,
    val data: String  // 서버가 빈 문자열 "" 전송
)

/**
 * 인증번호 확인 응답
 * POST /api/v1/phone-verifications/verify
 *
 * 실제 서버 응답:
 * {
 *   "message": "인증이 성공적으로 완료되었습니다.",
 *   "data": {
 *     "verified": true,
 *     "verificationToken": "eyJhbGciOi..."
 *   }
 * }
 */
@Serializable
data class VerifyCodeResponse(
    val message: String,
    val data: VerifyCodeData
)

@Serializable
data class VerifyCodeData(
    val verified: Boolean,
    val verificationToken: String
)
