package kr.co.ongil.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kr.co.ongil.R

// ---- 색상 토큰(필요 시 theme/Color.kt로 이관) ----
private val OngilAccent = Color(0xFF8CA898)
private val OngilGray = Color(0xFF364046)
private val OngilBeige = Color(0xFFF8EBD6)

// ---- 헤더 타입 ----
enum class OngilHeaderType { BackTitleBell, BrandCard }

/** 공통 컨테이너: 아래쪽 1px 가이드라인(디바이더) 옵션 */
@Composable
private fun HeaderContainer(
    containerColor: Color = Color.White,
    bottomDivider: Boolean = true,
    dividerColor: Color = Color(0xFFEAEAEA),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
    ) {
        content()
        if (bottomDivider) {
            HorizontalDivider(thickness = 1.dp, color = dividerColor)
        }
    }
}

/** [타입1] 뒤로가기 + 제목 + 알림 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngilTopBar(
    title: String,
    onBackClick: () -> Unit,
    onBellClick: () -> Unit = {},
    bottomDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    HeaderContainer(bottomDivider = bottomDivider) {
        TopAppBar(
            windowInsets = TopAppBarDefaults.windowInsets,
            modifier = modifier,
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = OngilGray
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = OngilGray
                )
            },
            actions = {
                IconButton(onClick = onBellClick) {
                    Icon(
                        Icons.Outlined.NotificationsNone,
                        contentDescription = "알림",
                        tint = OngilGray
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
    }
}

/** [타입2] 로고 + 프로필 + 알림 (카드형) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngilBrandHeaderCard(
    onBellClick: () -> Unit = {},
    profileImageUrl: String? = null,
    bottomDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    HeaderContainer(bottomDivider = bottomDivider) {
        TopAppBar(
            windowInsets = TopAppBarDefaults.windowInsets,
            modifier = modifier,
            navigationIcon = {
                Image(
                    painter = painterResource(id = R.drawable.ongillogo),
                    contentDescription = "온길 로고",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(start = 15.dp)
                )
            },
            title = {
                // 빈 타이틀
            },
            actions = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OngilBeige),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "프로필",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = onBellClick) {
                    Icon(
                        Icons.Outlined.NotificationsNone,
                        contentDescription = "알림",
                        tint = OngilGray
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
    }
}

/** 스위처: 타입만 바꿔서 공통으로 사용 */
@Composable
fun OngilHeader(
    type: OngilHeaderType,
    title: String = "",
    onBackClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    profileImageUrl: String? = null,
    modifier: Modifier = Modifier
) {
    when (type) {
        OngilHeaderType.BackTitleBell ->
            OngilTopBar(
                title = title,
                onBackClick = onBackClick,
                onBellClick = onBellClick,
                modifier = modifier
            )
        OngilHeaderType.BrandCard ->
            OngilBrandHeaderCard(
                onBellClick = onBellClick,
                profileImageUrl = profileImageUrl,
                modifier = modifier
            )
    }
}

/** route에 따라 제목을 자동으로 설정하는 TopBar */
@Composable
fun OngilTopBarForRoute(
    route: String,
    onBackClick: () -> Unit,
    onBellClick: () -> Unit = {}
) {
    // route에 따라 화면 제목 자동 설정
    val title = when {
        route.contains("edit_info") -> "내 정보 수정"
        route.contains("my_info") -> "내 정보"
        route.contains("call_history") -> "최근 통화목록"
        route.contains("recent_calls") -> "최근 통화목록"
        route.contains("call_detail") -> "통화 상세"
        route.contains("change_password") -> "비밀번호 변경"
        route.contains("search_user") -> "사용자 찾기"
        route.contains("register_user") -> "사용자 등록"
        route.contains("patient_detail") -> "환자 상세"
        route.contains("place_detail") -> "장소 상세"
        else -> ""
    }

    OngilTopBar(
        title = title,
        onBackClick = onBackClick,
        onBellClick = onBellClick
    )
}
