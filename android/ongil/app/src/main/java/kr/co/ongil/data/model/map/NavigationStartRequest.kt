package kr.co.ongil.data.model.map

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 길안내 시작 요청 DTO
 * POST /api/v1/navigation/start
 */
@Serializable
data class NavigationStartRequest(
    @SerialName("patientId")
    val patientId: Long,

    @SerialName("startLocation")
    val startLocation: NavigationLocationDto,

    @SerialName("endLocation")
    val endLocation: NavigationLocationDto,

    @SerialName("initiatedBy")
    val initiatedBy: String
)

/**
 * 길안내 위치 정보 DTO
 */
@Serializable
data class NavigationLocationDto(
    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double,

    @SerialName("name")
    val name: String
)
