package kr.co.ongil.data.model.favorite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 단일 장소 DTO
@Serializable
data class FavoritePlaceDto(
    @SerialName("favoriteId")
    val favoriteId: Long,
    @SerialName("patientId")
    val patientId: Long,
    @SerialName("placeName")
    val placeName: String,
    @SerialName("placeAlias")
    val placeAlias: String?,   // 사용자 지정 별칭
    @SerialName("category")
    val category: String?,     // 이거 null가능한거 아니면 수정해야됨
    @SerialName("address")
    val address: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("isDefault")
    val isDefault: Boolean,
    @SerialName("count")
    val count: Int,
    @SerialName("createdAt")
    val createdAt: String
)

// 목록 응답 DTO
@Serializable
data class FavoritePlacesDto(
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: FavoritePlacesDataDto?
)

@Serializable
data class FavoritePlacesDataDto(
    @SerialName("totalCount")
    val totalCount: Int,
    @SerialName("favorites")
    val favorites: List<FavoritePlaceDto>
)