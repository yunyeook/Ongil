package kr.co.ongil.presentation.ui.common.favorite

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
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
fun PlaceCard(
    name: String,
    address: String,
    isDefault: Boolean,
    onClickCard: () -> Unit,
    onClickIcon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ongilColors

    val borderColor = if (isDefault) {
        colors.accent.copy(alpha = 0.5f)      // 기본 목적지 → 살짝 진한 테두리
    } else {
        Color(0xFFE5E7EB)                     // 일반 카드 → 연한 회색 테두리
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickCard() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDefault) {
                colors.accent.copy(alpha = 0.08f) // 은은한 배경 하이라이트
            } else {
                Color.White
            }
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp              // 그림자 대신 테두리만
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (isDefault) {
                    Text(
                        text = "기본 목적지",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.accent,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = address,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { onClickIcon() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = "위치 아이콘",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
fun PlaceCardPreviewGuardian() {
    OngilThemeProvider(userType = "GUARDIAN") {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = Color(0xFFF9FAFB)
        ) {
            PlaceCard(
                name = "이마트 중계점",
                address = "서울시 노원구 중계로 235",
                isDefault = true,
                onClickCard = {},
                onClickIcon = {}
            )
        }
    }
}
