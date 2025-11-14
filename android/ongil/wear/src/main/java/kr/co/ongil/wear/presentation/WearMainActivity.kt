package kr.co.ongil.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kr.co.ongil.wear.presentation.theme.OngilTheme
import kr.co.ongil.wear.presentation.ui.LoginSyncScreen
import kr.co.ongil.wear.presentation.ui.MainScreen
import kr.co.ongil.wear.presentation.viewmodel.WearAuthViewModel

/**
 * 워치 메인 Activity
 *
 * 스프링의 @Controller 메인 엔트리포인트와 비슷
 * - 로그인 상태에 따라 화면 전환
 * - LoginSyncScreen ↔ MainScreen
 */
@AndroidEntryPoint  // Hilt 사용
class WearMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 스플래시 화면 설치
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // 테마 설정
        setTheme(android.R.style.Theme_DeviceDefault)

        // UI 설정
        setContent {
            WearApp()
        }
    }
}

/**
 * 워치 앱 메인 Composable
 *
 * 로그인 상태에 따라 화면 분기
 */
@Composable
fun WearApp(
    viewModel: WearAuthViewModel = viewModel()  // ViewModel 주입
) {
    // 테마 적용
    OngilTheme {
        // ViewModel 상태 관찰
        val isLoggedIn by viewModel.isLoggedIn.collectAsState()
        val userId by viewModel.userId.collectAsState()
        val userType by viewModel.userType.collectAsState()

        // 로그인 상태에 따라 화면 분기
        if (isLoggedIn) {
            // 로그인됨 → 메인 화면
            MainScreen(
                userId = userId,
                userType = userType
            )
        } else {
            // 비로그인 → 동기화 대기 화면
            LoginSyncScreen()
        }
    }
}