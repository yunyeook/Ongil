package kr.co.ongil.presentation.ui.common.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun PatientCard(
    name: String,
    profileImageUrl: String? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF5C7165)
    val borderColor = if (isSelected) primaryColor else Color(0xFFE5E7EB)
    val bgColor = if (isSelected) Color(0xFFE9F0EC) else Color.White
    val nameColor = if (isSelected) primaryColor else Color(0xFF111827)
    val subTextColor = if (isSelected) Color(0xFF71817A) else Color(0xFF6B7280)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp) // 🔹 높이 줄임
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ▣ 프로필 원형 영역
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        brush = if (profileImageUrl == null) {
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF6F8578),
                                    Color(0xFF4B6355)
                                )
                            )
                        } else {
                            Brush.linearGradient(listOf(Color.LightGray, Color.LightGray))
                        }
                    )
                ,
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl == null) {
                    val initial = name.firstOrNull()?.toString() ?: ""
                    Text(
                        text = initial,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                } else {
                    // TODO: Coil AsyncImage 같은 걸로 프로필 이미지 로딩
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ▣ 텍스트 부분
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = name,
                    color = nameColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "환자",
                    color = subTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // ▣ 선택 상태 배지 (텍스트 대신 작은 포인트)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF5C7165))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "선택됨",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PatientCardPreview_Unselected() {
    PatientCard(
        name = "황경례",
        profileImageUrl = null,
        isSelected = false,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PatientCardPreview_Selected() {
    PatientCard(
        name = "황경례",
        profileImageUrl = null,
        isSelected = true,
        onClick = {}
    )
}
