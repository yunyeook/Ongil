package kr.co.ongil.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import kr.co.ongil.R
import kr.co.ongil.presentation.theme.*
import kr.co.ongil.presentation.ui.common.selection.PatientInfoUi
import kr.co.ongil.presentation.ui.common.selection.SelectPatientModal

// ---- 헤더 타입 ----
enum class OngilHeaderType { BackTitleBell, BrandCard }

/** 공통 컨테이너: 위/아래쪽 1px 가이드라인(디바이더) 옵션 */
@Composable
private fun HeaderContainer(
    containerColor: Color = Color.White,
    topDivider: Boolean = false,
    bottomDivider: Boolean = true,
    dividerColor: Color = Color(0xFFEAEAEA),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
    ) {
//        if (topDivider) {
//            HorizontalDivider(thickness = 1.dp, color = dividerColor)
//        }
        content()
//        if (bottomDivider) {
//            HorizontalDivider(thickness = 1.dp, color = dividerColor)
//        }
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
    modifier: Modifier = Modifier,
    customActions: (@Composable RowScope.() -> Unit)? = null
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
                if (customActions != null) {
                    customActions()
                } else {
                    IconButton(onClick = onBellClick) {
                        Icon(
                            Icons.Outlined.NotificationsNone,
                            contentDescription = "알림",
                            tint = OngilGray
                        )
                    }
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
    profileName: String? = null,  // 프로필 이미지 옆에 표시할 이름
    patients: List<PatientInfoUi> = emptyList(),
    onSelectPatient: (PatientInfoUi) -> Unit = {},
    userType: String = "",  // "GUARDIAN" 또는 "PATIENT"
    selectedPatientId: String? = null,  // 선택된 환자 ID
    bottomDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showPatientModal by remember { mutableStateOf(false) }
    val isGuardian = userType.uppercase() == "GUARDIAN"

    // 사용자 타입에 따른 색상 선택
    val accentColor = if (userType.uppercase() == "PATIENT") OngilAccentPatient else OngilAccent
    val beigeColor = if (userType.uppercase() == "PATIENT") OngilBeigePatient else OngilBeige

    Box {
        HeaderContainer(topDivider = true, bottomDivider = bottomDivider) {
            TopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets,
                modifier = modifier,
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.logosymbol),
                        contentDescription = "온길 로고",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(start = 15.dp)
                    )
                },
                title = {
                    // 빈 타이틀
                },
                actions = {
                    // 보호자일 때만 프로필 표시
                    if (isGuardian) {
                        Row(
                            modifier = Modifier.clickable { showPatientModal = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(beigeColor),
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
                                } else {
                                    // 프로필 이미지가 없을 때 기본 아이콘 표시
//                                    Icon(
//                                        imageVector = Icons.Outlined.Person,
//                                        contentDescription = "프로필",
//                                        tint = accentColor,
//                                        modifier = Modifier.size(20.dp)
//                                    )
                                }
                            }

                            // 이름 표시
                            if (!profileName.isNullOrEmpty()) {
                                Text(
                                    text = profileName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = OngilGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(Modifier.width(3.dp))
                    }

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

        // 환자 선택 모달 (화면 정중앙) - 보호자일 때만
        if (isGuardian && showPatientModal && patients.isNotEmpty()) {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showPatientModal = false },
                properties = PopupProperties(focusable = true)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                    ) {
                        SelectPatientModal(
                            patients = patients,
                            selectedPatientId = selectedPatientId,
                            onSelectPatient = { patient ->
                                onSelectPatient(patient)
                                showPatientModal = false
                            },
                            onDismiss = { showPatientModal = false }
                        )
                    }
                }
            }
        }
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
    patients: List<PatientInfoUi> = emptyList(),
    onSelectPatient: (PatientInfoUi) -> Unit = {},
    userType: String = "",
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
                patients = patients,
                onSelectPatient = onSelectPatient,
                userType = userType,
                modifier = modifier
            )
    }
}

/** route에 따라 제목을 자동으로 설정하는 TopBar */
@Composable
fun OngilTopBarForRoute(
    route: String,
    onBackClick: () -> Unit,
    onBellClick: () -> Unit = {},
    userType: String = "" // 사용자 타입 (PATIENT, GUARDIAN 등)
) {
    // route에 따라 화면 제목 자동 설정
    val title = when (route) {
        "edit_info" -> "내 정보 수정"
        "my_info" -> "내 정보"
        "call_history" -> "최근 통화목록"
        "change_password" -> "비밀번호 변경"
        "notifications" -> "알림"
        "search_user" -> "사용자 찾기"
        "register_user" -> "사용자 등록"
        "location" -> "위치"
        "favorite" -> "즐겨찾기"
        "home" -> "홈"
        "patient_list" -> "환자 정보"
        else -> when {
            route.startsWith("call_detail") -> "통화 상세"
            route.startsWith("user_detail") -> {
                // 사용자 타입에 따라 동적으로 제목 변경
                when (userType.uppercase()) {
                    "PATIENT" -> "보호자 상세"
                    "GUARDIAN" -> "환자 상세"
                    else -> "사용자 상세" // 기본값
                }
            }
            route.startsWith("place_detail") -> "장소 상세"
            else -> ""
        }
    }

    OngilTopBar(
        title = title,
        onBackClick = onBackClick,
        onBellClick = onBellClick
    )
}

