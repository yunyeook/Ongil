package kr.co.ongil.wear.data.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Refresh Token 요청 DTO
 * POST /api/v1/auth/refresh
 */
@Serializable
data class RefreshTokenRequest(
    @SerialName("refreshToken")
    val refreshToken: String
)

/**
 * Refresh Token 응답 DTO
 */
@Serializable
data class RefreshTokenResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: RefreshTokenData
)

@Serializable
data class RefreshTokenData(
    @SerialName("accessToken")
    val accessToken: String,

    @SerialName("refreshToken")
    val refreshToken: String
)
