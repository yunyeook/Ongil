package kr.co.ongil.presentation.ui.favorite

import kr.co.ongil.data.model.favorite.FavoritePlaceDto

import kotlinx.serialization.Serializable
@Serializable
data class PlaceData(
    val patientId: Long,
    val favoriteId: Long,
    val placeName: String,  // placeAlias가 있으면 그걸, 없으면 placeName
    val address: String,
    val isDefault: Boolean
)

fun FavoritePlaceDto.toPlaceData(): PlaceData {
    return PlaceData(
        patientId = patientId,
        favoriteId = favoriteId,
        placeName = placeAlias ?: placeName,
        address = address,
        isDefault = isDefault
    )
}