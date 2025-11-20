package kr.co.ongil.presentation.ui.myinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kr.co.ongil.presentation.ui.myinfo.MyInfoUiState

/**
 * 온길 - 나의 정보 화면 (헤더/바텀바 제외)
 * 위: 프로필 헤더 (프로필/닉네임 느낌)
 * 아래: 설정/메뉴 카드
 */
@Composable
fun MyInfoScreen(
    uiState: MyInfoUiState,
    onEditInfo: () -> Unit,
    onRecentCalls: () -> Unit,
    onVoipCallTest: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(0xFF8CA898) // 온길 포인트톤
    val primaryText = Color(0xFF243033)
    val secondaryText = Color(0xFF6B767A)
    val background = Color(0xFFF3F5F7)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer((Modifier.height(36.dp)))
                // 🔹 프로필 헤더 (카톡 프로필 비슷한 느낌)
                ProfileHeader(
                    uiState = uiState,
                    accent = accent,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onEditInfo = onEditInfo
                )

                Spacer(Modifier.height(24.dp))


                // 🔹 메뉴 카드 (이 부분은 거의 유지)
                MenuCard(
                    onEditInfo = onEditInfo,
                    onRecentCalls = onRecentCalls,
                    onLogout = onLogout
                )

                Spacer(Modifier.height(40.dp))

                Text(
                    text = "OnGil · 보호자와 환자를 위한 안전한 연결",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    uiState: MyInfoUiState,
    accent: Color,
    primaryText: Color,
    secondaryText: Color,
    onEditInfo: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 아바타 + 테두리 + 편집 아이콘 (카톡 프로필 느낌)
        Box(
            contentAlignment = Alignment.Center
        ) {
            // 바깥 원(테두리 느낌)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        clip = false
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 실제 프로필 영역
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F3F1)),
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
                        Text(
                            text = uiState.name
                                .takeIf { it.isNotBlank() }
                                ?.firstOrNull()
                                ?.toString()
                                ?: "∙",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = accent
                        )
                    }
                }
            }

            // 편집 아이콘 (오른쪽 아래 동그라미)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .clickable { onEditInfo() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "프로필 편집",
                    tint = Color.White,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 이름
        Text(
            text = uiState.name.ifBlank { "온길 사용자" },
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        // 온길 계정 배지 (나이 대신 작은 뱃지 느낌)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFEFF4F1))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "온길 계정",
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
        }

        // 전화번호는 여기선 안 보이게 깔끔하게 숨겨놓음
        // 필요하면 아주 작은 서브텍스트로 한 줄 넣어도 됨
    }
}

@Composable
private fun MenuCard(
    onEditInfo: () -> Unit,
    onRecentCalls: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            MenuItem(
                title = "내 정보 수정",
                description = "이름, 연락처 등 기본 정보 변경",
                leading = {
                    IconWithCircleBackground(
                        icon = Icons.Outlined.Edit,
                        iconTint = Color(0xFF2F3A3A)
                    )
                },
                onClick = onEditInfo
            )
            MenuItem(
                title = "최근 통화목록",
                description = "최근 보호자·환자 간 통화 기록 조회",
                leading = {
                    IconWithCircleBackground(
                        icon = Icons.Outlined.Phone,
                        iconTint = Color(0xFF2F3A3A)
                    )
                },
                onClick = onRecentCalls
            )
            MenuItem(
                title = "로그아웃",
                description = "현재 계정에서 로그아웃",
                leading = {
                    IconWithCircleBackground(
                        icon = Icons.Outlined.PowerSettingsNew,
                        iconTint = Color(0xFFD85B4E),
                        background = Color(0xFFFFF1EE)
                    )
                },
                onClick = onLogout,
                titleColor = Color(0xFFD85B4E),
                descriptionColor = Color(0xFFEB8A7D),
                showDivider = false
            )
        }
    }
}

@Composable
private fun IconWithCircleBackground(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    background: Color = Color(0xFFF1F3F5)
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint
        )
    }
}

@Composable
private fun MenuItem(
    title: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    titleColor: Color = Color(0xFF243033),
    description: String? = null,
    descriptionColor: Color = Color(0xFF8C969A),
    showDivider: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = titleColor
                )
                if (!description.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = descriptionColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFB3BCC0)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = Color(0xFFF2F2F2))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F5F7)
@Composable
private fun PreviewMyInfoScreen() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MyInfoScreen(
            uiState = MyInfoUiState(
                name = "오노고오",
                phoneNumber = "010-1234-5678",
                profileImage = null
            ),
            onEditInfo = { },
            onRecentCalls = { },
            onLogout = { }
        )
    }
}
