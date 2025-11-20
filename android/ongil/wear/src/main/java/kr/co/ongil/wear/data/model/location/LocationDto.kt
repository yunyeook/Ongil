package kr.co.ongil.wear.data.model.location

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 환자 위치 전송 Request
 * POST /api/v1/patients/{patientId}/location
 */
@Serializable
data class UpdateLocationRequest(
    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double
)

/**
 * 환자 위치 전송 Response
 */
@Serializable
data class UpdateLocationResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: String? = null
)

/**
 * 네비게이션 시작 요청 DTO
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
 * 네비게이션 시작 응답 DTO
 */
@Serializable
data class NavigationStartResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: NavigationData
)

/**
 * 네비게이션 종료 요청 DTO
 * POST /api/v1/navigation/end
 */
@Serializable
data class NavigationEndRequest(
    @SerialName("navigationId")
    val navigationId: Long,

    @SerialName("endedBy")
    val endedBy: String
)

/**
 * 네비게이션 종료 응답 DTO
 */
@Serializable
data class NavigationEndResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: String? = null
)

/**
 * 네비게이션 위치 정보 DTO
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

/**
 * 네비게이션 데이터 DTO
 */
@Serializable
data class NavigationData(
    @SerialName("navigationId")
    val navigationId: Long,

    @SerialName("patientId")
    val patientId: Long,

    @SerialName("startLocation")
    val startLocation: NavigationLocationDto,

    @SerialName("endLocation")
    val endLocation: NavigationLocationDto,

    @SerialName("status")
    val status: String
)
