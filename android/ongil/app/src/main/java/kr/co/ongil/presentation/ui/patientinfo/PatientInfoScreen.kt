package kr.co.ongil.presentation.ui.patientinfo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.health.connect.client.PermissionController
import kotlinx.coroutines.launch
import kr.co.ongil.presentation.ui.common.patientinfo.InfoCard



private object OnGilColors {
    val Primary = Color(0xFF8CA898)
    val Bg = Color(0xFFF7F8F9)
    val CardStroke = Color(0xFFE8ECEF)
    val Label = Color(0xFF6B767A)
    val Title = Color(0xFF2A2F2E)
    val Pill = Color(0xFFEFF2F3)
}

@Composable
fun PatientInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: PatientInfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selected by rememberSaveable { mutableStateOf(0) }

    Surface(color = OnGilColors.Bg) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "환자 기록",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = OnGilColors.Title,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (selected == 0) "활동기록을 확인하고 관리해보세요" else "건강정보를 확인해보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = OnGilColors.Label
            )
            Spacer(Modifier.height(16.dp))

            SegmentedTwoTabs(
                left = "활동 기록",
                right = "건강 정보",
                selectedIndex = selected,
                onSelected = { selected = it }
            )

            Spacer(Modifier.height(20.dp))

            if (selected == 0) {
                ActivityLogTab(uiState = uiState)
            } else {
                HealthInfoTab(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun SegmentedTwoTabs(
    left: String,
    right: String,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(18.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(pillShape)
            .background(OnGilColors.Pill)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SegmentItem(text = left, selected = selectedIndex == 0, onClick = { onSelected(0) }, modifier = Modifier.weight(1f))
        SegmentItem(text = right, selected = selectedIndex == 1, onClick = { onSelected(1) }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SegmentItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) OnGilColors.Title else OnGilColors.Label,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun zipStats(labels: String, values: String): List<Pair<String, String>> {
    val ls = labels.split("/")
    val vs = values.split("/")
    return ls.zip(vs)
}

// ———————————————— Tab 1: 활동 기록 ————————————————
@Composable
private fun ActivityLogTab(uiState: PatientInfoUiState) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("활동 기록") }

        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("로딩 중...", color = OnGilColors.Label)
                    }
                }
            }
            uiState.error != null -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.error, color = Color.Red)
                    }
                }
            }
            uiState.activityLog != null -> {
                val activityLog = uiState.activityLog

                // 가장 많이 찾은 목적지
                item {
                    val stats = if (activityLog.favoriteLocations.isNotEmpty()) {
                        activityLog.favoriteLocations.take(3).map { location ->
                            val displayName = if (location.placeName.length > 6) {
                                location.placeName.take(6) + "..."
                            } else {
                                location.placeName
                            }
                            displayName to "${location.placeCount}회"
                        }
                    } else {
                        listOf(
                            "데이터 없음" to "-"
                        )
                    }
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "가장 많이 찾은 목적지",
                        stats = stats
                    )
                }

                // 안전구역 이탈 이력
                item {
                    val first = activityLog.safezoneExit["FIRST"] ?: 0
                    val second = activityLog.safezoneExit["SECOND"] ?: 0
                    val third = activityLog.safezoneExit["THIRD"] ?: 0

                    val safezoneStats = listOf(
                        "1단계" to "${first}회",
                        "2단계" to "${second}회",
                        "3단계" to "${third}회"
                    )
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "안전구역 이탈 이력",
                        stats = safezoneStats
                    )
                }

                // 길찾기 이탈
                item {
                    val transition = when(activityLog.routeTransition) {
                        "INCREASE" -> "증가"
                        "DECREASE" -> "감소"
                        else -> "동일"
                    }
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "길찾기 이탈",
                        stats = listOf(
                            "합계" to "${activityLog.routeLost}회",
                            "저번주 대비" to "${activityLog.routeLostDiff}회",
                            "추이" to transition
                        )
                    )
                }

                // 안전구역 이상탐지 발생 빈도
                item {
                    val transition = when(activityLog.safezoneTransition) {
                        "INCREASE" -> "증가"
                        "DECREASE" -> "감소"
                        else -> "동일"
                    }
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "안전구역 이상탐지 발생 빈도",
                        stats = listOf(
                            "합계" to "${activityLog.safezoneEmer}회",
                            "저번주 대비" to "${activityLog.safezoneEmerDiff}회",
                            "추이" to transition
                        )
                    )
                }

                // 도움요청 이력
                item {
                    val transition = when(activityLog.sosSignTransition) {
                        "INCREASE" -> "증가"
                        "DECREASE" -> "감소"
                        else -> "동일"
                    }
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "도움요청 이력",
                        stats = listOf(
                            "합계" to "${activityLog.sosSign}회",
                            "저번주 대비" to "${activityLog.sosSignDiff}회",
                            "추이" to transition
                        )
                    )
                }

                // 응급전화 이력
                item {
                    val transition = when(activityLog.emerCallTransition) {
                        "INCREASE" -> "증가"
                        "DECREASE" -> "감소"
                        else -> "동일"
                    }
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "응급전화 이력",
                        stats = listOf(
                            "합계" to "${activityLog.emerCall}회",
                            "저번주 대비" to "${activityLog.emerCallDiff}회",
                            "추이" to transition
                        )
                    )
                }
            }
        }
    }
}

// ———————————————— Tab 2: 건강 정보 ————————————————
@Composable
private fun HealthInfoTab(
    uiState: PatientInfoUiState,
    viewModel: PatientInfoViewModel
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Health Connect 권한 요청 launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        viewModel.onPermissionResult()
    }

    // 디버깅용 로그
    android.util.Log.d("HealthInfoTab", "healthPermissionGranted: ${uiState.healthPermissionGranted}")
    android.util.Log.d("HealthInfoTab", "healthData: ${uiState.healthData}")

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("건강 정보") }

        val healthData = uiState.healthData

        // 권한이 있으면 항상 4개 카드 표시
        if (uiState.healthPermissionGranted) {
            // 심박수
            item {
                val heartRate = healthData?.heartRate
                val stats = if (heartRate != null) {
                    listOf(
                        "평균" to "${heartRate.average} BPM",
                        "최대" to "${heartRate.max} BPM",
                        "최소" to "${heartRate.min} BPM"
                    )
                } else {
                    listOf("" to "Samsung Health에서\n심박수를 측정해주세요")
                }
                InfoCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "심박수 (최근 30일)",
                    stats = stats
                )
            }

            // 혈중산소포화도
            item {
                val oxygen = healthData?.oxygenSaturation
                val stats = if (oxygen != null) {
                    listOf(
                        "평균" to "${String.format("%.1f", oxygen.average)}%",
                        "최대" to "${String.format("%.1f", oxygen.max)}%",
                        "최소" to "${String.format("%.1f", oxygen.min)}%"
                    )
                } else {
                    listOf("" to "Samsung Health에서\n혈중산소포화도를 측정해주세요")
                }
                InfoCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "혈중산소포화도 (최근 30일)",
                    stats = stats
                )
            }

            // 수면
            item {
                val sleep = healthData?.sleep
                val stats = if (sleep != null) {
                    listOf(
                        "평균" to "${String.format("%.1f", sleep.average)}시간",
                        "최대" to "${String.format("%.1f", sleep.max)}시간",
                        "최소" to "${String.format("%.1f", sleep.min)}시간"
                    )
                } else {
                    listOf("" to "Samsung Health에서\n수면 기록을 측정해주세요")
                }
                InfoCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "수면 (최근 30일)",
                    stats = stats
                )
            }

            // 걸음수
            item {
                val steps = healthData?.steps
                val stats = if (steps != null) {
                    listOf(
                        "평균" to "${String.format("%,d", steps.average)}걸음",
                        "최대" to "${String.format("%,d", steps.max)}걸음",
                        "최소" to "${String.format("%,d", steps.min)}걸음"
                    )
                } else {
                    listOf("" to "Samsung Health에서\n걸음수를 측정해주세요")
                }
                InfoCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "걸음수 (최근 30일)",
                    stats = stats
                )
            }
        } else {
            // 데이터가 없는 경우
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!uiState.healthPermissionGranted) {
                        // 권한이 없는 경우
                        Text(
                            text = "건강 데이터를 확인하려면\nHealth Connect 권한이 필요합니다.",
                            color = OnGilColors.Label,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    val permissions = viewModel.getPermissionsToRequest()
                                    if (permissions.isNotEmpty()) {
                                        permissionLauncher.launch(permissions)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OnGilColors.Primary
                            )
                        ) {
                            Text("권한 요청하기")
                        }
                    } else {
                        // 권한은 있지만 데이터가 없는 경우
                        Text(
                            text = "건강 데이터를 불러올 수 없습니다",
                            color = OnGilColors.Label,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Health Connect와 Samsung Health\n연동 확인이 필요합니다",
                            color = OnGilColors.Label.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Health Connect 앱 열기 버튼
                        Button(
                            onClick = {
                                try {
                                    val intent = context.packageManager.getLaunchIntentForPackage(
                                        "com.google.android.apps.healthdata"
                                    )
                                    if (intent != null) {
                                        context.startActivity(intent)
                                    } else {
                                        // Health Connect가 설치되지 않은 경우
                                        android.widget.Toast.makeText(
                                            context,
                                            "Health Connect 앱이 설치되지 않았습니다",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Health Connect 앱을 열 수 없습니다",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OnGilColors.Primary
                            )
                        ) {
                            Text("Health Connect 앱 열기")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "확인사항:",
                                color = OnGilColors.Label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Health Connect에서 Samsung Health가\n   데이터 소스로 연결되어 있는지",
                                color = OnGilColors.Label.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "2. Ongil 앱에 데이터 읽기 권한이\n   부여되어 있는지",
                                color = OnGilColors.Label.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "3. Samsung Health에서 Health Connect로\n   데이터 공유가 활성화되어 있는지",
                                color = OnGilColors.Label.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = OnGilColors.Title,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

// ———————————————— Previews ————————————————
@Preview(name = "활동 기록 탭", showBackground = true)
@Composable
private fun Preview_ActivityLogTab() {
    Surface { PatientInfoScreen_Preview(selected = 0) }
}

@Preview(name = "건강 정보 탭", showBackground = true)
@Composable
private fun Preview_HealthInfoTab() {
    Surface { PatientInfoScreen_Preview(selected = 1) }
}

@Composable
private fun PatientInfoScreen_Preview(selected: Int) {
    var sel by remember { mutableStateOf(selected) }
    val previewUiState = PatientInfoUiState(
        isLoading = false,
        activityLog = ActivityLog(
            favoriteLocations = listOf(
                FavoriteLocation(1, "답십리공원", 13),
                FavoriteLocation(2, "엔제리너스대점건", 8),
                FavoriteLocation(3, "경희대학교병원", 5)
            ),
            safezoneExit = mapOf("FIRST" to 1, "SECOND" to 1, "THIRD" to 0),
            routeLost = 1,
            routeLostDiff = 0,
            routeTransition = "SAME",
            safezoneEmer = 2,
            safezoneEmerDiff = 0,
            safezoneTransition = "SAME",
            sosSign = 1,
            sosSignDiff = 0,
            sosSignTransition = "SAME",
            emerCall = 0,
            emerCallDiff = 0,
            emerCallTransition = "SAME"
        )
    )
    Surface(color = OnGilColors.Bg) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            SegmentedTwoTabs(left = "활동 기록", right = "건강 정보", selectedIndex = sel, onSelected = { sel = it })
            Spacer(Modifier.height(20.dp))
            if (sel == 0) {
                ActivityLogTab(uiState = previewUiState)
            } else {
                // Preview에서는 ViewModel을 제공할 수 없으므로 간단한 텍스트만 표시
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("건강 정보 탭 (Preview에서는 표시되지 않음)", color = OnGilColors.Label)
                }
            }
        }
    }
}