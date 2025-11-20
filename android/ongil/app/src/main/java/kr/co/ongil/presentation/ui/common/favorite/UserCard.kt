package kr.co.ongil.presentation.ui.common.favorite

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import kr.co.ongil.presentation.theme.OngilThemeProvider
import kr.co.ongil.presentation.theme.ongilColors

@Composable
fun PatientCard(
    patientName: String,
    phoneNumber: String,
    isDefault: Boolean,
    userType: String,
    onClickCard: () -> Unit,
    onClickCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ongilColors

    // ● 기본 카드인지 여부에 따라 테두리 색상 변화
    val borderColor = if (isDefault) {
        colors.accent.copy(alpha = 0.5f)
    } else {
        Color(0xFFE5E7EB)     // 연한 gray-200 느낌
    }

    // ● 기본 카드일 때 배경을 accent의 아주 연한 톤으로
    val backgroundColor = if (isDefault) {
        colors.accent.copy(alpha = 0.08f)
    } else {
        Color.White
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickCard() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // 그림자 제거 → 두꺼운 박스 느낌 사라짐
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {

                // ● 기본 보호자 / 기본 환자 라벨
                if (isDefault) {
                    Text(
                        text = if (userType.uppercase() == "PATIENT") "기본 보호자" else "기본 환자",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.accent,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = patientName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = phoneNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ● 전화 버튼은 테마 accent 색상
            IconButton(
                onClick = { onClickCall() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "call button",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
fun PatientCardPreviewGuardian() {
    OngilThemeProvider(userType = "GUARDIAN") {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = Color(0xFFF9FAFB)
        ) {
            PatientCard(
                patientName = "우리 보호자",
                phoneNumber = "010-1234-5678",
                isDefault = true,
                userType = "GUARDIAN",
                onClickCard = {},
                onClickCall = {}
            )
        }
    }
}
