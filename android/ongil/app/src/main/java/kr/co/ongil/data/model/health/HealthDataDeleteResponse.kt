package kr.co.ongil.data.model.health

import kotlinx.serialization.Serializable

/**
 * 건강 데이터 삭제 응답
 */
@Serializable
data class HealthDataDeleteResponse(
    val message: String
)