package kr.co.ongil.data.model.map

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 길안내 시작 응답 DTO
 */
@Serializable
data class NavigationStartResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: NavigationDataDto
)

/**
 * 길안내 데이터 DTO
 */
@Serializable
data class NavigationDataDto(
    @SerialName("navigationId")
    val navigationId: String,

    @SerialName("route")
    val route: RouteDto,

    @SerialName("startedAt")
    val startedAt: String,

    @SerialName("expectedArrival")
    val expectedArrival: String,

    @SerialName("initiatedBy")
    val initiatedBy: String
)

/**
 * 경로 정보 DTO
 */
@Serializable
data class RouteDto(
    @SerialName("startLocation")
    val startLocation: NavigationLocationDto,

    @SerialName("endLocation")
    val endLocation: NavigationLocationDto,

    @SerialName("totalDistance")
    val totalDistance: Int,

    @SerialName("totalTime")
    val totalTime: Int,

    @SerialName("path")
    val path: List<NavigationPathDto>,

    @SerialName("guides")
    val guides: List<NavigationGuideDto>
)

/**
 * 경로 좌표 DTO
 */
@Serializable
data class NavigationPathDto(
    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double
)

/**
 * 길안내 가이드 DTO
 */
@Serializable
data class NavigationGuideDto(
    @SerialName("index")
    val index: Int,

    @SerialName("instruction")
    val instruction: String,

    @SerialName("distance")
    val distance: Int,

    @SerialName("time")
    val time: Int,

    @SerialName("turnTypeCode")
    val turnTypeCode: Int,

    @SerialName("turnType")
    val turnType: String,

    @SerialName("roadName")
    val roadName: String
)
