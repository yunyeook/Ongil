package kr.co.ongil.data.model.favorite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddFavoriteRequestDto(
    @SerialName("placeName")
    val placeName: String,
    @SerialName("placeAlias")
    val placeAlias: String? = null,
    @SerialName("category")
    val category: String?,
    @SerialName("address")
    val address: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("isDefault")
    val isDefault: Boolean = false
)