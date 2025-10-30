package kr.co.ongil.presentation.ui.components.patientinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    title: String,
    stats: List<Pair<String, String>>
) {
    val borderColor = Color(0xFFD9D9D9)
    val mainTextColor = Color(0xFF101828)
    val subTextColor = Color(0xFF99A1AF)
    val iconBgColor = Color(0xFF5C7165)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    bgColor = iconBgColor,
                    diameter = 20.dp,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    color = mainTextColor,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 하단 3열 통계
            Row(
                modifier = Modifier.fillMaxWidth(0.8f) .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceBetween,

            ) {
                stats.forEach { (label, value) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            color = subTextColor,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = value,
                            color = mainTextColor,
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// 여기 아이콘 들어가면 됨. 영역만 잡아둠.
@Composable
fun Icon(
    bgColor: Color,
    diameter: Dp,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier.size(diameter)
    ) {
        drawCircle(color = bgColor)
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFEDEDED
)
@Composable
fun InfoCardPreview() {
    InfoCard(
        modifier = Modifier
            .width(327.dp)
            .height(142.dp),
        cornerRadius = 24.dp,
        title = "가장 많이 찾은 목적지",
        stats = listOf(
            "집" to "13회", // "${place}" to "${count}회" 이런식으로 쓰면 됨.
            "편의점" to "10회",
            "마트" to "8회",
            "양로원" to "5회"
        )
    )
}