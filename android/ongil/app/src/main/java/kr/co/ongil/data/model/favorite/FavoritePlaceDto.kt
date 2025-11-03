package kr.co.ongil.data.model.favorite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// api명세서 기준으로 작성함
@Serializable
data class FavoritePlaceDto(
    @SerialName("favoriteId")
    val favoriteId: Long,
    @SerialName("patientId")
    val patientId: Long,
    @SerialName("placeName")
    val placeName: String,
    @SerialName("placeAlias")
    val placeAlias: String?,   // 사용자 수정 별칭
    @SerialName("address")
    val address: String,
    @SerialName("category")
    val category: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("count")
    val count: Int,
    @SerialName("isDefault")
    val isDefault: Boolean,
    @SerialName("createdAt")
    val createdAt: String
)

@Serializable
data class FavoritePlacesResponseDto(
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: List<FavoritePlaceDto>
)