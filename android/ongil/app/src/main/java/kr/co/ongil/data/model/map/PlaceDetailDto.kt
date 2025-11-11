package kr.co.ongil.data.model.map

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 장소 상세 정보 응답 DTO
 */
@Serializable
data class PlaceDetailApiResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: PlaceDetailDto
)

/**
 * 장소 상세 정보 DTO
 */
@Serializable
data class PlaceDetailDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("address")
    val address: AddressDto,

    @SerialName("coordinate")
    val coordinate: CoordinateDto,

    @SerialName("category")
    val category: CategoryDto,

    @SerialName("phoneNumber")
    val phoneNumber: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("zipCode")
    val zipCode: String? = null,

    @SerialName("parking")
    val parking: Boolean? = null,

    @SerialName("businessInfo")
    val businessInfo: BusinessInfoDto? = null
)

/**
 * 주소 정보 DTO
 */
@Serializable
data class AddressDto(
    @SerialName("roadAddress")
    val roadAddress: String? = null,

    @SerialName("jibunAddress")
    val jibunAddress: String? = null
)

/**
 * 좌표 정보 DTO
 */
@Serializable
data class CoordinateDto(
    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double
)

/**
 * 영업 정보 DTO
 */
@Serializable
data class BusinessInfoDto(
    @SerialName("businessHours")
    val businessHours: String? = null,

    @SerialName("closedDays")
    val closedDays: String? = null,

    @SerialName("is24Hours")
    val is24Hours: Boolean? = null,

    @SerialName("isYearRound")
    val isYearRound: Boolean? = null
)
