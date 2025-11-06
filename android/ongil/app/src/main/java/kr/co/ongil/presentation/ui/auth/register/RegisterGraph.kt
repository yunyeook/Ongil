package kr.co.ongil.presentation.ui.auth.register

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.co.ongil.presentation.navigation.Routes

/**
 * Register flow navigation graph.
 * UI는 변경하지 않고, 여기서 ViewModel 바인딩만 수행합니다.
 */
fun NavGraphBuilder.registerGraph(
    navController: NavHostController,
    route: String = "register_graph",
    startDestination: String = RegisterDest.REGISTER
) {
    navigation(startDestination = startDestination, route = route) {
        composable(RegisterDest.REGISTER) {
            RegisterRoute(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    // 성공 시: 회원가입 스택 제거하고 로그인 화면으로 이동
                    navController.navigate(Routes.Login.route) {
                        popUpTo(route) { inclusive = true }
                    }
                }
            )
        }
    }
}

object RegisterDest {
    const val REGISTER = "register"
}

@Composable
private fun RegisterRoute(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    RegisterScreen(
        uiState = uiState,
        onBackClick = onBack,
        onProfileImageClick = {
            // 이미지 경로를 픽커로부터 받은 후 아래 이벤트로 전달하세요
            // viewModel.onEvent(RegisterUiEvent.OnProfileImagePathChange(path))
        },
        onNameChange = { viewModel.onEvent(RegisterUiEvent.OnNameChange(it)) },
        onBirthClick = { viewModel.onEvent(RegisterUiEvent.OnShowDatePicker) },
        onSetBirth = { yyyyMMddOrNull ->
            viewModel.onEvent(RegisterUiEvent.OnBirthChange(yyyyMMddOrNull ?: ""))
        },
        onDismissDatePicker = { viewModel.onEvent(RegisterUiEvent.OnHideDatePicker) },
        onPhoneChange = { viewModel.onEvent(RegisterUiEvent.OnPhoneNumberChange(it)) },
        onRequestVerificationCode = { viewModel.onEvent(RegisterUiEvent.OnRequestVerificationCode) },
        onVerificationTokenChange = { viewModel.onEvent(RegisterUiEvent.OnVerificationCodeChange(it)) },
        onVerifyTokenClick = { viewModel.onEvent(RegisterUiEvent.OnVerifyCode) },
        onPasswordChange = { viewModel.onEvent(RegisterUiEvent.OnPasswordChange(it)) },
        onTogglePasswordVisible = { viewModel.onEvent(RegisterUiEvent.OnTogglePasswordVisibility) },
        onPasswordConfirmChange = { viewModel.onEvent(RegisterUiEvent.OnPasswordConfirmChange(it)) },
        onTogglePasswordConfirmVisible = { viewModel.onEvent(RegisterUiEvent.OnToggleConfirmPasswordVisibility) },
        onSelectGuardian = { viewModel.onEvent(RegisterUiEvent.OnUserTypeSelect(UserType.GUARDIAN)) },
        onSelectPatient = { viewModel.onEvent(RegisterUiEvent.OnUserTypeSelect(UserType.PATIENT)) },
        onSubmitRegister = { viewModel.onEvent(RegisterUiEvent.OnSubmit) },
        onDismissSuccessModal = onSuccess,
        onDismissErrorModal = { viewModel.onEvent(RegisterUiEvent.OnClearError) }
    )
}