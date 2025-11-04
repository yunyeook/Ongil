package kr.co.ongil.data.model.favorite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDetailDto(
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: FavoritePlaceDto?,
    @SerialName("isDefault")
    val isDefault: Boolean? = null

)