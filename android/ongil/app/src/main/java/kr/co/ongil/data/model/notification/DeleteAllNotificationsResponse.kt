package kr.co.ongil.data.model.notification

import kotlinx.serialization.Serializable

/**
 * 전체 알림 삭제 API 응답
 * DELETE /api/v1/notifications
 */
@Serializable
data class DeleteAllNotificationsResponse(
    val message: String,
    val data: DeleteAllNotificationsData
)

@Serializable
data class DeleteAllNotificationsData(
    val deleteCount: Int
)
