package kr.co.ongil.presentation.ui.common.patientinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.theme.GuardianColors
import kr.co.ongil.presentation.theme.PatientColors

sealed interface StatValue {
    data class Text(val value: String) : StatValue
    data class Icon(val icon: ImageVector, val tint: Color = Color(0xFF101828)) : StatValue
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    title: String,
    stats: List<Pair<String, StatValue>>,
    userType: String = "GUARDIAN"
) {
    val themeColors = if (userType == "PATIENT") PatientColors else GuardianColors
    val mainTextColor = Color(0xFF2A2F2E)
    val subTextColor = Color(0xFF6B767A)
    val accentColor = themeColors.accent

    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 왼쪽 컬러 바
                .drawBehind {
                    val barWidth = 4.dp.toPx()
                    drawRect(
                        color = accentColor,
                        topLeft = Offset.Zero,
                        size = Size(barWidth, size.height)
                    )
                }
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
        ) {
            // 상단 타이틀
            Text(
                text = title,
                color = mainTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 하단 통계 - 균등하게 배치
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                stats.forEach { (label, value) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (value) {
                            is StatValue.Text -> {
                                Text(
                                    text = value.value,
                                    color = accentColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            is StatValue.Icon -> {
                                Icon(
                                    imageVector = value.icon,
                                    contentDescription = label,
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = label,
                            color = subTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
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
            .width(320.dp)
            .height(120.dp),
        cornerRadius = 16.dp,
        title = "가장 많이 찾은 목적지",
        stats = listOf(
            "집" to StatValue.Text("13회"),
            "편의점" to StatValue.Text("10회"),
            "마트" to StatValue.Text("8회"),
            "양로원" to StatValue.Text("5회")
        )
    )
}
