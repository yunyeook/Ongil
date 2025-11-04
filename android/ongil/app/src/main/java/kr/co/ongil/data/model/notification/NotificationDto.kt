package kr.co.ongil.data.model.notification

import kotlinx.serialization.Serializable

/**
 * 알림 DTO
 */
@Serializable
data class NotificationDto(
    val notificationId: Long,
    val title: String,
    val content: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: String
)
