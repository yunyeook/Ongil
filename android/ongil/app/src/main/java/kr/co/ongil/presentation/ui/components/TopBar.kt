package kr.co.ongil.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview  // 👈 Preview import 추가
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 공통 TopBar 컴포넌트
 * 두 가지 버전을 지원합니다:
 * 1. 뒤로가기 + 제목 + 알림 (showBackButton = true)
 * 2. 프로필 + 제목 + 토글 + 알림 (showProfile = true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    showBackButton: Boolean = false,
    showProfile: Boolean = false,
    showToggle: Boolean = false,
    showNotification: Boolean = true,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onToggleChange: (Boolean) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)  // 👈 높이 증가: 56dp → 64dp
                .padding(horizontal = 20.dp),  // 👈 여백 증가
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 영역
            when {
                // 버전 1: 뒤로가기 버튼
                showBackButton -> {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)  // 👈 아이콘 크기 증가
                        )
                    }
                }
                // 버전 2: 프로필 아이콘
                showProfile -> {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "프로필",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                // 아무것도 없으면 공간만 차지
                else -> {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // 중앙 제목
            Text(
                text = title,
                fontSize = 24.sp,  // 👈 크기 증가: 20sp → 24sp
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 12.dp)  // 👈 여백 증가
            )

            // 오른쪽으로 밀기
            Spacer(modifier = Modifier.weight(1f))

            // 오른쪽 영역
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 토글 스위치 (버전 2에만)
                if (showToggle) {
                    var checked by remember { mutableStateOf(false) }
                    Switch(
                        checked = checked,
                        onCheckedChange = {
                            checked = it
                            onToggleChange(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFFD54F),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE0E0E0)
                        )
                    )
                }

                // 알림 아이콘
                if (showNotification) {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "알림",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)  // 👈 아이콘 크기 증가
                        )
                    }
                }
            }
        }
    }
}

// ===== Preview =====

@Preview(showBackground = true, name = "내 정보 수정 헤더")
@Composable
fun TopBarPreview1() {
    kr.co.ongil.presentation.theme.OngilTheme {
        TopBar(
            title = "내 정보 수정",
            showBackButton = true,
            onBackClick = { },
            onNotificationClick = { }
        )
    }
}

@Preview(showBackground = true, name = "온길 메인 헤더")
@Composable
fun TopBarPreview2() {
    kr.co.ongil.presentation.theme.OngilTheme {
        TopBar(
            title = "온길",
            showProfile = true,
            showToggle = true,
            onProfileClick = { },
            onToggleChange = { },
            onNotificationClick = { }
        )
    }
}