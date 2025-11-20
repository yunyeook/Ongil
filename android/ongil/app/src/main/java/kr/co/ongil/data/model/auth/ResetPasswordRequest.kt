package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 비밀번호 재설정 요청
 * POST /api/v1/auth/reset-password (추정)
 *
 * 실제 서버 요청:
 * {
 *   "verificationToken": "string",
 *   "newPassword": "g1cktTZ?cCEbp",
 *   "confirmPassword": "string"
 * }
 */
@Serializable
data class ResetPasswordRequest(
    val verificationToken: String,
    val newPassword: String,
    val confirmPassword: String
)
