package kr.co.ongil.presentation.ui.myinfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kr.co.ongil.presentation.ui.myinfo.MyInfoUiState

/**
 * 온길 - 나의 정보 화면 (헤더/바텀바 제외)
 */
@Composable
fun MyInfoScreen(
    uiState: MyInfoUiState,
    onEditInfo: () -> Unit,
    onRecentCalls: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(0xFF8CA898) // 온길 포인트톤

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        // 상단: 프로필 요약
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아바타(원형)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                if (!uiState.profileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = uiState.profileImage,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    // 프로필 이미지가 없을 때 이니셜 표시
                    Text(
                        text = "∙",
                        style = MaterialTheme.typography.displaySmall,
                        color = accent,
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = uiState.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF243033)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = uiState.phoneNumber,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF6B767A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        HorizontalDivider(color = Color(0xFFEAEAEA))

        // 메뉴 목록
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            MenuItem(
                title = "내 정보 수정",
                leading = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color(0xFF2F3A3A)) },
                onClick = onEditInfo
            )
            MenuItem(
                title = "최근 통화목록",
                leading = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = Color(0xFF2F3A3A)) },
                onClick = onRecentCalls
            )
            MenuItem(
                title = "로그아웃",
                leading = { Icon(Icons.Outlined.PowerSettingsNew, contentDescription = null, tint = Color(0xFFD85B4E)) },
                onClick = onLogout,
                titleColor = Color(0xFFD85B4E)
            )
        }

        // 하단 여백
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MenuItem(
    title: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    titleColor: Color = Color(0xFF243033)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF959DA1)
        )
    }
    HorizontalDivider(color = Color(0xFFF2F2F2))
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewMyInfoScreen() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MyInfoScreen(
            uiState = MyInfoUiState(
                name = "홍길동",
                phoneNumber = "010-1234-5678",
                profileImage = null
            ),
            onEditInfo = {
                println("✅ 내 정보 수정 클릭됨")
            },
            onRecentCalls = {
                println("✅ 최근 통화목록 클릭됨")
            },
            onLogout = {
                println("✅ 로그아웃 클릭됨")
            }
        )
    }
}