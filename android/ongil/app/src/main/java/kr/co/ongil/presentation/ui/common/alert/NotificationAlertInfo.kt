package kr.co.ongil.presentation.ui.common.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PhoneMissed
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.uistate.NotificationType

/* ---------- 색상 팔레트 ---------- */
private val Gray900 = Color(0xFF212A30)
private val Gray600 = Color(0xFF6B767A)
private val Gray400 = Color(0xFF9CA3AF)
private val Accent = Color(0xFF8CA898)

// 배경 색상
private val BubbleSOS = Color(0xFFFFE8DE)
private val BubbleBlue = Color(0xFFE8F0FF)
private val BubbleGreen = Color(0xFFE8F1EC)
private val BubbleBeige = Color(0xFFF8EBD6)
private val BubbleGray = Color(0xFFEFF3EF)

// 아이콘 색상
private val IconSOS = Color(0xFFD35B41)
private val IconBlue = Color(0xFF4C77C6)
private val IconBeige = Color(0xFF9C7E52)

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
        NotificationType.SOS -> NotificationStyle(
            bubbleColor = BubbleSOS,
            iconColor = IconSOS,
            icon = Icons.Outlined.ErrorOutline
        )
        NotificationType.FRIEND_REQUEST -> NotificationStyle(
            bubbleColor = BubbleBlue,
            iconColor = IconBlue,
            icon = Icons.Outlined.PersonAdd
        )
        NotificationType.FRIEND_DONE -> NotificationStyle(
            bubbleColor = BubbleBlue,
            iconColor = IconBlue,
            icon = Icons.Outlined.Person
        )
        NotificationType.MISSED_CALL -> NotificationStyle(
            bubbleColor = BubbleGreen,
            iconColor = Accent,
            icon = Icons.Outlined.PhoneMissed
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
            icon = Icons.Outlined.Shield
        )
    }
}

/**
 * 알림 아이템 (AlertInfo 확장 버전)
 * - 타입별 아이콘/색상 지원
 * - 읽음/안읽음 상태 표시
 * - 스와이프 삭제 지원
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationAlertInfo(
    type: NotificationType,
    title: String,
    content: String,
    timeText: String,
    isRead: Boolean = false,
    onItemClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val style = getNotificationStyle(type)
    val backgroundColor = if (isRead) Color.White else Color(0xFFF9FAFB)

    SwipeToDismissBox(
        state = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                    onDelete()
                    true
                } else {
                    false
                }
            }
        ),
        backgroundContent = {
            // 스와이프 시 보이는 삭제 배경
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEF4444))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "삭제",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable { onItemClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 왼쪽 원형 아이콘
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(style.bubbleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            // 본문 내용
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Gray900,
                    fontSize = 16.sp,
                    fontWeight = if (isRead) FontWeight.Medium else FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = content,
                    color = Gray600,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = timeText,
                    color = Gray400,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // 읽지 않은 알림 표시 점
            if (!isRead) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Accent)
                        .align(Alignment.Top)
                )
            }
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NotificationAlertInfoPreview() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        NotificationAlertInfo(
            type = NotificationType.SOS,
            title = "이상 탐지 알림",
            content = "이상 상황이 감지되었습니다.\n환자의 위치를 확인해보세요.",
            timeText = "1시간 전",
            isRead = false
        )
        HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)

        NotificationAlertInfo(
            type = NotificationType.FRIEND_REQUEST,
            title = "친구 요청 알림",
            content = "김철수님이 친구 등록을 신청하셨습니다.",
            timeText = "2시간 전",
            isRead = false
        )
        HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)

        NotificationAlertInfo(
            type = NotificationType.MISSED_CALL,
            title = "부재중 전화 알림",
            content = "박민수님께 걸려온 부재중 전화가 있습니다.",
            timeText = "3시간 전",
            isRead = true
        )
        HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)

        NotificationAlertInfo(
            type = NotificationType.GEOFENCE_OUT,
            title = "범위 이탈",
            content = "정수진님이 2단계 안전구역에서 벗어났습니다.",
            timeText = "1일 전",
            isRead = true
        )
    }
}
