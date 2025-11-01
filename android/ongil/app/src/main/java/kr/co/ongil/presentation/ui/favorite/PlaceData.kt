package kr.co.ongil.presentation.ui.favorite


data class PlaceData(
    val patientId: Long,
    val favoriteId: Long,
    val placeName: String,  // placeAlias가 있으면 그걸, 없으면 placeName
    val address: String,
    val isDefault: Boolean
)