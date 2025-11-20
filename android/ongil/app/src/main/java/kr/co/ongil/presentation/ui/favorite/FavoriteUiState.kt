package kr.co.ongil.presentation.ui.favorite

import kr.co.ongil.domain.model.FavoritePlace


data class FavoriteUiState(
    val selectedTab: FavoriteTab = FavoriteTab.PATIENTS,
    val patients: List<PatientData> = emptyList(),
    val places: List<FavoritePlace> = emptyList(),
    val currentPatientId: Long = 0L, // 현재 조회 중인 환자 ID
    val isLoading: Boolean = true, // 초기 로딩 상태
    val error: String? = null,
    val userName: String = "",
    val userType: String = "GUARDIAN" // 기본값: GUARDIAN (테마 깜빡임 방지)
)

// 탭 종류 (환자 목록 / 장소 목록)
enum class FavoriteTab {
    PATIENTS, PLACES
}