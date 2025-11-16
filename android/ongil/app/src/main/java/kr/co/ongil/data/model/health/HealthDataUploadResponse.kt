package kr.co.ongil.data.model.health

import kotlinx.serialization.Serializable

/**
 * 생체 데이터 업로드 응답
 */
@Serializable
data class HealthDataUploadResponse(
    val message: String,
    val data: String? = null
)