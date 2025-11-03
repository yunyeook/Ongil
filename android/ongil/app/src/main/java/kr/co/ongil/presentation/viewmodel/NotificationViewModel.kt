package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.presentation.uistate.NotificationEvent
import kr.co.ongil.presentation.uistate.NotificationUi
import kr.co.ongil.presentation.uistate.NotificationUiState
import kr.co.ongil.presentation.uistate.NotificationType

/**
 * 알림 화면 ViewModel
 */
class NotificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun onEvent(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.MarkAsRead -> markAsRead(event.id)
            is NotificationEvent.MarkAllAsRead -> markAllAsRead()
            is NotificationEvent.DeleteNotification -> deleteNotification(event.id)
            is NotificationEvent.DeleteAllNotifications -> deleteAllNotifications()
            is NotificationEvent.LoadNotifications -> loadNotifications()
            is NotificationEvent.OnNotificationClick -> onNotificationClick(event.notification)
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // TODO: 실제 API 호출로 대체
                val notifications = getDummyNotifications()

                _uiState.update {
                    it.copy(
                        notifications = notifications,
                        isLoading = false,
                        hasUnread = notifications.any { notification -> !notification.isRead }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "알림을 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }

    private fun markAsRead(id: Long) {
        viewModelScope.launch {
            try {
                // TODO: 실제 API 호출로 대체
                _uiState.update { state ->
                    val updated = state.notifications.map { notification ->
                        if (notification.id == id) notification.copy(isRead = true)
                        else notification
                    }
                    state.copy(
                        notifications = updated,
                        hasUnread = updated.any { !it.isRead }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "읽음 처리에 실패했습니다.") }
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            try {
                // TODO: 실제 API 호출로 대체
                _uiState.update { state ->
                    state.copy(
                        notifications = state.notifications.map { it.copy(isRead = true) },
                        hasUnread = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "전체 읽음 처리에 실패했습니다.") }
            }
        }
    }

    private fun deleteNotification(id: Long) {
        viewModelScope.launch {
            try {
                // TODO: 실제 API 호출로 대체
                _uiState.update { state ->
                    val updated = state.notifications.filter { it.id != id }
                    state.copy(
                        notifications = updated,
                        hasUnread = updated.any { !it.isRead }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "삭제에 실패했습니다.") }
            }
        }
    }

    private fun deleteAllNotifications() {
        viewModelScope.launch {
            try {
                // TODO: 실제 API 호출로 대체
                _uiState.update {
                    it.copy(
                        notifications = emptyList(),
                        hasUnread = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "전체 삭제에 실패했습니다.") }
            }
        }
    }

    private fun onNotificationClick(notification: NotificationUi) {
        // 클릭 시 읽음 처리
        if (!notification.isRead) {
            markAsRead(notification.id)
        }
        // TODO: 알림 타입별 네비게이션 처리
    }

    /**
     * 더미 데이터 (개발용)
     */
    private fun getDummyNotifications(): List<NotificationUi> {
        return listOf(
            NotificationUi(
                id = 1,
                type = NotificationType.SOS,
                title = "이상 탐지 알림",
                body = "이상 상황이 감지되었습니다.\n환자의 위치를 확인해보세요.",
                timeAgo = "1시간 전",
                isRead = false
            ),
            NotificationUi(
                id = 2,
                type = NotificationType.FRIEND_REQUEST,
                title = "친구 요청 알림",
                body = "김철수님이 친구 등록을 신청하셨습니다.\n카카오톡에서 인증번호를 확인해주세요.",
                timeAgo = "1시간 전",
                isRead = false
            ),
            NotificationUi(
                id = 3,
                type = NotificationType.FRIEND_DONE,
                title = "친구 등록 완료",
                body = "이영희님과 친구 등록이 완료되었습니다.",
                timeAgo = "2시간 전",
                isRead = true
            ),
            NotificationUi(
                id = 4,
                type = NotificationType.MISSED_CALL,
                title = "부재중 전화 알림",
                body = "박민수님께 걸려온 부재중 전화가 있습니다.",
                timeAgo = "3시간 전",
                isRead = true
            ),
            NotificationUi(
                id = 5,
                type = NotificationType.NAV_START,
                title = "길찾기 시작",
                body = "최지현님이 서울역으로 길찾기를 시작하셨습니다.",
                timeAgo = "5시간 전",
                isRead = true
            ),
            NotificationUi(
                id = 6,
                type = NotificationType.NAV_END,
                title = "길찾기 종료",
                body = "최지현님이 서울역으로 길찾기를 완료했습니다.",
                timeAgo = "5시간 전",
                isRead = true
            ),
            NotificationUi(
                id = 7,
                type = NotificationType.GEOFENCE_OUT,
                title = "범위 이탈",
                body = "정수진님이 2단계 안전구역에서 벗어났습니다.",
                timeAgo = "1일 전",
                isRead = true
            )
        )
    }
}
