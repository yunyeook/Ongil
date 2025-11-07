package kr.co.ongil.data.model.error

/**
 * API 에러 응답 모델
 */
data class ApiError(
    val statusCode: Int,
    val message: String,
    val timestamp: String? = null,
    val path: String? = null
)