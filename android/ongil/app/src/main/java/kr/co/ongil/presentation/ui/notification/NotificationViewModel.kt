package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.model.error.ApiException
import kr.co.ongil.data.model.notification.NotificationDto
import kr.co.ongil.domain.repository.NotificationRepository
import kr.co.ongil.presentation.uistate.NotificationEvent
import kr.co.ongil.presentation.uistate.NotificationUi
import kr.co.ongil.presentation.uistate.NotificationUiState
import kr.co.ongil.presentation.uistate.NotificationType
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

/**
 * 알림 화면 ViewModel
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val userDataStoreManager: UserDataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // Debounce를 위한 Channel
    private val loadNotificationsChannel = Channel<Unit>(Channel.CONFLATED)

    init {
        loadNotifications()
        setupDebouncedLoad()
    }

    /**
     * Debounced load notifications (300ms 내 중복 호출 방지)
     */
    private fun setupDebouncedLoad() {
        viewModelScope.launch {
            loadNotificationsChannel.consumeAsFlow()
                .debounce(300)
                .collect {
                    actuallyLoadNotifications()
                }
        }
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

    /**
     * 알림 로드 (Channel을 통해 debounce 처리)
     */
    private fun loadNotifications() {
        viewModelScope.launch {
            loadNotificationsChannel.send(Unit)
        }
    }

    /**
     * 실제 알림 로드 로직
     */
    private fun actuallyLoadNotifications() {
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
                    handleError(exception)
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * 에러 처리 (인증 실패 시 재인증 플래그 설정)
     */
    private fun handleError(exception: Throwable) {
        android.util.Log.e("NotificationViewModel", "Error occurred", exception)

        when (exception) {
            is ApiException.Unauthorized, is ApiException.Forbidden -> {
                // 인증 실패 - 로그아웃 및 재인증 필요
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "세션이 만료되었습니다. 다시 로그인해주세요.",
                        requiresReauth = true
                    )
                }
                // 로그아웃 처리 (토큰 삭제)
                viewModelScope.launch {
                    userDataStoreManager.clearTokens()
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "알림을 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }

    private fun markAsRead(id: Long) {
        viewModelScope.launch {
            try {
                // 실제 API 호출
                val result = repository.markAsRead(id)

                result.onSuccess {
                    // API 호출 성공 시 서버에서 최신 데이터 다시 불러오기
                    loadNotifications()
                }.onFailure { exception ->
                    _uiState.update { it.copy(error = exception.message ?: "읽음 처리에 실패했습니다.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "읽음 처리에 실패했습니다.") }
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            try {
                // 실제 API 호출
                val result = repository.markAllAsRead()

                result.onSuccess {
                    // API 호출 성공 시 서버에서 최신 데이터 다시 불러오기
                    loadNotifications()
                }.onFailure { exception ->
                    _uiState.update { it.copy(error = exception.message ?: "전체 읽음 처리에 실패했습니다.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "전체 읽음 처리에 실패했습니다.") }
            }
        }
    }

    /**
     * 개별 알림 삭제 (낙관적 업데이트 패턴 적용)
     */
    private fun deleteNotification(id: Long) {
        viewModelScope.launch {
            // 1. 백업 (롤백용)
            val originalList = _uiState.value.notifications

            // 2. UI에서 먼저 제거 & 삭제 중 상태 추가
            _uiState.update {
                it.copy(
                    notifications = it.notifications.filter { n -> n.id != id },
                    deletingIds = it.deletingIds + id,
                    hasUnread = it.notifications.filter { n -> n.id != id }.any { n -> !n.isRead }
                )
            }

            try {
                // 3. API 호출
                val result = repository.deleteNotification(id)

                result.onSuccess {
                    // 성공 - 삭제 중 상태만 제거
                    _uiState.update {
                        it.copy(deletingIds = it.deletingIds - id)
                    }
                }.onFailure { exception ->
                    // 실패 - 롤백 및 에러 메시지 표시
                    _uiState.update {
                        it.copy(
                            notifications = originalList,
                            deletingIds = it.deletingIds - id,
                            error = "삭제에 실패했습니다: ${exception.message}",
                            hasUnread = originalList.any { n -> !n.isRead }
                        )
                    }
                }
            } catch (e: Exception) {
                // 예외 - 롤백 및 에러 메시지 표시
                _uiState.update {
                    it.copy(
                        notifications = originalList,
                        deletingIds = it.deletingIds - id,
                        error = "삭제에 실패했습니다.",
                        hasUnread = originalList.any { n -> !n.isRead }
                    )
                }
            }
        }
    }

    /**
     * 전체 알림 삭제 (낙관적 업데이트 패턴 적용)
     */
    private fun deleteAllNotifications() {
        viewModelScope.launch {
            // 1. 백업 (롤백용)
            val originalList = _uiState.value.notifications

            // 2. UI에서 먼저 모두 제거
            _uiState.update {
                it.copy(
                    notifications = emptyList(),
                    isLoading = true,
                    hasUnread = false
                )
            }

            try {
                // 3. API 호출
                val result = repository.deleteAllNotifications()

                result.onSuccess { deleteCount ->
                    android.util.Log.d("NotificationViewModel", "전체 알림 삭제 완료: ${deleteCount}개")
                    // 성공 - 로딩 상태만 해제
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                }.onFailure { exception ->
                    // 실패 - 롤백 및 에러 메시지 표시
                    _uiState.update {
                        it.copy(
                            notifications = originalList,
                            isLoading = false,
                            error = "전체 삭제에 실패했습니다: ${exception.message}",
                            hasUnread = originalList.any { n -> !n.isRead }
                        )
                    }
                }
            } catch (e: Exception) {
                // 예외 - 롤백 및 에러 메시지 표시
                _uiState.update {
                    it.copy(
                        notifications = originalList,
                        isLoading = false,
                        error = "전체 삭제에 실패했습니다.",
                        hasUnread = originalList.any { n -> !n.isRead }
                    )
                }
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
