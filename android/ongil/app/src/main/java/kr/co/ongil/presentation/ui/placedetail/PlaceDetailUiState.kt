package kr.co.ongil.presentation.ui.placedetail

data class PlaceDetailUiState(
    val favoriteId: Long,
    val placeName: String,
    val address: String,
    val isDefault: Boolean,
    val patientId: Long
)