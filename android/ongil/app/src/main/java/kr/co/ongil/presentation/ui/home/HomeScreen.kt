package kr.co.ongil.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
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
    val Primary = Color(0xFF8CA898)
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
    val routeFailCount: Int
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

        Spacer(Modifier.height(20.dp))

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

        Spacer(Modifier.height(16.dp))

        DashboardCards(uiState = uiState)
        Spacer(Modifier.height(24.dp))
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

private enum class CardStyle { Highlight, Default }

@Composable
private fun DashboardCards(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    // ===================== 카드 크기 설정 =======================
    val cardHeight = 110.dp // 카드 높이
    val cardSpacing = 12.dp // 카드 사이 간격
    // =========================================================

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing)
    ) {
        SummaryCard(
            title = uiState.mostVisitedLabel,
            value = uiState.mostVisitedPlace,
            modifier = Modifier.weight(1f),
            style = CardStyle.Highlight,
            cardHeight = cardHeight
        )

        SummaryCard(
            title = "안전구역 벗어난 횟수",
            value = "${uiState.outOfSafeZoneCount}회",
            modifier = Modifier.weight(1f),
            style = CardStyle.Default,
            cardHeight = cardHeight
        )

        SummaryCard(
            title = "길찾기 실패 횟수",
            value = "${uiState.routeFailCount}회",
            modifier = Modifier.weight(1f),
            style = CardStyle.Default,
            cardHeight = cardHeight
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    style: CardStyle = CardStyle.Default,
    cardHeight: Dp
) {
    // ================= 폰트 크기 설정 ===========================

    val contentFontSize = 22.sp           // 상단 내용 폰트 크기 ("집", "0회" 등)
    val labelFontSize = 10.sp             // 하단 라벨 폰트 크기 ("가장 많이 방문한 목적지", "안전구역 벗어난 횟수" 등)
    val titleLetterSpacing = (-0.7).sp    // 라벨 자간
    val labelSpacing = 13.sp              // 줄 간격

    // 카드 내부 여백
    val cardPadding = 10.dp             // 카드 안쪽 여백
    val cornerRadius = 16.dp            // 카드 모서리 둥글기
    // =========================================================

    val bgColor = when (style) {
        CardStyle.Highlight -> HomeColors.Primary
        CardStyle.Default -> HomeColors.CardBg
    }

    val heightModifier = modifier.height(cardHeight)

    val shapedModifier = if (style == CardStyle.Default) {
        heightModifier
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = HomeColors.Border,
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }
    } else {
        heightModifier
    }

    Surface(
        modifier = shapedModifier,
        shape = RoundedCornerShape(cornerRadius),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(cardPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 내용 (값) - 데이터 없으면 안내 문구 표시
            if (value.isEmpty() || value.isBlank()) {
                Text(
                    text = "아직 조회된\n정보가 없습니다!",
                    fontSize = 10.sp,
                    color = if (style == CardStyle.Highlight) HomeColors.OnPrimary else HomeColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
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
                    fontSize = contentFontSize, // 상단 내용 폰트 크기 ("집", "0회" 등)
                    color = if (style == CardStyle.Highlight) HomeColors.OnPrimary else HomeColors.TextPrimary,
                    lineHeight = labelSpacing,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(if (style == CardStyle.Highlight) 8.dp else 4.dp)) // 값과 라벨 사이 간격

            // 하단 라벨 (항상 표시)
            Text(
                text = title,
                fontSize = labelFontSize, // 하단 라벨 폰트 크기 ("가장 많이 방문한 목적지", "안전구역 벗어난 횟수" 등)
                letterSpacing = titleLetterSpacing,
                color = if (style == CardStyle.Highlight) HomeColors.OnPrimary else HomeColors.TextSecondary,
                lineHeight = labelSpacing,
                textAlign = TextAlign.Center
            )
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
                mostVisitedLabel = "아직 조회된 정보가 없습니다!",
                mostVisitedPlace = "집",
                outOfSafeZoneCount = 8,
                routeFailCount = 0
            )
        )
    }
}
