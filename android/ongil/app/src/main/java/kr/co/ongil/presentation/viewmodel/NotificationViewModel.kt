package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.data.model.notification.NotificationDto
import kr.co.ongil.data.repository.FakeNotificationRepositoryImpl
import kr.co.ongil.domain.repository.NotificationRepository
import kr.co.ongil.presentation.uistate.NotificationEvent
import kr.co.ongil.presentation.uistate.NotificationUi
import kr.co.ongil.presentation.uistate.NotificationUiState
import kr.co.ongil.presentation.uistate.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 알림 화면 ViewModel
 */
class NotificationViewModel(
    private val repository: NotificationRepository = FakeNotificationRepositoryImpl()
    // TODO: DI로 주입하도록 변경
) : ViewModel() {

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
                val result = repository.getNotifications(page = 1, size = 20)

                result.onSuccess { (notificationDtos, pageInfo) ->
                    val notifications = notificationDtos.map { dto ->
                        dto.toNotificationUi()
                    }

                    _uiState.update {
                        it.copy(
                            notifications = notifications,
                            isLoading = false,
                            hasUnread = notifications.any { notification -> !notification.isRead }
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "알림을 불러오는데 실패했습니다."
                        )
                    }
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
     * NotificationDto를 NotificationUi로 변환
     */
    private fun NotificationDto.toNotificationUi(): NotificationUi {
        return NotificationUi(
            id = this.notificationId,
            type = NotificationType.fromApiString(this.type),
            title = this.title,
            body = this.content,
            timeAgo = calculateTimeAgo(this.createdAt),
            isRead = this.isRead
        )
    }

    /**
     * 시간 차이를 "n분 전", "n시간 전" 형식으로 변환
     */
    // NotificationViewModel.kt
    private fun calculateTimeAgo(createdAt: String): String = try {
        val created = parseIso8601Lenient(createdAt) ?: return createdAt
        val now = Date()
        val diff = now.time - created.time
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)
        when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours   < 24 -> "${hours}시간 전"
            days    < 7  -> "${days}일 전"
            days    < 30 -> "${days / 7}주 전"
            else         -> "${days / 30}개월 전"
        }
    } catch (e: Exception) { createdAt }

    private fun parseIso8601Lenient(s: String): Date? {
        val utc = TimeZone.getTimeZone("UTC")
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX"
        )
        for (p in patterns) {
            try {
                val sdf = java.text.SimpleDateFormat(p, java.util.Locale.getDefault())
                if (!p.contains('X')) sdf.timeZone = utc
                return sdf.parse(s)
            } catch (_: java.text.ParseException) {}
        }
        return null
    }

}
