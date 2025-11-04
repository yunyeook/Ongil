package kr.co.ongil.presentation.uistate

/**
 * 알림 타입
 */
enum class NotificationType {
    RELATIONSHIP_REGIST,  // 환자-보호자 관계 등록 (API)
    SAFEZONE_EXIT,        // 안전구역 이탈 (API)
    NAVIGATION_START,     // 길안내 시작 (API)
    SOS,                  // 이상 탐지 알림
    FRIEND_REQUEST,       // 친구 요청 알림
    FRIEND_DONE,          // 친구 등록 완료
    MISSED_CALL,          // 부재중 전화
    NAV_START,            // 길찾기 시작 (레거시)
    NAV_END,              // 길찾기 종료
    GEOFENCE_OUT,         // 범위 이탈 (레거시)
    UNKNOWN;              // 알 수 없는 타입

    companion object {
        /**
         * API 문자열을 NotificationType으로 변환
         */
        fun fromApiString(type: String): NotificationType {
            return when (type) {
                "RELATIONSHIP_REGIST" -> RELATIONSHIP_REGIST
                "SAFEZONE_EXIT" -> SAFEZONE_EXIT
                "NAVIGATION_START" -> NAVIGATION_START
                else -> UNKNOWN
            }
        }
    }
}

/**
 * 알림 아이템 UI 모델
 */
data class NotificationUi(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timeAgo: String,
    val isRead: Boolean = false
)

/**
 * 알림 화면 UI 상태
 */
data class NotificationUiState(
    val notifications: List<NotificationUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasUnread: Boolean = false
)

/**
 * 알림 화면 이벤트
 */
sealed interface NotificationEvent {
    // 개별 읽음 처리
    data class MarkAsRead(val id: Long) : NotificationEvent

    // 전체 읽음 처리
    data object MarkAllAsRead : NotificationEvent

    // 개별 삭제
    data class DeleteNotification(val id: Long) : NotificationEvent

    // 전체 삭제
    data object DeleteAllNotifications : NotificationEvent

    // 데이터 로드
    data object LoadNotifications : NotificationEvent

    // 알림 클릭
    data class OnNotificationClick(val notification: NotificationUi) : NotificationEvent
}
