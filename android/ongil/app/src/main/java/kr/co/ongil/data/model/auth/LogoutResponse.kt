package kr.co.ongil.data.model.auth

/**
 * 로그아웃 응답 모델
 * POST /api/v1/auth/logout
 */
data class LogoutResponse(
    val message: String,
    val data: Map<String, Any> = emptyMap() // 빈 객체
)
