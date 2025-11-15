package kr.co.ongil.presentation.ui.common.patientinfo

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


sealed interface StatValue {
    data class Text(val value: String) : StatValue
    data class Icon(val icon: ImageVector, val tint: Color = Color(0xFF101828)) : StatValue
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    title: String,
    stats: List<Pair<String, StatValue>>
) {
    val mainTextColor = Color(0xFF2A2F2E)
    val subTextColor = Color(0xFF6B767A)
    val iconBgColor = Color(0xFF8CA898)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        border = null,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIcon(
                    bgColor = iconBgColor,
                    diameter = 24.dp,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    color = mainTextColor,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 하단 3열 통계
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stats.forEach { (label, value) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            color = subTextColor,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        when (value) {
                            is StatValue.Text -> {
                                Text(
                                    text = value.value,
                                    color = mainTextColor,
                                    fontSize = 19.sp,
                                    lineHeight = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            is StatValue.Icon -> {
                                Icon(
                                    imageVector = value.icon,
                                    contentDescription = label,
                                    tint = value.tint,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 여기 아이콘 들어가면 됨. 영역만 잡아둠.
@Composable
fun CircleIcon(
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
            "집" to StatValue.Text("13회"), // "${place}" to "${count}회" 이런식으로 쓰면 됨.
            "편의점" to StatValue.Text("10회"),
            "마트" to StatValue.Text("8회"),
            "양로원" to StatValue.Text("5회")
        )
    )
}