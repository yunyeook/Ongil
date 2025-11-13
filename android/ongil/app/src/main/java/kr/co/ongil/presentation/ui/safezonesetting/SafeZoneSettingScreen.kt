package kr.co.ongil.presentation.ui.safezonesetting

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.ui.common.GreenButton

/** SafeZone fixed palette (no theme dependency) */
private object SZ {
    // base
    val Bg = Color(0xFFFFFFFF)
    val TextMain = Color(0xFF111827)
    val TextSub = Color(0xFF6B7280)

    // brand greens
    val Green = Color(0xFF8CA898)      // main button/switch on
    val GreenLight = Color(0xFFEFEFEF) // badges

    // levels
    val Level1 = Color(0xFF4CAF50)     // green
    val Level2 = Color(0xFF2196F3)     // blue
    val Level3 = Color(0xFFE91E63)     // pink

    // map preview
    val MapBg = Color(0xFFF5F7F7)
    val MapRing1 = Color(0x33E91E63)   // outer (pink)
    val MapRing2 = Color(0x332196F3)   // middle (blue)
    val MapRing3 = Color(0x334CAF50)   // inner (green)
    val MapDot = Color(0xFF364046)

    val Error = Color(0xFFEF4444)
}

/**
 * 안전구역 설정 화면 (UI 전용)
 * - 상태는 UiState로 전달, 변경은 onEvent로 콜백
 * - 실제 ViewModel/Navigation 연동은 별도 파일에서 처리
 */

@Composable
fun SafeZoneSettingScreen(
    uiState: SafeZoneUiState,
    onEvent: (SafeZoneSettingUiEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = SZ.Bg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 타이틀 / 설명 영역
                SafeZoneTitleSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 지도 미리보기
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    SafeZoneMapPreview(
                        level1Radius = uiState.level1.distanceMeters,
                        level2Radius = uiState.level2.distanceMeters,
                        level3Radius = uiState.level3.distanceMeters,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SafeZoneAssistiveText(
                        text = "야간에는 설정값보다 탐지 범위가 줄어듭니다."
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 설정 섹션들
                Text(
                    text = "안전구역 및 탐지 설정",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                SafeZoneLevelSection(
                    title = "1단계",
                    color = SZ.Level1,
                    distanceMeters = uiState.level1.distanceMeters,
                    dwellMinutes = uiState.level1.dwellMinutes,
                    distanceRange = 50..150,
                    dwellRange = 1..30,
                    onDistanceChange = { onEvent(SafeZoneSettingUiEvent.ChangeLevelDistance(1, it)) },
                    onDwellChange = { onEvent(SafeZoneSettingUiEvent.ChangeLevelDwell(1, it)) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )

                SafeZoneLevelSection(
                    title = "2단계",
                    color = SZ.Level2,
                    distanceMeters = uiState.level2.distanceMeters,
                    dwellMinutes = uiState.level2.dwellMinutes,
                    distanceRange = 200..500,
                    dwellRange = 10..60,
                    onDistanceChange = { onEvent(SafeZoneSettingUiEvent.ChangeLevelDistance(2, it)) },
                    onDwellChange = { onEvent(SafeZoneSettingUiEvent.ChangeLevelDwell(2, it)) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )

                SafeZoneLevelSection(
                    title = "3단계",
                    color = SZ.Level3,
                    distanceMeters = uiState.level3.distanceMeters,
                    dwellMinutes = uiState.level3.dwellMinutes,
                    distanceRange = 500..1000,
                    dwellRange = 30..180,
                    onDistanceChange = { onEvent(SafeZoneSettingUiEvent.ChangeLevelDistance(3, it)) },
                    onDwellChange = { onEvent(SafeZoneSettingUiEvent.ChangeLevelDwell(3, it)) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "알림 설정",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                )

                SafeZoneAlertToggleCard(
                    title = "푸시 알림",
                    subtitle = "안전구역 이탈 시 앱 알림",
                    checked = uiState.pushEnabled,
                    onCheckedChange = { onEvent(SafeZoneSettingUiEvent.TogglePush(it)) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )


                Spacer(modifier = Modifier.height(24.dp))

                // 저장 버튼 (맨 하단)
                GreenButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    text = "설정 저장",
                    onClick = {
                        onEvent(
                            SafeZoneSettingUiEvent.Save(
                        onSuccess = { onBack() }
                            )
                        )
                    },
                    isActive = uiState.isSavable,
                    enabled = uiState.isSavable
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
    }
}

// ---------- UI Pieces ----------

@Composable
private fun SafeZoneTitleSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "안전구역 설정",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SZ.TextMain,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "자주 머무는 곳 반경을 정하고, 이탈 시 알림/연락 방식을 설정하세요.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = SZ.TextSub,
            lineHeight = 20.sp
        )
    }
}

// ---------- Preview ----------

@Preview(name = "SafeZone Setting - Light", showBackground = true)
@Preview(
    name = "SafeZone Setting - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun PreviewSafeZoneSettingScreen() {
    var state by remember {
        mutableStateOf(
            SafeZoneUiState()
        )
    }

    MaterialTheme {
        SafeZoneSettingScreen(
            uiState = state,
            onEvent = { event ->
                when (event) {
                    is SafeZoneSettingUiEvent.ChangeLevelDistance -> {
                        state = when (event.level) {
                            1 -> state.copy(level1 = state.level1.copy(distanceMeters = event.meters))
                            2 -> state.copy(level2 = state.level2.copy(distanceMeters = event.meters))
                            else -> state.copy(level3 = state.level3.copy(distanceMeters = event.meters))
                        }
                    }
                    is SafeZoneSettingUiEvent.ChangeLevelDwell -> {
                        state = when (event.level) {
                            1 -> state.copy(level1 = state.level1.copy(dwellMinutes = event.minutes))
                            2 -> state.copy(level2 = state.level2.copy(dwellMinutes = event.minutes))
                            else -> state.copy(level3 = state.level3.copy(dwellMinutes = event.minutes))
                        }
                    }
                    is SafeZoneSettingUiEvent.TogglePush -> state = state.copy(pushEnabled = event.enabled)
                    is SafeZoneSettingUiEvent.ToggleAutoCall -> state = state.copy(autoCallEnabled = event.enabled)
                    is SafeZoneSettingUiEvent.Save -> {}
                }
            },
            onBack = {}
        )
    }
}
