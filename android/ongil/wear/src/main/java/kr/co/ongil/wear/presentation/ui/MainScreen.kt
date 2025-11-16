package kr.co.ongil.wear.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.co.ongil.wear.presentation.ui.map.WearTMapComposable

/**
 * 로그인 후 메인 화면
 *
 * 지도 전체 화면 표시
 * TODO: 실제 지도 기능 구현 시 MapScreen으로 교체 예정
 *
 * @param userId 사용자 ID (선택)
 * @param userType 사용자 타입 (PATIENT or GUARDIAN, 선택)
 */
@Composable
fun MainScreen(
    userId: String? = null,
    userType: String? = null
) {
    // 지도 전체 화면
    WearTMapComposable(
        modifier = Modifier.fillMaxSize()
    )
}