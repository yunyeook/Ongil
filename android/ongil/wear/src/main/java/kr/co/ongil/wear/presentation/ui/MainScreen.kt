package kr.co.ongil.wear.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.co.ongil.wear.presentation.ui.map.WearTMapComposable

/**
 * 로그인 후 메인 화면
 *
 * 지도 전체 화면 표시
 *
 * @param userId 사용자 ID
 * @param userType 사용자 타입 (PATIENT or GUARDIAN)
 */
@Composable
fun MainScreen(
    userId: String?,
    userType: String?
) {
    // 지도 전체 화면
    WearTMapComposable(
        modifier = Modifier.fillMaxSize()
    )
}