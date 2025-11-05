// presentation/ui/auth/LoginRoute.kt
package kr.co.ongil.presentation.ui.auth

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,  // 로그인 성공 후 호출될 함수
    onClickFindPw: () -> Unit,
    onClickSignup: () -> Unit,
    onClickKakao: () -> Unit = {},
    onClickGoogle: () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    viewModel: LoginViewModel = hiltViewModel()  // ViewModel 주입
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    // 1. 1회성 효과(네비게이션/스낵바) 수신 처리
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { eff ->
            when (eff) {
                is LoginEffect.NavigateHome -> onLoginSuccess()  // 성공하면 홈으로 네비
                is LoginEffect.ShowSnack -> snackbarHostState?.let { host -> // 에러 메시지 수신
                    scope.launch { host.showSnackbar(eff.message) }
                }
            }
        }
    }

    // 2. LoginScreen에서 상태와 이벤트를 받아 처리
    LoginScreen(
        state = state,
        onPhoneChange = viewModel::onPhoneChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onClickLogin,
        onClickFindPw = onClickFindPw,
        onClickSignup = onClickSignup,
        onClickKakao = onClickKakao,
        onClickGoogle = onClickGoogle
    )
}
