package kr.co.ongil.presentation.uistate

/**
 * 연락처 정보
 */
data class ContactInfo(
    val initials: String,
    val name: String,
    val phone: String,
    val badge: String?,          // "VoIP", "일반전화" 등
    val isEmergency: Boolean     // 긴급 연락처 여부
)

/**
 * 통화 요약 정보
 */
data class CallSummary(
    val direction: String,       // "발신 통화" / "수신 통화"
    val duration: String,        // "5분 12초"
    val location: String?,       // "서울시 강남구 역삼동"
    val date: String,            // "2024년 10월 28일"
    val startTime: String,       // "오후 3:24:15"
    val endTime: String          // "오후 3:29:27"
)

/**
 * 채팅 메시지
 */
data class ChatMessage(
    val text: String,
    val time: String,
    val isMine: Boolean
)

/**
 * 통화 상세 정보 화면 UI 상태
 */
data class CallDetailUiState(
    val contactInfo: ContactInfo? = null,
    val callSummary: CallSummary? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 통화 상세 정보 화면 이벤트
 */
sealed interface CallDetailEvent {
    // 데이터 로드
    data class LoadCallDetail(val callLogId: Long) : CallDetailEvent
}
