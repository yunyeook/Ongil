package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 비밀번호 재설정 응답
 * POST /api/v1/auth/reset-password (추정)
 *
 * 실제 서버 응답:
 * {
 *   "message": "string",
 *   "data": "string"
 * }
 */
@Serializable
data class ResetPasswordResponse(
    val message: String,
    val data: String
)
