package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 토큰 재발급 응답 모델
 * POST /api/v1/auth/refresh
 */
@Serializable
data class RefreshTokenResponse(
    val message: String,
    val data: RefreshTokenData
)

@Serializable
data class RefreshTokenData(
    val accessToken: String,
    val refreshToken: String
)
