package kr.co.ongil.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kr.co.ongil.presentation.ui.patientinfo.ActivityLog
import kr.co.ongil.data.model.location.Coordinate
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import android.Manifest
import android.content.Intent
import androidx.core.content.ContextCompat
import kr.co.ongil.service.location.LocationTrackingService


private object HomeColors {
    val Primary = Color(0xFF88A293)
    val CardBg = Color(0xFFF3F4F6)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B767A)
    val Border = Color(0xFFD9DEE3)
    val HighlightBg = Color(0xFFE8EFEA)
    val OnPrimary = Color(0xFFFFFFFF)
}

@Immutable
data class HomeUiState(
    val guardianName: String,
    val patientName: String,
    val mostVisitedLabel: String,
    val mostVisitedPlace: String,
    val outOfSafeZoneCount: Int,
    val routeFailCount: Int,
    val averageSleepHours: Double? = null,
    val averageSteps: Int? = null,
    val activityLog: ActivityLog? = null,
    val healthData: kr.co.ongil.data.model.health.LocalHealthData? = null
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onMapClick: () -> Unit = {},
    userType: String = "",
    selectedPatientId: String? = null,
    patientLocations: Map<Long, Coordinate> = emptyMap(),
    locationBus: kr.co.ongil.common.location.LocationStreamBus? = null
) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current

    // 위치 권한 확인
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    // 위치 추적 서비스 시작 (환자만, 권한이 있을 때)
    LaunchedEffect(hasLocationPermission, userType) {
        if (!inPreview && hasLocationPermission && userType == "PATIENT") {
            android.util.Log.d("HomeScreen", "🚀 위치 추적 서비스 시작")
            val intent = Intent(context, LocationTrackingService::class.java)
                .setAction(LocationTrackingService.ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    // 화면 종료 시 위치 추적 서비스는 중지하지 않음 (MapScreen에서도 사용하므로)
    // MapScreen이 종료될 때 중지됨

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // 지도 섹션 (클릭 시 위치 탭으로 이동)
        MapSectionPreview(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            onClick = onMapClick,
            userType = userType,
            selectedPatientId = selectedPatientId,
            patientLocations = patientLocations,
            locationBus = locationBus
        )

        Spacer(Modifier.height(24.dp))

        // 대시보드 시작: "최근 정보를 요약했어요"
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = HomeColors.Primary)) {
                    append(uiState.patientName)
                }
                append("님의 최근 정보를\n요약했어요.")
            },
            style = MaterialTheme.typography.headlineSmall,
            lineHeight = MaterialTheme.typography.headlineSmall.lineHeight
        )

        Spacer(Modifier.height(20.dp))

        KeyIndicatorsSection(uiState = uiState)

        Spacer(Modifier.height(24.dp))

        // 활동 로그 데이터가 있으면 5개 시각화 컴포넌트 표시
        uiState.activityLog?.let { activityLog ->
            // 1행 2열: 이번 주 안전 점수 + 행동 패턴 분석
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. 이번 주 안전 점수
                RiskGaugeCard(
                    activityLog = activityLog,
                    modifier = Modifier.weight(1f)
                )

                // 2. 행동 패턴 분석
                BehaviorRadarCard(
                    activityLog = activityLog,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))

            // 3. 이상변동 강조 카드
            AbnormalChangeSpotlightCard(activityLog = activityLog)
            Spacer(Modifier.height(16.dp))

            // 4. 시간대별 위험도
            TimeBasedHeatmapCard(activityLog = activityLog)
            Spacer(Modifier.height(16.dp))

            // 5. 위험 행동 누적 추이
            CumulativeIncidentsCard(activityLog = activityLog)
            Spacer(Modifier.height(16.dp))

            // 6. 활동 vs 건강 교차 지표 (종합 인사이트)
            if (uiState.healthData != null) {
                CrossInsightCard(activityLog = activityLog, healthData = uiState.healthData)
                Spacer(Modifier.height(24.dp))
            } else {
                Spacer(Modifier.height(24.dp))
            }
        }

    }
}

@Composable
private fun MapSectionPreview(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    userType: String = "",
    selectedPatientId: String? = null,
    patientLocations: Map<Long, Coordinate> = emptyMap(),
    locationBus: kr.co.ongil.common.location.LocationStreamBus? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
    ) {
        // 실제 TMap 표시 (환자는 본인 위치 추적, 보호자는 환자 위치 표시)
        kr.co.ongil.presentation.ui.map.TMapComposable(
            modifier = Modifier.fillMaxSize(),
            enableTracking = userType == "PATIENT",  // 환자만 위치 추적
            locationBus = locationBus,
            zoomLevel = 14,
            userType = userType,
            selectedPatientId = selectedPatientId,
            patientLocations = patientLocations,
            isHomeScreen = true  // 홈 화면용 TMapView 사용
        )

        // 클릭 감지용 투명 레이어
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        )
    }
}

@Composable
private fun KeyIndicatorsSection(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    val hasHealthData = uiState.averageSleepHours != null && uiState.averageSteps != null

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "주요 지표",
            style = MaterialTheme.typography.titleMedium,
            color = HomeColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasHealthData) {
                IndicatorCard(
                    title = "수면 시간",
                    value = uiState.averageSleepHours?.let { String.format("%.1f시간", it) } ?: "-",
                    modifier = Modifier.weight(1f),
                    backgroundColor = HomeColors.Primary,
                    highlight = true
                )
                IndicatorCard(
                    title = "하루 평균 걸음 수",
                    value = uiState.averageSteps?.let { "%,d걸음".replace(" ", "").format(it) } ?: "-",
                    modifier = Modifier.weight(1f),
                    backgroundColor = HomeColors.CardBg,
                    isDashedBorder = true
                )
                IndicatorCard(
                    title = "안전구역 이탈",
                    value = "${uiState.outOfSafeZoneCount}회",
                    modifier = Modifier.weight(1f),
                    backgroundColor = HomeColors.CardBg,
                    isDashedBorder = true
                )
            } else {
                IndicatorCard(
                    title = uiState.mostVisitedLabel,
                    value = uiState.mostVisitedPlace,
                    modifier = Modifier.weight(1f),
                    backgroundColor = HomeColors.Primary,
                    highlight = true
                )
                IndicatorCard(
                    title = "안전구역 벗어난 횟수",
                    value = "${uiState.outOfSafeZoneCount}회",
                    modifier = Modifier.weight(1f),
                    backgroundColor = HomeColors.CardBg,
                    isDashedBorder = true
                )
                IndicatorCard(
                    title = "길찾기 실패 횟수",
                    value = "${uiState.routeFailCount}회",
                    modifier = Modifier.weight(1f),
                    backgroundColor = HomeColors.CardBg,
                    isDashedBorder = true
                )
            }
        }
    }
}

@Composable
private fun IndicatorCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    backgroundColor: Color = if (highlight) HomeColors.HighlightBg else Color.White,
    borderColor: Color = HomeColors.Border,
    borderWidth: Dp = 1.dp,
    isDashedBorder: Boolean = false,  // true면 점선, false면 실선
    dashIntervals: FloatArray = floatArrayOf(10f, 10f),  // 점선 패턴
    textBackgroundColor: Color? = null  // null이면 배경 없음
) {
    // 기존 스타일 설정
    val cardHeight = 110.dp
    val contentFontSize = 22.sp
    val labelFontSize = 10.sp
    val titleLetterSpacing = (-0.7).sp
    val labelSpacing = 13.sp
    val cardPadding = 10.dp
    val cornerRadius = 16.dp

    // Color logic for value, label, and placeholder
    val valueTextColor = if (highlight) HomeColors.OnPrimary else HomeColors.TextPrimary
    val labelTextColor = if (highlight) HomeColors.OnPrimary else HomeColors.TextSecondary
    val placeholderTextColor = if (highlight) HomeColors.OnPrimary.copy(alpha = 0.85f) else HomeColors.TextSecondary

    Box(
        modifier = modifier
            .height(cardHeight)
            .background(backgroundColor, RoundedCornerShape(cornerRadius))
            .then(
                if (isDashedBorder) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = borderColor,
                            cornerRadius = CornerRadius(cornerRadius.toPx()),
                            style = Stroke(
                                width = borderWidth.toPx(),
                                pathEffect = PathEffect.dashPathEffect(dashIntervals, 0f)
                            )
                        )
                    }
                } else {
                    Modifier.border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
                }
            )
            .padding(cardPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 내용 (값) - 데이터 없으면 안내 문구 표시
            if (value.isEmpty() || value.isBlank() || value == "-") {
                Text(
                    text = "아직 조회된\n정보가 없습니다!",
                    fontSize = 10.sp,
                    color = placeholderTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = if (textBackgroundColor != null) {
                        Modifier
                            .background(textBackgroundColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    } else {
                        Modifier
                    }
                )
            } else {
                // 3글자 초과 시 3글자까지만 표시하고 ... 추가
                val displayValue = if (value.length > 3) {
                    value.take(3) + "..."
                } else {
                    value
                }
                Text(
                    text = displayValue,
                    fontSize = contentFontSize,
                    color = valueTextColor,
                    lineHeight = labelSpacing,
                    textAlign = TextAlign.Center,
                    modifier = if (textBackgroundColor != null) {
                        Modifier
                            .background(textBackgroundColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    } else {
                        Modifier
                    }
                )
            }

            Spacer(Modifier.height(4.dp))

            // 하단 라벨 (항상 표시)
            Text(
                text = title,
                fontSize = labelFontSize,
                letterSpacing = titleLetterSpacing,
                color = labelTextColor,
                lineHeight = labelSpacing,
                textAlign = TextAlign.Center
            )
        }
    }
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
                color = HomeColors.TextPrimary,
                textAlign = TextAlign.Center
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
                        color = HomeColors.TextPrimary
                    )
                    Text(
                        text = if (scoreDiff > 0) "지난주보다 +${scoreDiff}점"
                               else if (scoreDiff < 0) "지난주보다 ${scoreDiff}점"
                               else "지난주와 동일",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (scoreDiff > 0) Color(0xFF4CAF50)
                               else if (scoreDiff < 0) Color(0xFFEF5350)
                               else HomeColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 하단 텍스트 (행동패턴 분석과 동일한 위치에 배치)
            Text(
                text = "${riskLevel.first} (${riskScore}점 이상)",
                style = MaterialTheme.typography.bodySmall,
                color = riskLevel.second,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "행동 패턴 분석",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = HomeColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarChart(
                    values = values,
                    labels = labels,
                    modifier = Modifier.size(180.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "차트가 넓게 퍼질수록 위험도가 높습니다",
                style = MaterialTheme.typography.bodySmall,
                color = HomeColors.TextSecondary,
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
                color = HomeColors.Primary.copy(alpha = 0.3f)
            )

            // 테두리
            drawPath(
                path = dataPath,
                color = HomeColors.Primary,
                style = Stroke(width = 2.dp.toPx())
            )

            // 데이터 포인트
            for (i in values.indices) {
                val angle = -PI.toFloat() / 2 + angleStep * i
                val distance = radius * values[i]
                val x = centerX + distance * cos(angle)
                val y = centerY + distance * sin(angle)
                drawCircle(
                    color = HomeColors.Primary,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
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
                    color = HomeColors.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "지난주 대비 ${kotlin.math.abs(maxChange.second)}회 ${if (isIncrease) "증가" else "감소"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HomeColors.TextSecondary
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
                color = HomeColors.TextPrimary
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
                        color = HomeColors.TextPrimary
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
                color = HomeColors.TextSecondary,
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
                color = HomeColors.TextPrimary
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
                            color = HomeColors.Primary,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // 포인트
                        data.forEachIndexed { index, value ->
                            val x = (width / (points - 1)) * index
                            val y = height - (value / maxVal * height * 0.8f)
                            drawCircle(
                                color = HomeColors.Primary,
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
                    color = HomeColors.TextSecondary
                )
                Text(
                    text = "오늘",
                    style = MaterialTheme.typography.labelSmall,
                    color = HomeColors.TextSecondary
                )
            }
        }
    }
}

// 6. 활동 vs 건강 교차 지표 (종합 인사이트)
@Composable
private fun CrossInsightCard(
    activityLog: ActivityLog,
    healthData: kr.co.ongil.data.model.health.LocalHealthData,
    modifier: Modifier = Modifier
) {
    val insights = mutableListOf<Pair<String, String>>()

    // 교차 분석
    if (activityLog.routeLost > 3 && (healthData.sleep?.average ?: 0.0) < 6.0) {
        insights.add("활동량 감소" to "수면 부족으로 인한 피로 가능성")
    }

    if (activityLog.safezoneEmer > 2 && (healthData.heartRate?.average ?: 0) > 80) {
        insights.add("이상탐지 증가" to "스트레스나 불안 가능성")
    }

    if ((healthData.steps?.average ?: 0) < 2000) {
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
                text = "확인해 보세요",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = HomeColors.TextPrimary
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
                        color = HomeColors.Primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = HomeColors.TextPrimary
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = HomeColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewHomeScreen() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState(
                guardianName = "김정희",
                patientName = "김복자",
                mostVisitedLabel = "가장 많이 방문한 목적지",
                mostVisitedPlace = "집",
                outOfSafeZoneCount = 1,
                routeFailCount = 0,
                averageSleepHours = 7.2,
                averageSteps = 5185
            )
        )
    }
}
