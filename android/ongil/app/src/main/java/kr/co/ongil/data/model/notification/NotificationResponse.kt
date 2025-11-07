package kr.co.ongil.data.model.notification

import kotlinx.serialization.Serializable

/**
 * 알림 목록 조회 API 응답
 * GET /api/v1/notifications?page={page}&size={size}&read={read}
 */
@Serializable
data class NotificationResponse(
    val message: String,
    val data: NotificationData
)

@Serializable
data class NotificationData(
    val notifications: List<NotificationDto>,
    val pageInfo: PageInfo
)

@Serializable
data class PageInfo(
    val totalElements: Int,
    val totalPages: Int,
    val isLast: Boolean,
    val currPage: Int
)
