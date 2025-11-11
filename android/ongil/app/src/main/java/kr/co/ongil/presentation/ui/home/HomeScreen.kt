package kr.co.ongil.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
    val mostVisitedLabel: String, // 예: "가장 많이 방문한 목적지"
    val mostVisitedPlace: String, // 예: "집"
    val outOfSafeZoneCount: Int,  // 안전구역 벗어난 횟수
    val routeFailCount: Int       // 길찾기 실패 횟수
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 120.dp // 여기를 수정하여 모든 카드 높이를 조절하세요
) {
    // 헤더는 외부에서 제공. 여기서는 본문만 구성.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        Text(
            text = "안녕하세요, ${uiState.guardianName} 님",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "${uiState.patientName}님의 위치를 찾아볼까요:)",
            style = MaterialTheme.typography.bodyMedium,
            color = HomeColors.TextSecondary
        )

        Spacer(Modifier.height(12.dp))

        // 지도 섹션 플레이스홀더(큰 네모 박스)
        MapSectionPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(Modifier.height(20.dp))

        // 대시보드 시작: “최근 정보를 요약했어요”
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

        DashboardCards(uiState = uiState, cardHeight = 160.dp)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MapSectionPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HomeColors.CardBg)
            .semantics {
                contentDescription = "지도 영역 플레이스홀더"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "지도 영역",
            style = MaterialTheme.typography.titleMedium,
            color = HomeColors.TextSecondary
        )
    }
}

private enum class CardStyle { Highlight, Default }

@Composable
private fun DashboardCards(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    cardHeight: Dp
) {
    // 한 줄에 3개, 반응형으로 weight 분배
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = uiState.mostVisitedLabel,
            value = uiState.mostVisitedPlace,
            leading = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HomeColors.Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = HomeColors.Primary
                    )
                }
            },
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
    val bgColor = when (style) {
        CardStyle.Highlight -> HomeColors.HighlightBg
        CardStyle.Default -> HomeColors.CardBg
    }

    val heightModifier = modifier.height(cardHeight)

    val shapedModifier = if (style == CardStyle.Default) {
        heightModifier
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = HomeColors.Border,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
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
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            if (style == CardStyle.Highlight) {
                if (leading != null) leading()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = HomeColors.TextSecondary
                )
                Surface(
                    color = HomeColors.Primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = value,
                        color = HomeColors.OnPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = HomeColors.TextPrimary,
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = HomeColors.TextSecondary
                )
            }
        }
    }
}

/* ============================== Previews ============================== */

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
                outOfSafeZoneCount = 8,
                routeFailCount = 0
            ),
            cardHeight = 100.dp // 이 값을 변경하여 프리뷰에서 카드 높이를 테스트할 수 있습니다.
        )
    }
}
