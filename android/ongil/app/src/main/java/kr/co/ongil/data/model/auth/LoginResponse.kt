package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable
import kr.co.ongil.data.model.user.UserDto

/**
 * 로그인 응답 모델
 * POST /api/v1/auth/login
 */
@Serializable
data class LoginResponse(
    val message: String,
    val data: LoginData
)

@Serializable
data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)
