package kr.co.ongil.presentation.ui.common.map

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 지도에서 사용하는 원형 플로팅 버튼 공통 컴포넌트
 *
 * @param icon 버튼 내부에 표시할 아이콘
 * @param onClick 버튼 클릭 이벤트
 * @param isToggled 토글 상태 (true면 활성화 색상)
 * @param modifier Modifier
 * @param size 버튼 크기 (기본 56dp)
 * @param containerColor 배경색 (기본: 토글 OFF - White, ON - Red)
 * @param contentColor 아이콘 색상 (기본: 토글 OFF - Black, ON - White)
 */
@Composable
fun CircleFloatingButton(
    icon: ImageVector,
    onClick: () -> Unit,
    isToggled: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val defaultContainerColor = if (isToggled) Color(0xFFE53935) else Color.White
    val defaultContentColor = if (isToggled) Color.White else Color.Black

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = CircleShape,
        containerColor = containerColor ?: defaultContainerColor,
        contentColor = contentColor ?: defaultContentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
