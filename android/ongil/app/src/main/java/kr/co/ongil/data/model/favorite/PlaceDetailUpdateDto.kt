package kr.co.ongil.data.model.favorite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDetailUpdateDto(
    @SerialName("placeName") val placeName: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("isDefault") val isDefault: Boolean? = null,
    @SerialName("placeAlias") val placeAlias: String? = null,
    // 이 밑에꺼도 쓰는거임??
    @SerialName("category")   val category: String? = null,
    @SerialName("latitude")   val latitude: Double? = null,
    @SerialName("longitude")  val longitude: Double? = null,

)
