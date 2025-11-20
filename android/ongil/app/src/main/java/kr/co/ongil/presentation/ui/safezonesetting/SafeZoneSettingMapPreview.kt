package kr.co.ongil.presentation.ui.safezonesetting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * 안전구역 지도 미리보기 컴포넌트
 * 각 레벨의 거리 값에 따라 동적으로 원 크기 조절
 */
@Composable
fun SafeZoneMapPreview(
    level1Radius: Int,
    level2Radius: Int,
    level3Radius: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F7))
        ) {
            // 동적 원 크기 계산 (실제 거리 값 기반)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.minDimension / 2.2f

                // 최대 거리(level3)를 기준으로 정규화
                val maxDistance = level3Radius.toFloat()

                // 각 레벨의 실제 비율 계산
                val radius3 = maxRadius * (level3Radius / maxDistance)
                val radius2 = maxRadius * (level2Radius / maxDistance)
                val radius1 = maxRadius * (level1Radius / maxDistance)

                // 3단계 (가장 바깥쪽, 핑크)
                drawCircle(
                    color = Color(0x33E91E63),
                    radius = radius3,
                    center = center
                )
                // 2단계 (중간, 파랑)
                drawCircle(
                    color = Color(0x332196F3),
                    radius = radius2,
                    center = center
                )
                // 1단계 (가장 안쪽, 초록)
                drawCircle(
                    color = Color(0x334CAF50),
                    radius = radius1,
                    center = center
                )
                // 중앙점 - 채워진 초록색 원
                drawCircle(
                    color = Color(0xFF87A293),  // 초록색
                    radius = 8.dp.toPx(),
                    center = center
                )
            }
        }
    }
}

/**
 * 안전구역 안내 텍스트
 */
@Composable
fun SafeZoneAssistiveText(
    modifier: Modifier = Modifier,
    text: String = "야간에는 설정값보다 탐지 범위가 줄어듭니다."
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280)
        )
    }
}
