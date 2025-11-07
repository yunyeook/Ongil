package kr.co.ongil.domain.repository

import kr.co.ongil.data.model.notification.NotificationDto
import kr.co.ongil.data.model.notification.PageInfo

/**
 * 알림 Repository 인터페이스
 */
interface NotificationRepository {

    /**
     * 알림 목록 조회
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지당 데이터 개수
     * @param read 읽음 여부 필터 (true: 읽은 알림, false: 안 읽은 알림, null: 전체)
     * @return Result<Pair<List<NotificationDto>, PageInfo>>
     */
    suspend fun getNotifications(
        page: Int = 1,
        size: Int = 10,
        read: Boolean? = null
    ): Result<Pair<List<NotificationDto>, PageInfo>>

    /**
     * 개별 알림 읽음 처리
     * @param notificationId 읽음 처리할 알림 ID
     */
    suspend fun markAsRead(notificationId: Long): Result<Unit>

    /**
     * 전체 읽음 처리
     */
    suspend fun markAllAsRead(): Result<Unit>

    /**
     * 개별 알림 삭제
     * @param notificationId 삭제할 알림 ID
     */
    suspend fun deleteNotification(notificationId: Long): Result<Unit>

    /**
     * 전체 알림 삭제
     * @return Result<Int> 삭제된 알림 개수
     */
    suspend fun deleteAllNotifications(): Result<Int>
}
