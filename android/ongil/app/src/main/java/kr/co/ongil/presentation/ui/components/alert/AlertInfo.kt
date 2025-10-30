package kr.co.ongil.presentation.ui.components.alert

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
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
fun AlertInfo(
    title: String,
    content: String,
    timeText: String,
    modifier: Modifier = Modifier       // 타입 이런거 나중에 다 넣어야됨. 근데 수신시각을 기준으로 n분전 이런거 구분할 수 있나?
    // 그리고 그 뭐냐.. 그.. 수신확인은 뭐예요..?
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = Color.Gray, shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFF111827),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )

            content.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    color = Color(0xFF4B5563),
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = timeText,
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AlertInfoPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AlertInfo(
            title = "안전구역 이탈 감지",
            content = "보호 대상자가 지정된 안전 구역에서 벗어났습니다.",
            timeText = "5분 전"
        )
        Divider() // 디바이더는 이런 식으로 넣으면 됨
        AlertInfo(
            title = "새로운 소식",
            content = "새로운 기능이 추가되었습니다. 확인해보세요.",
            timeText = "1일 전"
        )
    }
}