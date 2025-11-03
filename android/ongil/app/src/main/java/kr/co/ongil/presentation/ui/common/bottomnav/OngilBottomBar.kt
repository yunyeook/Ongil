package kr.co.ongil.presentation.ui.common.bottomnav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.navigation.BottomNavItem
import kr.co.ongil.presentation.navigation.OngilBottomNavItems
import kr.co.ongil.presentation.theme.OngilIconSelected
import kr.co.ongil.presentation.theme.OngilIconUnselected

/**
 * 온길 앱의 공통 하단 네비게이션 바
 *
 * @param selectedRoute 현재 선택된 route
 * @param onClick 탭 클릭 콜백
 * @param modifier 외부에서 적용할 Modifier
 * @param items 네비게이션 아이템 리스트 (기본값: OngilBottomNavItems.all)
 */
@Composable
fun OngilBottomBar(
    selectedRoute: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = OngilBottomNavItems.all
) {
    // 카드는 상단 모서리만 둥글게, 하단은 직각, 좌우 패딩 없음
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(
            topStart = 40.dp,
            topEnd = 40.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 22.dp, bottom = 5.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.route == selectedRoute
                BottomBarItem(
                    label = item.label,
                    selected = selected,
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    },
                    onClick = { onClick(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 개별 탭 아이템 (아이콘 위, 라벨 아래)
 */
@Composable
private fun BottomBarItem(
    label: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint = if (selected) OngilIconSelected else OngilIconUnselected
    val textColor = iconTint

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .noRippleClickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(35.dp),
            contentAlignment = Alignment.Center
        ) {
            // 아이콘 색상 적용
            CompositionLocalProvider(LocalContentColor provides iconTint) {
                icon()
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

/**
 * 클릭 리플 없애는 확장 함수
 * 원하면 리플로 바꿔도 됨
 */
private fun Modifier.noRippleClickable(onClick: () -> Unit) = this
    .semantics { role = Role.Button }
    .clickable(
        indication = null,
        interactionSource = MutableInteractionSource()
    ) {
        onClick()
    }
