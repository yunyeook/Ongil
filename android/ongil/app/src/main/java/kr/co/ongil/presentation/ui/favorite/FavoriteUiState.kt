package kr.co.ongil.presentation.ui.favorite

// 즐겨찾기 화면이 현재 어떻게 보여야 하는지를 들고 있는 상태 모델
data class FavoriteUiState(
    val selectedTab: FavoriteTab = FavoriteTab.PATIENTS, // 현재 선택된 탭 (환자 / 장소)
    val patients: List<PatientItem> = emptyList(),        // 즐겨찾기 환자들
    val places: List<PlaceItem> = emptyList(),            // 즐겨찾기 장소들
    val isLoading: Boolean = false                       // 로딩 등 필요 시 사용
)

// 탭 종류 (환자 목록 / 장소 목록)
enum class FavoriteTab {
    PATIENTS, PLACES
}

// UI에서 쓸 환자 항목 형태 (화면 전용 DTO)
data class PatientItem(
    val id: Long,
    val displayName: String,   // "김철수 (남, 75세)" 처럼 가공된 표시용 문자열 가능
    val phoneNumber: String    // "010-1234-5678"
)

// UI에서 쓸 장소 항목 형태 (장소 탭용, 일단 뼈대만)
data class PlaceItem(
    val id: Long,
    val label: String,         // "집", "병원", "복지센터"
    val address: String        // "서울시 ..."
)