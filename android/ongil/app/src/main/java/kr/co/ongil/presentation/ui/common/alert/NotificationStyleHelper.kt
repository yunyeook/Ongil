package kr.co.ongil.presentation.ui.common.alert

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PhoneMissed
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kr.co.ongil.presentation.uistate.NotificationType

/* ---------- 색상 팔레트 ---------- */
private val Gray900 = Color(0xFF212A30)

// 배경 색상
private val BubbleSOS = Color(0xFFFFD6D6)
private val BubbleBlue = Color(0xFFE8F0FF)
private val BubbleGreen = Color(0xFFE8F1EC)
private val BubbleBeige = Color(0xFFF8EBD6)
private val BubbleGray = Color(0xFFEFF3EF)

// 아이콘 색상
private val IconSOS = Color(0xFFE53E3E)
private val IconBlue = Color(0xFF4C77C6)
private val IconBeige = Color(0xFF9C7E52)
private val Accent = Color(0xFF8CA898)

/**
 * 알림 타입별 스타일 데이터
 */
data class NotificationStyle(
    val bubbleColor: Color,
    val iconColor: Color,
    val icon: ImageVector
)

/**
 * 알림 타입에 따른 스타일 반환
 */
fun getNotificationStyle(type: NotificationType): NotificationStyle {
    return when (type) {
        // API 타입
        NotificationType.RELATIONSHIP_REGIST -> NotificationStyle(
            bubbleColor = BubbleBlue,
            iconColor = IconBlue,
            icon = Icons.Outlined.Person
        )
        NotificationType.SAFEZONE_EXIT -> NotificationStyle(
            bubbleColor = BubbleGray,
            iconColor = Gray900,
            icon = Icons.Filled.RadioButtonChecked
        )
        NotificationType.NAVIGATION_START -> NotificationStyle(
            bubbleColor = BubbleBeige,
            iconColor = IconBeige,
            icon = Icons.Outlined.Place
        )

        // 레거시 타입
        NotificationType.SOS -> NotificationStyle(
            bubbleColor = BubbleSOS,
            iconColor = IconSOS,
            icon = Icons.Filled.Warning
        )
        NotificationType.FRIEND_REQUEST -> NotificationStyle(
            bubbleColor = BubbleBlue,
            iconColor = IconBlue,
            icon = Icons.Outlined.Person
        )
        NotificationType.FRIEND_DONE -> NotificationStyle(
            bubbleColor = BubbleBlue,
            iconColor = IconBlue,
            icon = Icons.Outlined.Person
        )
        NotificationType.MISSED_CALL -> NotificationStyle(
            bubbleColor = BubbleGreen,
            iconColor = Accent,
            icon = Icons.Filled.Phone
        )
        NotificationType.NAV_START -> NotificationStyle(
            bubbleColor = BubbleBeige,
            iconColor = IconBeige,
            icon = Icons.Outlined.Place
        )
        NotificationType.NAV_END -> NotificationStyle(
            bubbleColor = BubbleBeige,
            iconColor = IconBeige,
            icon = Icons.Outlined.Place
        )
        NotificationType.GEOFENCE_OUT -> NotificationStyle(
            bubbleColor = BubbleGray,
            iconColor = Gray900,
            icon = Icons.Filled.RadioButtonChecked
        )

        // 알 수 없는 타입
        NotificationType.UNKNOWN -> NotificationStyle(
            bubbleColor = BubbleGray,
            iconColor = Gray900,
            icon = Icons.Outlined.ErrorOutline
        )
    }
}
