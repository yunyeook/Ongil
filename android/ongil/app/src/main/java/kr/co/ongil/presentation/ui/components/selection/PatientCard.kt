package kr.co.ongil.presentation.ui.components.patient

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PatientCard(
    name: String,
    position: String,               // ex) "집", "이동중"
    profileImageUrl: String? = null, // 이미지 로딩은 안 하고 null이면 이니셜 보여줌 여기서는 성 정도만 보여주는거 어떤지
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = Color(0xFF5C7165)
    val bgColor = if (isSelected) Color(0xFF5C7165) else Color.Transparent
    val nameColor = if (isSelected) Color.White else Color(0xFF1F2937)
    val positionColor = if (isSelected) Color.White else Color(0xFF5C7165)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (profileImageUrl == null)
                            Color(0xFF053a20)                   // 기본 원 배경색 몰라요 프로필 안올려둘수도있으니까 그냥 일단 아무색이나 해둠
                        else
                            Color.LightGray
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl == null) {
                    val initial = name.firstOrNull()?.toString() ?: ""
                    Text(
                        text = initial,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                } else {
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    color = nameColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp
                )
                Text(
                    text = position,
                    color = positionColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PatientCardPreview_Unselected_NoImage() {
    PatientCard(
        name = "황경례",
        position = "집",
        profileImageUrl = null,
        isSelected = false,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PatientCardPreview_Selected_NoImage() {
    PatientCard(
        name = "황경례",
        position = "이동중",
        profileImageUrl = null,
        isSelected = true,
        onClick = {}
    )
}