package kr.co.ongil.presentation.ui.myinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.co.ongil.presentation.theme.ongilColors
import kr.co.ongil.presentation.uistate.ChangePasswordEvent
import kr.co.ongil.presentation.uistate.ChangePasswordUiState
import kr.co.ongil.presentation.viewmodel.ChangePasswordViewModel

/**
 * 비밀번호 변경 화면 (ViewModel 기반)
 */
@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: ChangePasswordViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onPasswordChanged: () -> Unit = {},
    isResetMode: Boolean = false  // 비밀번호 찾기에서 온 경우 true
) {
    val uiState by viewModel.uiState.collectAsState()

    // 비밀번호 변경 성공 시 내정보 화면으로 이동
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPasswordChanged()  // NavGraph에서 MyInfo로 이동 처리
        }
    }

    ChangePasswordContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        isResetMode = isResetMode
    )
}

/**
 * 비밀번호 변경 화면 컨텐츠 (Stateless)
 */
@Composable
private fun ChangePasswordContent(
    uiState: ChangePasswordUiState,
    onEvent: (ChangePasswordEvent) -> Unit,
    modifier: Modifier = Modifier,
    isResetMode: Boolean = false
) {
    // Snackbar 호스트 상태
    val snackbarHostState = remember { SnackbarHostState() }

    // 에러 메시지 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 현재 비밀번호 (비밀번호 재설정 모드에서는 숨김)
            if (!isResetMode) {
                PasswordInputField(
                    label = "현재 비밀번호",
                    placeholder = "현재 비밀번호를 입력해주세요.",
                    value = uiState.currentPassword,
                    onValueChange = { onEvent(ChangePasswordEvent.UpdateCurrentPassword(it)) },
                    visible = uiState.currentPasswordVisible,
                    onVisibilityToggle = { onEvent(ChangePasswordEvent.ToggleCurrentPasswordVisibility) }
                )
                Spacer(Modifier.height(20.dp))
            } else {
                // 재설정 모드일 때 위쪽 여백 추가
                Spacer(Modifier.height(60.dp))
            }

            // 새 비밀번호
            PasswordInputField(
                label = "새 비밀번호",
                placeholder = "새 비밀번호를 입력해주세요.",
                value = uiState.newPassword,
                onValueChange = { onEvent(ChangePasswordEvent.UpdateNewPassword(it)) },
                visible = uiState.newPasswordVisible,
                onVisibilityToggle = { onEvent(ChangePasswordEvent.ToggleNewPasswordVisibility) }
            )

            Text(
                text = "영문, 숫자, 특수문자 포함 8자 이상",
                color = Color(0xFF6B767A),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 6.dp, bottom = 20.dp)
            )

            // 새 비밀번호 확인
            PasswordInputField(
                label = "새 비밀번호 확인",
                placeholder = "새 비밀번호를 다시 입력해주세요.",
                value = uiState.confirmPassword,
                onValueChange = { onEvent(ChangePasswordEvent.UpdateConfirmPassword(it)) },
                visible = uiState.confirmPasswordVisible,
                onVisibilityToggle = { onEvent(ChangePasswordEvent.ToggleConfirmPasswordVisibility) },
                isError = uiState.confirmPassword.isNotEmpty() && uiState.newPassword != uiState.confirmPassword
            )

            // 비밀번호 불일치 메시지
            if (uiState.confirmPassword.isNotEmpty() && uiState.newPassword != uiState.confirmPassword) {
                Text(
                    text = "비밀번호가 일치하지 않습니다",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 6.dp)
                )
            }

            Spacer(Modifier.height(40.dp))

            // 저장하기 버튼 (MyInfoEditScreen 스타일)
            Button(
                onClick = { onEvent(ChangePasswordEvent.ChangePassword) },
                enabled = !uiState.isLoading &&
                        (isResetMode || uiState.currentPassword.isNotBlank()) &&
                        uiState.newPassword.isNotBlank() &&
                        uiState.confirmPassword.isNotBlank() &&
                        uiState.newPassword == uiState.confirmPassword,
                colors = ButtonDefaults.buttonColors(containerColor = ongilColors.accent),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("저장하기", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * 비밀번호 입력 필드 (label 밖으로 분리)
 */
@Composable
private fun PasswordInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onVisibilityToggle: () -> Unit,
    isError: Boolean = false
) {
    val focusManager = LocalFocusManager.current

    Column {
        // Label을 입력창 밖으로
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF6B767A),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp)
        )

        // 입력창
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFCBD5D0)) },
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (visible) "비밀번호 숨기기" else "비밀번호 보기",
                        tint = Color(0xFFCBD5D0)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            shape = RoundedCornerShape(14.dp),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFCBD5D0),
                unfocusedBorderColor = Color(0xFFE3E7E5),
                cursorColor = Color(0xFF6B767A),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
    }
}

// Preview는 Hilt 의존성 때문에 주석 처리
//@Preview(showBackground = true)
//@Composable
//fun PreviewChangePasswordScreen() {
//    MaterialTheme {
//        val fakeUserRepository = kr.co.ongil.data.repository.fake.FakeUserRepository()
//        val previewViewModel = ChangePasswordViewModel(userRepository = fakeUserRepository)
//        ChangePasswordScreen(viewModel = previewViewModel)
//    }
//}