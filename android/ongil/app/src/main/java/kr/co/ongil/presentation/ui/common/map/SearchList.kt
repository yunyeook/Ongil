package kr.co.ongil.presentation.ui.common.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import kr.co.ongil.presentation.theme.ongilColors


@Composable
fun SearchListItem(
    placeName: String,
    address: String,
    etaText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
//            .border(
//                width = 1.dp,
//                color = Color(0xFFE5E7EB),              // 또는 ongilColors.borderLight
//                shape = RoundedCornerShape(12.dp)        // radius 값
//            )
//            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp)) // 전체 아이템 모서리 살짝 둥글게
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 위치 핀 아이콘 영역 (동그란 연한 회색 배경)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "위치 아이콘",
                        tint = ongilColors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 장소명 + 주소
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = placeName,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = address,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF6B7280),
                        maxLines = 1
                    )
                }
            }


        }

        // 구분선 (아이콘 시작 위치에 맞춰서 안쪽으로)
        HorizontalDivider(
            modifier = Modifier.padding(start = 64.dp, end = 16.dp),
            thickness = 0.5.dp,
            color = Color(0xFFE5E7EB)
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
fun SearchListItemPreview() {
    Surface(
        color = Color(0xFF1E1E1E),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            SearchListItem(
                placeName = "도곡지구대",
                address = "서울 강남구 언주로 426 (역삼동)",
                etaText = "15분",
                onClick = {}
            )

            SearchListItem(
                placeName = "강남세브란스병원",
                address = "서울 강남구 언주로 211",
                etaText = "8분",
                onClick = {}
            )
        }
    }
}
