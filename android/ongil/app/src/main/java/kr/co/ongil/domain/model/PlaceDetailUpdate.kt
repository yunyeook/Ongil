package kr.co.ongil.domain.model.placedetail

data class PlaceDetailUpdate(
    val placeName: String? = null,
    val address: String? = null,
    val placeAlias: String? = null,
    val category: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isDefault: Boolean? = null
)