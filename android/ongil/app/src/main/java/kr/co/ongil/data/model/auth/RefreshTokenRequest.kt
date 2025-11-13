package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 토큰 재발급 요청 모델
 * POST /api/v1/auth/refresh
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)
