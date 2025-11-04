package kr.co.ongil.presentation.ui.common.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * 공통 알림 아이템 컴포넌트
 *
 * @param title 알림 제목
 * @param content 알림 내용
 * @param timeText 시간 텍스트 (예: "1시간 전")
 * @param modifier Modifier
 * @param icon 왼쪽 아이콘 (null이면 기본 원형)
 * @param iconColor 아이콘 색상
 * @param iconBackgroundColor 아이콘 배경 색상
 * @param isRead 읽음 여부 (읽지 않으면 배경색 다르고 오른쪽에 점 표시)
 * @param showUnreadDot 안 읽은 알림 점 표시 여부
 * @param onItemClick 아이템 클릭 시 콜백
 * @param enableSwipeToDelete 스와이프 삭제 활성화 여부
 * @param onDelete 삭제 콜백
 * @param showActionMenu 액션 메뉴(더보기) 표시 여부
 * @param onMarkAsRead 읽음 처리 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertInfo(
    title: String,
    content: String,
    timeText: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconColor: Color = Color.White,
    iconBackgroundColor: Color = Color.Gray,
    isRead: Boolean = true,
    showUnreadDot: Boolean = true,
    onItemClick: () -> Unit = {},
    enableSwipeToDelete: Boolean = false,
    onDelete: () -> Unit = {},
    showActionMenu: Boolean = false,
    onMarkAsRead: () -> Unit = {}
) {
    val backgroundColor = if (isRead) Color.White else Color(0xFFF9FAFB)
    var showMenu by remember { mutableStateOf(false) }

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable(enabled = onItemClick != {}) { onItemClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 왼쪽 원형 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center

            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            // 본문 내용
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF212A30),
                    fontSize = 16.sp,
                    fontWeight = if (isRead) FontWeight.Medium else FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = content,
                    color = Color(0xFF6B767A),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = timeText,
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // 액션 메뉴 또는 읽지 않은 알림 표시 점
            if (showActionMenu) {
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "더보기",
                            tint = Color(0xFF6B767A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // 안 읽은 알림만 읽음 처리 메뉴 표시
                        if (!isRead) {
                            DropdownMenuItem(
                                text = { Text("읽음 처리") },
                                onClick = {
                                    onMarkAsRead()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.DoneAll,
                                        contentDescription = null,
                                        tint = Color(0xFF8CA898)
                                    )
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("삭제") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        )
                    }
                }
            } else if (!isRead && showUnreadDot) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8CA898))
                        .align(Alignment.Top)
                )
            }
        }
    }

    if (enableSwipeToDelete) {
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
            content()
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "기본 알림")
@Composable
private fun AlertInfoPreview() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        AlertInfo(
            title = "안전구역 이탈 감지",
            content = "보호 대상자가 지정된 안전 구역에서 벗어났습니다.",
            timeText = "5분 전"
        )
        HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)

        AlertInfo(
            title = "새로운 소식",
            content = "새로운 기능이 추가되었습니다. 확인해보세요.",
            timeText = "1일 전"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "읽음/안읽음 상태")
@Composable
private fun AlertInfoReadStatePreview() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        AlertInfo(
            title = "안 읽은 알림",
            content = "이 알림은 아직 읽지 않았습니다.",
            timeText = "1시간 전",
            isRead = false,
            iconBackgroundColor = Color(0xFFFFE8DE),
            iconColor = Color(0xFFD35B41)
        )
        HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)

        AlertInfo(
            title = "읽은 알림",
            content = "이 알림은 이미 읽었습니다.",
            timeText = "3시간 전",
            isRead = true,
            iconBackgroundColor = Color(0xFFE8F1EC),
            iconColor = Color(0xFF8CA898)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "액션 메뉴")
@Composable
private fun AlertInfoWithMenuPreview() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        AlertInfo(
            title = "안 읽은 알림 (메뉴 포함)",
            content = "오른쪽 점 3개 버튼을 눌러 읽음 처리 또는 삭제할 수 있습니다.",
            timeText = "1시간 전",
            isRead = false,
            iconBackgroundColor = Color(0xFFFFE8DE),
            iconColor = Color(0xFFD35B41),
            showActionMenu = true,
            onMarkAsRead = { /* 읽음 처리 */ },
            onDelete = { /* 삭제 */ }
        )
        HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)

        AlertInfo(
            title = "읽은 알림 (메뉴 포함)",
            content = "이미 읽은 알림은 삭제만 가능합니다.",
            timeText = "3시간 전",
            isRead = true,
            iconBackgroundColor = Color(0xFFE8F1EC),
            iconColor = Color(0xFF8CA898),
            showActionMenu = true,
            onDelete = { /* 삭제 */ }
        )
    }
}