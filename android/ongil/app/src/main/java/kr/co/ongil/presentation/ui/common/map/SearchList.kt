package kr.co.ongil.presentation.ui.common.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SearchListItem(
    placeName: String,
    address: String,
    etaText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            // 위치 핀 아이콘 영역
            // 실제 디자인은 회색(#9CA3AF) 단색 아이콘. 지금은 기본 Place 아이콘을 tint로
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = "위치 아이콘", // 이거 스크린리더가 읽어야된대요
                tint = Color(0xFF9CA3AF),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 장소명 + 주소
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = placeName,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = address,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    maxLines = 1
                )
            }
        }

        Text(
            text = etaText,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            modifier = Modifier
                .padding(start = 12.dp)
                .align(Alignment.Top)
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
                .background(Color.White)
        ) {
            SearchListItem(
                placeName = "도곡지구대",
                address = "서울 강남구 언주로 426 (역삼동)",
                etaText = "15분",
                onClick = {}
            )
        }
    }
}