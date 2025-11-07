package kr.co.ongil.presentation.ui.favorite

import kr.co.ongil.domain.model.FavoritePlace


data class FavoriteUiState(
    val selectedTab: FavoriteTab = FavoriteTab.PATIENTS,
    val patients: List<PatientData> = emptyList(),
    val places: List<FavoritePlace> = emptyList(),
    val currentPatientId: Long = 0L, // 현재 조회 중인 환자 ID
    val isLoading: Boolean = false,
    val error: String? = null,
    val userName: String = "",
    val userType: String = "" // PATIENT, GUARDIAN 등
)

// 탭 종류 (환자 목록 / 장소 목록)
enum class FavoriteTab {
    PATIENTS, PLACES
}