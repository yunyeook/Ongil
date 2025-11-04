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
     * @return Result<Pair<List<NotificationDto>, PageInfo>>
     */
    suspend fun getNotifications(page: Int = 1, size: Int = 20): Result<Pair<List<NotificationDto>, PageInfo>>
}
