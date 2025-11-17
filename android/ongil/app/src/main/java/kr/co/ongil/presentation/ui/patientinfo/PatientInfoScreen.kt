package kr.co.ongil.presentation.ui.patientinfo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.PI
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.ongil.presentation.ui.common.patientinfo.InfoCard
import kr.co.ongil.presentation.ui.common.patientinfo.StatValue
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning


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

/**
 * 추이 문자열을 화살표 아이콘으로 변환
 * - INCREASE: 상승 화살표 (↗)
 * - DECREASE: 하강 화살표 (↘)
 * - SAME: 수평 화살표 (→)
 */
private fun getTrendIcon(transition: String): StatValue.Icon {
    return when (transition) {
        "INCREASE" -> StatValue.Icon(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            tint = Color(0xFF101828)
        )
        "DECREASE" -> StatValue.Icon(
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            tint = Color(0xFF101828)
        )
        else -> StatValue.Icon(
            icon = Icons.AutoMirrored.Filled.TrendingFlat,
            tint = Color(0xFF101828)
        )
    }
}

// ———————————————— Tab 1: 활동 기록 ————————————————
@Composable
private fun ActivityLogTab(uiState: PatientInfoUiState) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("활동 기록") }

        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("로딩 중...", color = OnGilColors.Label)
                    }
                }
            }

            uiState.error != null -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.error, color = Color.Red)
                    }
                }
            }

            uiState.activityLog != null -> {
                val activityLog = uiState.activityLog

                // 주간 요약 섹션
                if (uiState.summary.isNotEmpty()) {
                    item {
                        SummarySection(summary = uiState.summary)
                    }
                }

                // 좋아요 섹션
                item {
                    PositiveSignalsSection(signals = uiState.positiveSignals)
                }

                // 주의가 필요해요 섹션
                item {
                    WarningSignalsSection(signals = uiState.warningSignals)
                }

                // 보호자 팁 섹션 (가디언일 때만)
                if (uiState.userType == "GUARDIAN") {
                    item {
                        CaregiverSuggestionsSection(suggestions = uiState.caregiverSuggestions)
                    }
                }

                // 가장 많이 찾은 목적지
                item {
                    val stats = if (activityLog.favoriteLocations.isNotEmpty()) {
                        activityLog.favoriteLocations.take(3).map { location ->
                            val displayName = if (location.placeName.length > 6) {
                                location.placeName.take(6) + "..."
                            } else {
                                location.placeName
                            }
                            displayName to StatValue.Text("${location.placeCount}회")
                        }
                    } else {
                        listOf(
                            "데이터 없음" to StatValue.Text("-")
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
                        "1단계" to StatValue.Text("${first}회"),
                        "2단계" to StatValue.Text("${second}회"),
                        "3단계" to StatValue.Text("${third}회")
                    )
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "안전구역 이탈 이력",
                        stats = safezoneStats
                    )
                }

                // 길찾기 이탈
                item {
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "길찾기 이탈",
                        stats = listOf(
                            "합계" to StatValue.Text("${activityLog.routeLost}회"),
                            "저번주 대비" to StatValue.Text("${activityLog.routeLostDiff}회"),
                            "추이" to getTrendIcon(activityLog.routeTransition)
                        )
                    )
                }

                // 안전구역 이상탐지 발생 빈도
                item {
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "안전구역 이상탐지 발생 빈도",
                        stats = listOf(
                            "합계" to StatValue.Text("${activityLog.safezoneEmer}회"),
                            "저번주 대비" to StatValue.Text("${activityLog.safezoneEmerDiff}회"),
                            "추이" to getTrendIcon(activityLog.safezoneTransition)
                        )
                    )
                }

                // 도움요청 이력
                item {
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "도움요청 이력",
                        stats = listOf(
                            "합계" to StatValue.Text("${activityLog.sosSign}회"),
                            "저번주 대비" to StatValue.Text("${activityLog.sosSignDiff}회"),
                            "추이" to getTrendIcon(activityLog.sosSignTransition)
                        )
                    )
                }

                // 응급전화 이력
                item {
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "응급전화 이력",
                        stats = listOf(
                            "합계" to StatValue.Text("${activityLog.emerCall}회"),
                            "저번주 대비" to StatValue.Text("${activityLog.emerCallDiff}회"),
                            "추이" to getTrendIcon(activityLog.emerCallTransition)
                        )
                    )
                }
            }
        }
    }
}


@Composable
private fun RiskBarRow(
    label: String,
    value: Long,
    maxValue: Long,
    modifier: Modifier = Modifier
) {
    val fraction = if (maxValue > 0) value.toFloat() / maxValue.toFloat() else 0f

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = OnGilColors.Title,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${value}회",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = OnGilColors.Title
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(OnGilColors.Pill)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(OnGilColors.Primary)
            )
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

    // Health Connect 권한 요청 launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { 
        viewModel.onPermissionResult()
    }

    // 디버깅용 로그
    android.util.Log.d("HealthInfoTab", "healthPermissionGranted: ${uiState.healthPermissionGranted}")
    android.util.Log.d("HealthInfoTab", "healthData: ${uiState.healthData}")

    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 섹션 제목과 버튼
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("건강 정보")

                // 건강정보 불러오기 버튼
                if (uiState.healthPermissionGranted) {
                    Button(
                        onClick = {
                            viewModel.syncHealthDataToServer()
                        },
                        enabled = !uiState.isLoadingHealthData,
                        colors = ButtonDefaults.buttonColors(containerColor = OnGilColors.Primary),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (uiState.isLoadingHealthData) {
                            Text("불러오는 중...", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("건강정보 불러오기", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // 동기화 메시지 표시
        if (uiState.healthSyncMessage != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (uiState.healthSyncMessage.contains("성공"))
                        Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.healthSyncMessage.contains("성공"))
                                Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (uiState.healthSyncMessage.contains("성공"))
                                Color(0xFF4CAF50) else Color(0xFFFFA726),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = uiState.healthSyncMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnGilColors.Title
                        )
                    }
                }
            }
        }

        val healthData = uiState.healthData

        // 권한이 있으면 항상 4개 카드 표시
        if (uiState.healthPermissionGranted) {
            // 심박수
            item {
                val heartRateRecords = healthData?.heartRateRecords ?: emptyList()
                val stats = if (heartRateRecords.isNotEmpty()) {
                    val values = heartRateRecords.map { it.beatsPerMinute }
                    listOf(
                        "평균" to StatValue.Text("${values.average().toLong()} BPM"),
                        "최대" to StatValue.Text("${values.maxOrNull() ?: 0} BPM"),
                        "최소" to StatValue.Text("${values.minOrNull() ?: 0} BPM")
                    )
                } else {
                    listOf("" to StatValue.Text("Samsung Health에서\n심박수를 측정해주세요"))
                }
                InfoCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "심박수 (최근 30일)",
                    stats = stats
                )
            }

            // 혈중산소포화도
            item {
                val oxygenRecords = healthData?.oxygenSaturationRecords ?: emptyList()
                val stats = if (oxygenRecords.isNotEmpty()) {
                    val values = oxygenRecords.map { it.percentage }
                    listOf(
                        "평균" to StatValue.Text("${String.format("%.1f", values.average())}%"),
                        "최대" to StatValue.Text("${String.format("%.1f", values.maxOrNull() ?: 0.0)}%"),
                        "최소" to StatValue.Text("${String.format("%.1f", values.minOrNull() ?: 0.0)}%")
                    )
                } else {
                    listOf("" to StatValue.Text("Samsung Health에서\n혈중산소포화도를 측정해주세요"))
                }
                InfoCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "혈중산소포화도 (최근 30일)",
                    stats = stats
                )
            }

            // 수면
            item {
                val sleepRecords = healthData?.sleepRecords ?: emptyList()
                val stats = if (sleepRecords.isNotEmpty()) {
                    val values = sleepRecords.map { it.durationHours }
                    listOf(
                        "평균" to StatValue.Text("${String.format("%.1f", values.average())}시간"),
                        "최대" to StatValue.Text("${String.format("%.1f", values.maxOrNull() ?: 0.0)}시간"),
                        "최소" to StatValue.Text("${String.format("%.1f", values.minOrNull() ?: 0.0)}시간")
                    )
                } else {
                    listOf("" to StatValue.Text("Samsung Health에서\n수면 기록을 측정해주세요"))
                }
                InfoCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "수면 (최근 30일)",
                    stats = stats
                )
            }

            // 걸음수
            item {
                val stepsRecords = healthData?.stepsRecords ?: emptyList()
                val stats = if (stepsRecords.isNotEmpty()) {
                    val values = stepsRecords.map { it.count }
                    listOf(
                        "평균" to StatValue.Text("${String.format("%,d", values.average().toLong())}걸음"),
                        "최대" to StatValue.Text("${String.format("%,d", values.maxOrNull() ?: 0)}걸음"),
                        "최소" to StatValue.Text("${String.format("%,d", values.minOrNull() ?: 0)}걸음")
                    )
                } else {
                    listOf("" to StatValue.Text("Samsung Health에서\n걸음수를 측정해주세요"))
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
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    val permissions = viewModel.getPermissionsToRequest()
                                    permissionLauncher.launch(permissions)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OnGilColors.Primary)
                        ) {
                            Text("권한 요청하기")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = OnGilColors.Label,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

// ============ 고급 시각화 컴포넌트들 ============

// 1. 주간 위험 스코어 게이지
@Composable
private fun RiskGaugeCard(
    activityLog: ActivityLog,
    modifier: Modifier = Modifier
) {
    val totalIncidents = activityLog.routeLost + activityLog.safezoneEmer +
                        activityLog.sosSign + activityLog.emerCall

    // 위험도 계산 (100점 만점에서 감점 방식)
    val riskScore = (100 - (totalIncidents * 5).coerceAtMost(100)).toInt()

    val riskLevel = when {
        riskScore >= 80 -> "양호" to Color(0xFF4CAF50)
        riskScore >= 60 -> "주의" to Color(0xFFFFA726)
        else -> "위험" to Color(0xFFEF5350)
    }

    val previousTotal = (activityLog.routeLost - activityLog.routeLostDiff) +
                       (activityLog.safezoneEmer - activityLog.safezoneEmerDiff) +
                       (activityLog.sosSign - activityLog.sosSignDiff) +
                       (activityLog.emerCall - activityLog.emerCallDiff)
    val previousScore = (100 - (previousTotal * 5).coerceAtMost(100)).toInt()
    val scoreDiff = riskScore - previousScore

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "이번 주 안전 점수",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnGilColors.Title
            )
            Spacer(Modifier.height(20.dp))

            // 원형 게이지
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                CircularGauge(
                    score = riskScore,
                    color = riskLevel.second
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${riskScore}점",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = OnGilColors.Title
                    )
                    Text(
                        text = if (scoreDiff > 0) "지난주보다 +${scoreDiff}점"
                               else if (scoreDiff < 0) "지난주보다 ${scoreDiff}점"
                               else "지난주와 동일",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (scoreDiff > 0) Color(0xFF4CAF50)
                               else if (scoreDiff < 0) Color(0xFFEF5350)
                               else OnGilColors.Label
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 상태 표시
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = riskLevel.second.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${riskLevel.first} (${riskScore}점 이상)",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = riskLevel.second
                )
            }
        }
    }
}

@Composable
private fun CircularGauge(
    score: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 20.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val centerX = size.width / 2
        val centerY = size.height / 2

        // 배경 원
        drawCircle(
            color = Color(0xFFE0E0E0),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidth)
        )

        // 진행도 원
        val sweepAngle = (score / 100f) * 360f
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

// 2. 행동 위험 레이더 차트
@Composable
private fun BehaviorRadarCard(
    activityLog: ActivityLog,
    modifier: Modifier = Modifier
) {
    val maxValue = 10L // 최대값 기준
    val values = listOf(
        activityLog.routeLost.toFloat() / maxValue.toFloat(),
        activityLog.safezoneEmer.toFloat() / maxValue.toFloat(),
        activityLog.sosSign.toFloat() / maxValue.toFloat(),
        activityLog.emerCall.toFloat() / maxValue.toFloat()
    ).map { it.coerceIn(0f, 1f) }

    val labels = listOf("길찾기\n이탈", "안전구역\n이상탐지", "도움\n요청", "응급\n전화")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "행동 패턴 분석",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnGilColors.Title
            )
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarChart(
                    values = values,
                    labels = labels,
                    modifier = Modifier.size(220.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "차트가 넓게 퍼질수록 위험도가 높습니다",
                style = MaterialTheme.typography.bodySmall,
                color = OnGilColors.Label,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RadarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2 * 0.8f
            val angleStep = (2 * PI / values.size).toFloat()

            // 배경 격자 (3단계)
            for (level in 1..3) {
                val levelRadius = radius * (level / 3f)
                val path = Path()
                for (i in values.indices) {
                    val angle = -PI.toFloat() / 2 + angleStep * i
                    val x = centerX + levelRadius * cos(angle)
                    val y = centerY + levelRadius * sin(angle)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(
                    path = path,
                    color = Color(0xFFE0E0E0),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 축 선
            for (i in values.indices) {
                val angle = -PI.toFloat() / 2 + angleStep * i
                val endX = centerX + radius * cos(angle)
                val endY = centerY + radius * sin(angle)
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(centerX, centerY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 데이터 영역
            val dataPath = Path()
            for (i in values.indices) {
                val angle = -PI.toFloat() / 2 + angleStep * i
                val distance = radius * values[i]
                val x = centerX + distance * cos(angle)
                val y = centerY + distance * sin(angle)
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            // 채우기
            drawPath(
                path = dataPath,
                color = OnGilColors.Primary.copy(alpha = 0.3f)
            )

            // 테두리
            drawPath(
                path = dataPath,
                color = OnGilColors.Primary,
                style = Stroke(width = 2.dp.toPx())
            )

            // 데이터 포인트
            for (i in values.indices) {
                val angle = -PI.toFloat() / 2 + angleStep * i
                val distance = radius * values[i]
                val x = centerX + distance * cos(angle)
                val y = centerY + distance * sin(angle)
                drawCircle(
                    color = OnGilColors.Primary,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // 라벨 (레이더 차트 외곽)
        // Note: Compose에서는 Box 내부 절대 위치 지정이 복잡하므로 생략
        // 실제 구현에서는 Layout을 사용하거나 별도 처리 필요
    }
}

// 3. 이상변동 강조 카드
@Composable
private fun AbnormalChangeSpotlightCard(
    activityLog: ActivityLog,
    modifier: Modifier = Modifier
) {
    // 가장 큰 변화 찾기
    val changes = listOf(
        "야간 이탈" to activityLog.routeLostDiff,
        "안전구역 이상탐지" to activityLog.safezoneEmerDiff,
        "도움 요청" to activityLog.sosSignDiff,
        "응급 전화" to activityLog.emerCallDiff
    )

    val maxChange = changes.maxByOrNull { kotlin.math.abs(it.second) }

    if (maxChange == null || maxChange.second == 0L) return

    val isIncrease = maxChange.second > 0
    val percentage = kotlin.math.abs(maxChange.second) * 100 /
                    (if (isIncrease) maxChange.second else 1).coerceAtLeast(1)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isIncrease) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isIncrease) Color(0xFFFFA726) else Color(0xFF66BB6A),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isIncrease) "⚠️" else "✅",
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${maxChange.first}이 ${if (isIncrease) "증가" else "감소"}했습니다",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnGilColors.Title
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "지난주 대비 ${kotlin.math.abs(maxChange.second)}회 ${if (isIncrease) "증가" else "감소"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnGilColors.Label
                )
            }
        }
    }
}

// 4. 시간대별 위험 히트맵
@Composable
private fun TimeBasedHeatmapCard(
    activityLog: ActivityLog,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "시간대별 위험도",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnGilColors.Title
            )
            Spacer(Modifier.height(12.dp))

            // 간단한 시간대별 막대 (실제로는 더 상세한 데이터 필요)
            val timeSlots = listOf(
                "00-06시" to 0.2f,
                "06-12시" to 0.1f,
                "12-18시" to 0.3f,
                "18-24시" to 0.4f
            )

            timeSlots.forEach { (time, intensity) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp),
                        color = OnGilColors.Title
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF0F0F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(intensity)
                                .fillMaxHeight()
                                .background(
                                    when {
                                        intensity > 0.6f -> Color(0xFFEF5350)
                                        intensity > 0.3f -> Color(0xFFFFA726)
                                        else -> Color(0xFF66BB6A)
                                    }
                                )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = "색이 진할수록 해당 시간대에 위험 행동이 많이 발생했습니다",
                style = MaterialTheme.typography.bodySmall,
                color = OnGilColors.Label,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// 5. 위험 행동 누적 그래프
@Composable
private fun CumulativeIncidentsCard(
    activityLog: ActivityLog,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "위험 행동 누적 추이",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnGilColors.Title
            )
            Spacer(Modifier.height(16.dp))

            // 간단한 라인 차트 (실제 구현에서는 더 상세한 데이터 필요)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .drawBehind {
                        val width = size.width
                        val height = size.height
                        val points = 7

                        // 가상 데이터 (1주일)
                        val data = listOf(2f, 3f, 2f, 4f, 3f, 5f, activityLog.routeLost.toFloat())
                        val maxVal = data.maxOrNull() ?: 1f

                        val path = Path()
                        data.forEachIndexed { index, value ->
                            val x = (width / (points - 1)) * index
                            val y = height - (value / maxVal * height * 0.8f)
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = OnGilColors.Primary,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // 포인트
                        data.forEachIndexed { index, value ->
                            val x = (width / (points - 1)) * index
                            val y = height - (value / maxVal * height * 0.8f)
                            drawCircle(
                                color = OnGilColors.Primary,
                                radius = 5.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "일주일 전",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnGilColors.Label
                )
                Text(
                    text = "오늘",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnGilColors.Label
                )
            }
        }
    }
}

// 6. 활동 vs 건강 교차 지표
@Composable
private fun CrossInsightCard(
    activityLog: ActivityLog,
    healthData: kr.co.ongil.data.model.health.LocalHealthData,
    modifier: Modifier = Modifier
) {
    val insights = mutableListOf<Pair<String, String>>()

    // 개별 레코드에서 평균 계산
    val avgSleep = healthData.sleepRecords.map { it.durationHours }.average().takeIf { !it.isNaN() } ?: 0.0
    val avgHeartRate = healthData.heartRateRecords.map { it.beatsPerMinute.toDouble() }.average().takeIf { !it.isNaN() } ?: 0.0
    val avgSteps = healthData.stepsRecords.map { it.count.toDouble() }.average().takeIf { !it.isNaN() } ?: 0.0

    // 교차 분석
    if (activityLog.routeLost > 3 && avgSleep < 6.0) {
        insights.add("활동량 감소" to "수면 부족으로 인한 피로 가능성")
    }

    if (activityLog.safezoneEmer > 2 && avgHeartRate > 80) {
        insights.add("이상탐지 증가" to "스트레스나 불안 가능성")
    }

    if (avgSteps < 2000) {
        insights.add("걸음 수 감소" to "활동량 저하 주의 필요")
    }

    if (insights.isEmpty()) {
        insights.add("양호" to "활동과 건강 지표가 모두 안정적입니다")
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "종합 인사이트",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnGilColors.Title
            )
            Spacer(Modifier.height(12.dp))

            insights.forEach { (title, description) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnGilColors.Primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnGilColors.Title
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnGilColors.Label
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewPatientInfoScreen() {
    MaterialTheme {
        PatientInfoScreenPreview()
    }
}

@Composable
private fun PatientInfoScreenPreview() {
    val previewActivityLog = ActivityLog(
        favoriteLocations = listOf(
            FavoriteLocation(rank = 1, placeName = "집", placeCount = 15),
            FavoriteLocation(rank = 2, placeName = "병원", placeCount = 8),
            FavoriteLocation(rank = 3, placeName = "공원", placeCount = 5)
        ),
        safezoneExit = mapOf("FIRST" to 2, "SECOND" to 1, "THIRD" to 0),
        routeLost = 3L,
        routeLostDiff = 1L,
        routeTransition = "INCREASE",
        safezoneEmer = 2L,
        safezoneEmerDiff = 0L,
        safezoneTransition = "SAME",
        sosSign = 1L,
        sosSignDiff = -1L,
        sosSignTransition = "DECREASE",
        emerCall = 0L,
        emerCallDiff = 0L,
        emerCallTransition = "SAME"
    )

    val previewUiState = PatientInfoUiState(
        isLoading = false,
        activityLog = previewActivityLog,
        error = null,
        healthPermissionGranted = false,
        healthData = null
    )

    Surface(color = OnGilColors.Bg) {
        Column(
            modifier = Modifier
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
                text = "활동기록을 확인하고 관리해보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = OnGilColors.Label
            )
            Spacer(Modifier.height(16.dp))

            SegmentedTwoTabs(
                left = "활동 기록",
                right = "건강 정보",
                selectedIndex = 0,
                onSelected = { }
            )

            Spacer(Modifier.height(20.dp))

            ActivityLogTab(uiState = previewUiState)
        }
    }
}

// ============ 인사이트 섹션 컴포넌트들 ============

private enum class SignalType { POSITIVE, WARNING }

@Composable
private fun SummarySection(
    summary: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF5F7FA),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8EAF6),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "📊",
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "주간 요약",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnGilColors.Title
                )
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                    color = OnGilColors.Title,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PositiveSignalsSection(
    signals: List<String>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✨",
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "좋아요",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnGilColors.Title
                )
            }

            Spacer(Modifier.height(16.dp))

            if (signals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "긍정적인 신호가 감지되면 여기에 표시됩니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnGilColors.Label,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    signals.forEach { text ->
                        SignalChip(
                            text = text,
                            type = SignalType.POSITIVE
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WarningSignalsSection(
    signals: List<String>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "⚠️",
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "주의가 필요해요",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnGilColors.Title
                )
            }

            Spacer(Modifier.height(16.dp))

            if (signals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "주의가 필요한 신호가 감지되면 여기에 표시됩니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnGilColors.Label,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    signals.forEach { text ->
                        SignalChip(
                            text = text,
                            type = SignalType.WARNING
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaregiverSuggestionsSection(
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "💡",
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "보호자 팁",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnGilColors.Title
                )
            }

            Spacer(Modifier.height(16.dp))

            if (suggestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "보호자 팁이 준비되면 여기에 표시됩니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnGilColors.Label,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFFBF0)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        suggestions.forEach { line ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFFFA726),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnGilColors.Title,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalChip(
    text: String,
    type: SignalType,
    modifier: Modifier = Modifier,
    positiveIconColor: Color = Color(0xFF4CAF50),
    warningIconColor: Color = Color(0xFFD1462C)
) {
    val bgColor = when (type) {
        SignalType.POSITIVE -> Color(0xFFF1F8F4)
        SignalType.WARNING -> Color(0xFFFFF4E5)
    }
    val borderColor = when (type) {
        SignalType.POSITIVE -> Color(0xFF4CAF50).copy(alpha = 0.3f)
        SignalType.WARNING -> Color(0xFFFFA726).copy(alpha = 0.3f)
    }
    val icon = when (type) {
        SignalType.POSITIVE -> Icons.Filled.CheckCircle
        SignalType.WARNING -> Icons.Filled.Warning
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = when (type) {
                    SignalType.POSITIVE -> positiveIconColor
                    SignalType.WARNING -> warningIconColor
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = OnGilColors.Title
            )
        }
    }
}