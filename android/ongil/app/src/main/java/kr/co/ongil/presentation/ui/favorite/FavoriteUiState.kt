package kr.co.ongil.presentation.ui.favorite


data class FavoriteUiState(
    val selectedTab: FavoriteTab = FavoriteTab.PATIENTS, // 현재 선택된 탭 (환자 / 장소)
    val patients: List<PatientItem> = emptyList(),        // 즐겨찾기 환자들
    val places: List<PlaceData> = emptyList(),            // 즐겨찾기 장소들 (UI용 가공 데이터)
    val isLoading: Boolean = false                        // 로딩 등 필요 시 사용
)

// 탭 종류 (환자 목록 / 장소 목록)
enum class FavoriteTab {
    PATIENTS, PLACES
}

// UI에서 쓸 환자 항목 형태 (화면 전용)
data class PatientItem(
    val id: Long,
    val displayName: String,   // "김철수 (남, 75세)" 등 UI에 그대로 보여줄 문자열
    val phoneNumber: String    // "010-1234-5678"
)