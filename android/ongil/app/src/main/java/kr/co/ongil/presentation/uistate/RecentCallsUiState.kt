package kr.co.ongil.presentation.uistate

/**
 * 통화 타입
 */
enum class CallType {
    VOIP,    // VoIP 통화
    NORMAL,  // 일반 전화
    SOS      // 긴급 SOS 통화
}

/**
 * 최근 통화 목록 UI 모델
 */
data class RecentCallUi(
    val id: Long,
    val nameOrNumber: String,
    val type: CallType,
    val subtitle: String  // 예: "오늘 오후 3:24 · 통화시간 5분 12초"
)

/**
 * 최근 통화 목록 화면 UI 상태
 */
data class RecentCallsUiState(
    val calls: List<RecentCallUi> = emptyList(),
    val searchQuery: String = "",
    val filteredCalls: List<RecentCallUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 최근 통화 목록 화면 이벤트
 */
sealed interface RecentCallsEvent {
    // 검색
    data class UpdateSearchQuery(val query: String) : RecentCallsEvent

    // 액션
    data class OnInfoClick(val call: RecentCallUi) : RecentCallsEvent

    // 데이터 로드
    data object LoadCalls : RecentCallsEvent
}
