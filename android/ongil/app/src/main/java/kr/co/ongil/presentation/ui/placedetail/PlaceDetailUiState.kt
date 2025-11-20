package kr.co.ongil.presentation.ui.placedetail

data class PlaceDetailUiState(
    val favoriteId: Long,
    val placeName: String,
    val address: String,
    val isDefault: Boolean,
    val patientId: Long,
    val userType: String = "GUARDIAN", // PATIENT, GUARDIAN 등
    val initialIsDefault: Boolean? = null, // API 로드 완료 후 초기값 저장
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)