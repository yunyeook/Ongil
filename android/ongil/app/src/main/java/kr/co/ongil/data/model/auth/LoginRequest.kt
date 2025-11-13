package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 로그인 요청 모델
 * POST /api/v1/auth/login
 */
@Serializable
data class LoginRequest(
    val phoneNumber: String,
    val password: String
)
