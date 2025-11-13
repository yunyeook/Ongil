package kr.co.ongil.presentation.ui.auth

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,          // 로그인 성공 시 이동
    onClickFindPw: () -> Unit,
    onClickSignup: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    // 외부에서 주입되면 그것을 우선 사용, 아니면 내부 기본값 사용
    val internalHost = remember { SnackbarHostState() }
    val host = snackbarHostState ?: internalHost

    // 1회성 효과 처리 (네비게이션 / 스낵바)
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { eff ->
            when (eff) {
                is LoginEffect.NavigateHome -> onLoginSuccess()
                is LoginEffect.ShowSnack    -> scope.launch { host.showSnackbar(eff.message) }
            }
        }
    }

    // ✅ Scaffold 추가 (SnackbarHost가 포함됨)
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = host) }
    ) { paddingValues ->
        // paddingValues는 Scaffold의 inset을 처리하기 위해 사용
        LoginScreen(
            state = state,
            onPhoneChange = viewModel::onPhoneChange,
            onPasswordChange = viewModel::onPasswordChange,
            onLoginClick = viewModel::onClickLogin,
            onClickFindPw = onClickFindPw,
            onClickSignup = onClickSignup
        )
    }

}
