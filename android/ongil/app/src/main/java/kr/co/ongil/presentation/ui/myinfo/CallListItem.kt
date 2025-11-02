package kr.co.ongil.presentation.ui.myinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kr.co.ongil.presentation.uistate.CallType
import kr.co.ongil.presentation.uistate.RecentCallUi

private val Accent = Color(0xFF8CA898)

/**
 * 통화 목록 아이템
 */
@Composable
fun CallListItem(
    item: RecentCallUi,
    onCallClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (item.type) {
        CallType.SOS -> Color(0xFFF6F4F0)        // SOS 연한 강조
        CallType.VOIP -> Color(0xFFF2F6F4)       // 살짝 민트 톤
        CallType.NORMAL -> Color(0xFFF7F9F8)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // 아바타(이니셜)
            CallAvatar(nameOrNumber = item.nameOrNumber)

            Spacer(Modifier.width(14.dp))

            // 정보 영역
            CallInfo(
                item = item,
                modifier = Modifier.weight(1f)
            )

            // 우측 아이콘들
            CallActions(
                onCallClick = onCallClick,
                onInfoClick = onInfoClick
            )
        }
    }
}

/**
 * 통화 아바타 (이니셜)
 */
@Composable
private fun CallAvatar(
    nameOrNumber: String,
    modifier: Modifier = Modifier
) {
    val initials = nameOrNumber
        .filter { it.isLetter() || it.isDigit() }
        .take(2)

    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0xFF7E8C87)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}

/**
 * 통화 정보 (이름, 타입, 서브타이틀)
 */
@Composable
private fun CallInfo(
    item: RecentCallUi,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = item.nameOrNumber,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF243033)
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val typeText = when (item.type) {
                CallType.VOIP -> "VoIP"
                CallType.NORMAL -> "일반전화"
                CallType.SOS -> "SOS"
            }
            val typeColor = when (item.type) {
                CallType.SOS -> Color(0xFFD85B4E)
                else -> Color(0xFF6B767A)
            }
            Text(
                text = "↗ $typeText",
                color = typeColor,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.subtitle,
                color = Color(0xFF98A2A6),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * 통화 액션 버튼들 (통화, 상세)
 */
@Composable
private fun CallActions(
    onCallClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.Call,
            contentDescription = "통화",
            tint = Accent,
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onCallClick)
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "상세",
            tint = Color(0xFF8F9A9E),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onInfoClick)
        )
    }
}
