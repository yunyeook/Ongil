package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 로그아웃 응답 모델
 * POST /api/v1/auth/logout
 */
@Serializable
data class LogoutResponse(
    val message: String
)
