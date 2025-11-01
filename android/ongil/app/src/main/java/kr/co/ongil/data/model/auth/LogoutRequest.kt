package kr.co.ongil.data.model.auth

/**
 * 로그아웃 요청 모델
 * POST /api/v1/auth/logout
 */
data class LogoutRequest(
    val refreshToken: String
)