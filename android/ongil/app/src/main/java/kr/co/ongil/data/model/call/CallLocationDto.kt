package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * 통화 위치 정보 DTO
 */
@Serializable
data class CallLocationDto(
    val latitude: Double,
    val longitude: Double
)