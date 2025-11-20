package kr.co.ongil.data.model.map

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 최상위 응답 DTO
 */
@Serializable
data class SearchPlaceApiResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: SearchPlaceData
)

/**
 * 데이터 영역
 */
@Serializable
data class SearchPlaceData(
    @SerialName("pageInfo")
    val pageInfo: PageInfo,

    @SerialName("places")
    val places: List<SearchPlaceDto>
)

/**
 * 페이지 정보
 */
@Serializable
data class PageInfo(
    @SerialName("totalElements")
    val totalElements: Int,

    @SerialName("totalPages")
    val totalPages: Int,

    @SerialName("isLast")
    val isLast: Boolean,

    @SerialName("currPage")
    val currPage: Int
)

/**
 * 장소 정보 DTO
 */
@Serializable
data class SearchPlaceDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("address")
    val address: String,

    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double,

    @SerialName("distance")
    val distance: Int? = null,

    @SerialName("category")
    val category: CategoryDto? = null,

    @SerialName("phoneNumber")
    val phoneNumber: String? = null
)

/**
 * 카테고리 정보 DTO
 */
@Serializable
data class CategoryDto(
    @SerialName("upperCategory")
    val upperCategory: String? = null,

    @SerialName("middleCategory")
    val middleCategory: String? = null,

    @SerialName("lowerCategory")
    val lowerCategory: String? = null
)
