package kr.co.ongil.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.R


/**
 * 공통 TopBar 컴포넌트
 *
 * 버전 1 (서브 페이지): 뒤로가기 + 제목(선택)
 * 버전 2 (메인): 로고 + 사용자 이미지 + 알림
 */
@Composable
fun TopBar(
    // 공통
    title: String? = null,  // null이면 제목 안 보임

    // 버전 1: 서브 페이지
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},

    // 버전 2: 메인 헤더
    showMainHeader: Boolean = false,
    logoResId: Int? = null,  // 로고 이미지 리소스 ID
    userImageResId: Int? = null,  // 사용자 이미지 리소스 ID
    onUserImageClick: () -> Unit = {},
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
                .height(64.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                // ===== 버전 1: 서브 페이지 =====
                showBackButton -> {
                    // 뒤로가기 버튼
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 제목 (있을 때만)
                    if (title != null) {
                        Text(
                            text = title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }

                // ===== 버전 2: 메인 헤더 =====
                showMainHeader -> {
                    // 로고 (로고에 글자 포함되어 있음)
                    if (logoResId != null) {
                        Image(
                            painter = painterResource(id = logoResId),
                            contentDescription = "온길 로고",
                            modifier = Modifier
                                .height(36.dp)  // 높이 고정, 너비는 비율 유지
                        )
                    }

                    // 오른쪽으로 밀기
                    Spacer(modifier = Modifier.weight(1f))

                    // 사용자 이미지 (있을 때만)
                    if (userImageResId != null) {
                        IconButton(
                            onClick = onUserImageClick,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = userImageResId),
                                contentDescription = "사용자 프로필",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // 알림 아이콘
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "알림",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // ===== 제목만 있는 경우 =====
                else -> {
                    if (title != null) {
                        Text(
                            text = title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

// ===== Preview =====

@Preview(showBackground = true, name = "1. 뒤로가기 + 제목")
@Composable
fun TopBarPreview1() {
    TopBar(
        title = "내 정보 수정",
        showBackButton = true,
        onBackClick = { }
    )
}

@Preview(showBackground = true, name = "2. 뒤로가기만 (제목 없음)")
@Composable
fun TopBarPreview2() {
    TopBar(
        showBackButton = true,
        onBackClick = { }
    )
}

@Preview(showBackground = true, name = "3. 메인 헤더 (로고만)")
@Composable
fun TopBarPreview3() {
    TopBar(
        showMainHeader = true,
         logoResId = R.drawable.logo_ongil,  // 실제 사용 시 주석 해제
        onNotificationClick = { }
    )
}

@Preview(showBackground = true, name = "4. 메인 헤더 (전체)")
@Composable
fun TopBarPreview4() {
    TopBar(
        showMainHeader = true,
        logoResId = R.drawable.logo_ongil,  // 실제 사용 시 주석 해제
         //userImageResId = R.drawable.user_profile,  // 실제 사용 시 주석 해제
        onUserImageClick = { },
        onNotificationClick = { }
    )
}