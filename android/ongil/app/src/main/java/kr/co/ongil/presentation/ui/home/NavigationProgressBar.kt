package kr.co.ongil.presentation.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import kr.co.ongil.presentation.theme.ongilColors
import kotlin.math.roundToInt

/**
 * 길안내 진행 상황을 보여주는 프로그레스 바
 *

 * @param endLocation 목적지 이름
 * @param time 소요 시간
 * @param progress 진행률 (0.0 ~ 1.0)
 * @param userType 사용자 타입 (PATIENT or GUARDIAN)
 * @param modifier Modifier
 */
@Composable
fun NavigationProgressBar(
    endLocation: String,
    time: String,
    progress: Float,
    userType: String = "GUARDIAN",
    modifier: Modifier = Modifier
) {
    // 사용자 타입에 따라 색상 결정
    val backgroundColor = if (userType.uppercase() == "PATIENT") {
        Color(0xFFB8B9D4)  // 환자용 연한 보라색
    } else {
        Color(0xFFADC1B2)  // 보호자용 초록색
    }

    val progressColor = if (userType.uppercase() == "PATIENT") {
        Color(0xFF5F71CE)  // 환자용 진한 보라색
    } else {
        Color(0xFF5C7165)  // 보호자용 진한 초록색
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        InfoColumn(
            title = "목적지",
            value = endLocation,
            modifier = Modifier.weight(1f)
        )

        InfoColumn(
            title = "소요시간",
            value = time,
            modifier = Modifier.weight(1f)
        )

        Column(
            modifier = Modifier.weight(1.5f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    val barHeight = size.height
                    drawRoundRect(
                        color = Color.White,
                        size = Size(size.width, barHeight),
                        cornerRadius = CornerRadius(barHeight / 2f)
                    )
                    if (progress > 0f) {
                        drawRoundRect(
                            color = progressColor,
                            size = Size(size.width * progress.coerceIn(0f, 1f), barHeight),
                            cornerRadius = CornerRadius(barHeight / 2f)
                        )
                    }
                }

                val iconAlignment = remember(progress) {
                    object : Alignment {
                        override fun align(
                            size: IntSize,
                            space: IntSize,
                            layoutDirection: LayoutDirection
                        ): IntOffset {
                            val x = ((space.width - size.width) * progress.coerceIn(0f, 1f)).roundToInt()
                            val y = 0  // 상단에 배치
                            return IntOffset(x, y)
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = "현재 위치",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .align(iconAlignment)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "출발지", fontSize = 10.sp, color = Color.White)
                Text(text = "목적지", fontSize = 10.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun InfoColumn(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth(0.9f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.basicMarquee(
                    animationMode = MarqueeAnimationMode.Immediately,
                    repeatDelayMillis = 1000,
                    initialDelayMillis = 2000,
                    velocity = 30.dp
                )
            )
        }
    }
}
