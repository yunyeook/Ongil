package kr.co.ongil.presentation.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.ongil.presentation.ui.common.map.CircleFloatingButton

/**
 * 지도 화면
 * - TMap 표시
 * - 도움요청 토글 버튼 (플로팅 버튼)
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier
) {
    // 도움요청 토글 상태
    var isSosEnabled by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // TMap 표시
        TMapComposable(
            modifier = Modifier.fillMaxSize()
        )

        // 플로팅 버튼들 (화면 오른쪽 하단)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // 도움요청 토글 버튼
            CircleFloatingButton(
                icon = Icons.Default.Warning,
                isToggled = isSosEnabled,
                onClick = {
                    isSosEnabled = !isSosEnabled
                    // TODO: 도움요청 토글 상태 변경 시 로직
                    if (isSosEnabled) {
                        // 도움요청 활성화
                    } else {
                        // 도움요청 비활성화
                    }
                }
            )

            // TODO: 다른 플로팅 버튼 추가 (예: 길찾기, 현재 위치, 설정 등)
            // CircleFloatingButton(
            //     icon = Icons.Default.Navigation,
            //     onClick = { /* 길찾기 */ }
            // )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MapScreen()
}
