package kr.co.ongil.presentation.ui.safezonesetting

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 안전구역 레벨 설정 섹션 (1, 2, 3단계)
 */
@Composable
fun SafeZoneLevelSection(
    title: String,
    color: Color,
    distanceMeters: Int,
    dwellMinutes: Int,
    distanceRange: IntRange,
    dwellRange: IntRange,
    onDistanceChange: (Int) -> Unit,
    onDwellChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFEFEF)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${distanceMeters}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text("안전 거리")
            SliderWithEdges(
                value = distanceMeters.toFloat(),
                onValueChange = { onDistanceChange(it.toInt()) },
                valueRange = distanceRange.first.toFloat()..distanceRange.last.toFloat(),
                startLabel = "${distanceRange.first}m",
                endLabel = "${distanceRange.last}m",
                activeColor = color,
                inactiveColor = color.copy(alpha = 0.25f)
            )

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("배회 탐지 시간", modifier = Modifier.weight(1f))
                Text(
                    "${dwellMinutes}분",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF6B7280)
                )
            }
            SliderWithEdges(
                value = dwellMinutes.toFloat(),
                onValueChange = { onDwellChange(it.toInt()) },
                valueRange = dwellRange.first.toFloat()..dwellRange.last.toFloat(),
                startLabel = "${dwellRange.first}분",
                endLabel = "${dwellRange.last}분",
                activeColor = color,
                inactiveColor = color.copy(alpha = 0.25f)
            )
        }
    }
}

/**
 * 슬라이더 with 라벨
 */
@Composable
private fun SliderWithEdges(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    startLabel: String,
    endLabel: String,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
    thumbColor: Color = Color.White
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 슬라이더를 Box로 감싸서 터치 이벤트 처리 - 패딩 추가로 터치 영역 확보
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                // 터치 이벤트를 감지하여 부모 스크롤 방지
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            // 터치를 감지하여 이벤트 소비
                            tryAwaitRelease()
                        }
                    )
                }
        ) {
            Slider(
                value = value.coerceIn(valueRange.start, valueRange.endInclusive),
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    activeTrackColor = activeColor,
                    inactiveTrackColor = inactiveColor,
                    thumbColor = thumbColor
                )
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                startLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280)
            )
            Text(
                endLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280)
            )
        }
    }
}
