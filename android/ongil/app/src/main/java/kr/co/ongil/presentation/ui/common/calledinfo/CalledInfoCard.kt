package kr.co.ongil.presentation.ui.common.calledinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalledInfoCard(
    name: String,
    phoneNumber: String,
    callTimeLabel: String,
    callDurationLabel: String,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profileText = name.takeLast(2)             // 이름 뒤에부터 두 글자 가져오는게 더 낫지않나?
    val containerColor = Color(0xFFE0E3E1)      // 카드 배경색
    val nameColor = Color(0xFF101828)           // 진한 텍스트
    val subTextColor = Color(0xFF6B7280)        // 회색 텍스트
    val accentColor = Color(0xFF059669)         // 초록색 아이콘/화살표

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = {
            onCardClick()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 왼쪽 동그라미 프로필
            ProfileCircle(
                text = profileText,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 가운데 텍스트 영역 (이름, 번호, 시간들)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 초록 화살표 - 아이콘으로 교체 할지말지 모르겠음.
                    Text(
                        text = "↗",
                        color = accentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = name,
                        color = nameColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = phoneNumber,
                    color = subTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$callTimeLabel · $callDurationLabel",
                    color = subTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            CallButtonIcon(
                tint = accentColor,
                onClick = onCallClick
            )
        }
    }
}

@Composable
private fun ProfileCircle(
    text: String,
    modifier: Modifier = Modifier
) {
    // 동그란 배경 안에 흰 글씨
    Box(
        modifier = modifier
            .background(
                color = Color(0xFF5C7165),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CallButtonIcon(
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 일단 기본 Phone 아이콘 사용 (Material Icons)
    IconButton(
        onClick = onClick,
        modifier = modifier.size(32.dp) // 터치 영역 어느 정도 확보
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = "call",
            tint = tint
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CalledInfoCardPreview() {
    CalledInfoCard(
        name = "홍길동",
        phoneNumber = "010-1234-5678",
        callTimeLabel = "2023. 09." ,
        callDurationLabel ="12:34",
        onCardClick = {},
        onCallClick = {}
    )
}