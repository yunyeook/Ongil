package kr.co.ongil.data.model.notification

import kotlinx.serialization.Serializable

/**
 * 알림 DTO
 */
@Serializable
data class NotificationDto(
    val notificationId: Long,
    val senderId: Long,
    val receiverId: Long,
    val title: String,
    val content: String,
    val type: String,           // RELATIONSHIP_REGIST, SAFEZONE_EXIT, NAVIGATION_START, etc.
    val isRead: Boolean,
    val createdAt: String       // ISO8601 format
)
