package kr.co.ongil.presentation.ui.favorite

import kr.co.ongil.presentation.ui.favorite.PatientData
import kr.co.ongil.presentation.ui.favorite.PlaceData


data class FavoriteUiState(
    val selectedTab: FavoriteTab = FavoriteTab.PATIENTS,
    val patients: List<PatientData> = emptyList(),
    val places: List<PlaceData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// 탭 종류 (환자 목록 / 장소 목록)
enum class FavoriteTab {
    PATIENTS, PLACES
}