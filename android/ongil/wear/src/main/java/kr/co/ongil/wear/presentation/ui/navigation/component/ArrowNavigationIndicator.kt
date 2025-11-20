package kr.co.ongil.wear.presentation.ui.navigation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 화살표 네비게이션 표시 컴포넌트
 *
 * @param relativeBearing 상대 방향 각도 (0-360도, 0도 = 위쪽)
 * @param modifier Modifier
 * @param arrowColor 화살표 색상
 * @param animationDuration 애니메이션 시간 (밀리초)
 */
@Composable
fun ArrowNavigationIndicator(
    relativeBearing: Float,
    modifier: Modifier = Modifier,
    arrowColor: Color = Color.Green,
    animationDuration: Int = 300
) {
    // 화살표 회전 애니메이션
    val targetAngle = relativeBearing.toFloat()
    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "Arrow Rotation"
    )

    Canvas(modifier = modifier.size(120.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

        // 화살표 크기 설정
        val arrowLength = canvasHeight * 0.4f
        val arrowHeadWidth = canvasWidth * 0.15f
        val arrowHeadLength = canvasHeight * 0.2f

        // 화살표를 상대 각도만큼 회전
        rotate(degrees = animatedAngle, pivot = center) {
            // 화살표 경로 그리기
            val arrowPath = Path().apply {
                // 화살표 몸통 (위쪽 방향)
                moveTo(center.x, center.y - arrowLength)
                lineTo(center.x, center.y + arrowLength * 0.3f)

                // 화살표 머리 (아래쪽에서 위로)
                moveTo(center.x - arrowHeadWidth, center.y - arrowLength + arrowHeadLength)
                lineTo(center.x, center.y - arrowLength)
                lineTo(center.x + arrowHeadWidth, center.y - arrowLength + arrowHeadLength)
            }

            // 화살표 그리기
            drawPath(
                path = arrowPath,
                color = arrowColor,
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // 중심 원 그리기 (참조용)
        drawCircle(
            color = arrowColor.copy(alpha = 0.3f),
            radius = 6.dp.toPx(),
            center = center
        )
    }
}

/**
 * 방향 텍스트 표시 (선택사항)
 *
 * @param relativeBearing 상대 방향 각도 (0-360도)
 */
@Composable
fun getDirectionText(relativeBearing: Float): String {
    return when {
        relativeBearing < 22.5 || relativeBearing >= 337.5 -> "직진"
        relativeBearing < 67.5 -> "우측 앞"
        relativeBearing < 112.5 -> "우회전"
        relativeBearing < 157.5 -> "우측 뒤"
        relativeBearing < 202.5 -> "유턴"
        relativeBearing < 247.5 -> "좌측 뒤"
        relativeBearing < 292.5 -> "좌회전"
        relativeBearing < 337.5 -> "좌측 앞"
        else -> "직진"
    }
}

/**
 * 화살표 색상 결정 (거리에 따라)
 *
 * @param distanceMeters 남은 거리 (미터)
 */
@Composable
fun getArrowColor(distanceMeters: Int): Color {
    return when {
        distanceMeters < 50 -> Color.Red // 50m 이하: 빨강 (주의)
        distanceMeters < 100 -> Color.Yellow // 100m 이하: 노랑
        else -> Color.Green // 100m 이상: 초록
    }
}
