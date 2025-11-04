package kr.co.ongil.presentation.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.co.ongil.presentation.ui.common.OngilTopBar
import kr.co.ongil.presentation.ui.common.alert.AlertInfo
import kr.co.ongil.presentation.ui.common.alert.getNotificationStyle
import kr.co.ongil.presentation.uistate.NotificationEvent
import kr.co.ongil.presentation.uistate.NotificationUi
import kr.co.ongil.presentation.uistate.NotificationUiState
import kr.co.ongil.presentation.uistate.NotificationType
import kr.co.ongil.presentation.viewmodel.NotificationViewModel

/**
 * 알림 화면 (ViewModel 기반)
 */
@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    NotificationContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

/**
 * 알림 화면 컨텐츠 (Stateless)
 */
@Composable
private fun NotificationContent(
    uiState: NotificationUiState,
    onEvent: (NotificationEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showMarkAllReadDialog by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color.White,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TopBar with custom actions (점 3개 메뉴)
            OngilTopBar(
                title = "알림",
                onBackClick = onNavigateBack,
                customActions = {
                    if (uiState.notifications.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showDropdownMenu = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = "더보기",
                                    tint = Color(0xFF364046)
                                )
                            }

                            DropdownMenu(
                                expanded = showDropdownMenu,
                                onDismissRequest = { showDropdownMenu = false }
                            ) {
                                // 전체 읽음 (읽지 않은 알림이 있을 때만 표시)
                                if (uiState.hasUnread) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.DoneAll,
                                                    contentDescription = null,
                                                    tint = Color(0xFF8CA898),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    "전체 읽음",
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        },
                                        onClick = {
                                            showDropdownMenu = false
                                            showMarkAllReadDialog = true
                                        }
                                    )
                                }

                                // 전체 삭제
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                "전체 삭제",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    },
                                    onClick = {
                                        showDropdownMenu = false
                                        showDeleteAllDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )

            // 알림 목록
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF8CA898))
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                uiState.notifications.isEmpty() -> {
                    EmptyNotificationView()
                }

                else -> {
                    NotificationList(
                        notifications = uiState.notifications,
                        onEvent = onEvent
                    )
                }
            }
        }
    }

    // 전체 읽음 처리 확인 다이얼로그
    if (showMarkAllReadDialog) {
        ConfirmDialog(
            title = "전체 읽음 처리",
            message = "모든 알림을 읽음 처리하시겠습니까?",
            onConfirm = {
                onEvent(NotificationEvent.MarkAllAsRead)
                showMarkAllReadDialog = false
            },
            onDismiss = { showMarkAllReadDialog = false }
        )
    }

    // 전체 삭제 확인 다이얼로그
    if (showDeleteAllDialog) {
        ConfirmDialog(
            title = "전체 삭제",
            message = "모든 알림을 삭제하시겠습니까?\n삭제된 알림은 복구할 수 없습니다.",
            onConfirm = {
                onEvent(NotificationEvent.DeleteAllNotifications)
                showDeleteAllDialog = false
            },
            onDismiss = { showDeleteAllDialog = false }
        )
    }
}

/**
 * 알림 목록
 */
@Composable
private fun NotificationList(
    notifications: List<NotificationUi>,
    onEvent: (NotificationEvent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(notifications, key = { it.id }) { notification ->
            val style = getNotificationStyle(notification.type)

            AlertInfo(
                title = notification.title,
                content = notification.body,
                timeText = notification.timeAgo,
                icon = style.icon,
                iconColor = style.iconColor,
                iconBackgroundColor = style.bubbleColor,
                isRead = notification.isRead,
                showActionMenu = false,
                enableSwipeToDelete = true,
                onItemClick = {
                    onEvent(NotificationEvent.OnNotificationClick(notification))
                },
                onMarkAsRead = {
                    onEvent(NotificationEvent.MarkAsRead(notification.id))
                },
                onDelete = {
                    onEvent(NotificationEvent.DeleteNotification(notification.id))
                }
            )
            HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)
        }
    }
}

/**
 * 빈 알림 화면
 */
@Composable
private fun EmptyNotificationView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📭",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "알림이 없습니다",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF6B767A)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "새로운 알림이 오면 여기에 표시됩니다",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

/**
 * 확인 다이얼로그
 */
@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF8CA898)
                )
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/* ---------- Preview ---------- */
@Preview(showBackground = true)
@Composable
private fun PreviewNotificationScreen() {
    val sampleNotifications = listOf(
        NotificationUi(
            id = 1,
            type = NotificationType.SOS,
            title = "이상 탐지 알림",
            body = "이상 상황이 감지되었습니다.\n환자의 위치를 확인해보세요.",
            timeAgo = "1시간 전",
            isRead = false
        ),
        NotificationUi(
            id = 2,
            type = NotificationType.FRIEND_REQUEST,
            title = "친구 요청 알림",
            body = "김철수님이 친구 등록을 신청하셨습니다.\n카카오톡에서 인증번호를 확인해주세요.",
            timeAgo = "1시간 전",
            isRead = false
        ),
        NotificationUi(
            id = 3,
            type = NotificationType.FRIEND_DONE,
            title = "친구 등록 완료",
            body = "이영희님과 친구 등록이 완료되었습니다.",
            timeAgo = "2시간 전",
            isRead = true
        ),
        NotificationUi(
            id = 4,
            type = NotificationType.MISSED_CALL,
            title = "부재중 전화 알림",
            body = "박민수님께 걸려온 부재중 전화가 있습니다.",
            timeAgo = "3시간 전",
            isRead = true
        ),
        NotificationUi(
            id = 5,
            type = NotificationType.GEOFENCE_OUT,
            title = "범위 이탈",
            body = "정수진님이 2단계 안전구역에서 벗어났습니다.",
            timeAgo = "1일 전",
            isRead = true
        )
    )

    NotificationContent(
        uiState = NotificationUiState(
            notifications = sampleNotifications,
            hasUnread = true
        ),
        onEvent = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, name = "빈 화면")
@Composable
private fun PreviewEmptyNotificationScreen() {
    NotificationContent(
        uiState = NotificationUiState(notifications = emptyList()),
        onEvent = {},
        onNavigateBack = {}
    )
}
