package kr.co.ongil.presentation.ui.findpassword

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.GreyButton
import kr.co.ongil.presentation.ui.common.OngilTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPasswordScreen(
    uiState: FindPasswordUiState,
    onEvent: (FindPasswordUiEvent) -> Unit,
    formatPhone: (String) -> String,
    goHelp: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            OngilTopBar(
                title = "비밀번호 찾기",
                onBackClick = { onEvent(FindPasswordUiEvent.ClickBack) }
            )
        },
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = Color(0xFFFFFFFF)
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .imePadding()
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                "휴대폰 번호",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = "가입시 사용한 휴대폰 번호를 입력해주세요.",
                style = MaterialTheme.typography.bodySmall, // 글자 더 작게
                color = Color(0xFFE53935)
            )
            Spacer(Modifier.height(-5.dp)) // 라벨~인풋 사이 전체 간격 줄어듦

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = { raw -> onEvent(FindPasswordUiEvent.ChangePhone(formatPhone(raw))) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "휴대폰 번호를 입력해주세요.",
                            color = Color(0xFF9CA1A9),
                            fontSize = 16.sp
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    enabled = !uiState.isCodeRequested,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFDDE0E4),
                        focusedBorderColor = Color(0xFF788F7E),
                        disabledBorderColor = Color(0xFFDDE0E4),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    singleLine = true
                )
                val sendLabel = if (uiState.isCodeRequested) "재전송" else "발송"
                val sendEnabled = uiState.isPhoneValid
                val sendOnClick = {
                    if (uiState.isCodeRequested) onEvent(FindPasswordUiEvent.ClickResendCode)
                    else onEvent(FindPasswordUiEvent.ClickSendCode)
                }
                GreenButton(
                    text = sendLabel,
                    enabled = sendEnabled,
                    onClick = sendOnClick,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(56.dp)
                )
            }

            if (uiState.isCodeRequested) {
                Spacer(Modifier.height(20.dp))
                Text("인증번호", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.code,
                        onValueChange = { input ->
                            if (input.length <= uiState.codeLength && input.all { it.isDigit() }) {
                                onEvent(FindPasswordUiEvent.ChangeCode(input))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "인증번호 ${uiState.codeLength}자리",
                                color = Color(0xFF9CA1A9),
                                fontSize = 16.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFDDE0E4),
                            focusedBorderColor = Color(0xFF788F7E),
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        singleLine = true
                    )
                    val verifyEnabled = uiState.code.length == uiState.codeLength && uiState.remainingSec > 0
                    GreenButton(
                        text = "확인",
                        enabled = verifyEnabled,
                        onClick = { onEvent(FindPasswordUiEvent.ClickVerifyCode) },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(56.dp)
                    )
                }

                val timerText = if (uiState.remainingSec > 0) {
                    val m = uiState.remainingSec / 60
                    val s = uiState.remainingSec % 60
                    String.format("%02d:%02d", m, s)
                } else "00:00"

                Spacer(Modifier.height(8.dp))
                when (uiState.codeVerifyStatus) {
                    CodeVerifyStatus.Error -> Text(
                        text = "올바른 인증번호가 아닙니다.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    CodeVerifyStatus.Success -> Text(
                        text = "인증번호 등록에 성공했습니다.",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    CodeVerifyStatus.Expired -> Text(
                        text = "인증시간이 만료되었습니다. 재전송을 눌러 다시 시도해주세요.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("남은 시간: ", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                timerText,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            GreenButton(
                text = "비밀번호 재설정",
                onClick = { onEvent(FindPasswordUiEvent.ClickResetPassword) },
                isActive = uiState.isResetEnabled,
                enabled = uiState.isResetEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(Modifier.height(20.dp))
            AssistCard(onClick = goHelp)

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))
            Text(
                "계정이 기억나셨나요?",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "로그인으로 돌아가기",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onEvent(FindPasswordUiEvent.ClickGoLogin) }
                    .padding(vertical = 8.dp),
                color = Color(0xFF6B8F7E),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AssistCard(onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 0.dp,
        color = Color(0xFFF5F7F8),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("도움이 필요하세요?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "휴대폰 번호를 잃어버렸거나 문제가 있다면 고객센터로 문의해주세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "고객센터 문의",
                color = Color(0xFF6B8F7E),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clickable { onClick() }
                    .padding(top = 4.dp)
            )
        }
    }
}

// ---- Previews ----
@Preview(showBackground = true, name = "발송 전")
@Composable
private fun Preview_FindPassword_Initial() {
    MaterialTheme {
        FindPasswordScreen(
            uiState = FindPasswordUiState(
                phone = "010-1234-5678",
                isPhoneValid = true,
                isCodeRequested = false,
                code = "",
                remainingSec = 0,
                codeVerifyStatus = CodeVerifyStatus.Idle,
                isResetEnabled = false
            ),
            onEvent = {},
            formatPhone = { it }
        )
    }
}

@Preview(showBackground = true, name = "인증 진행/오류")
@Composable
private fun Preview_FindPassword_Verify_Error() {
    MaterialTheme {
        FindPasswordScreen(
            uiState = FindPasswordUiState(
                phone = "010-1234-5678",
                isPhoneValid = true,
                isCodeRequested = true,
                code = "123",
                codeLength = 6,
                remainingSec = 145,
                codeVerifyStatus = CodeVerifyStatus.Error,
                isResetEnabled = false
            ),
            onEvent = {},
            formatPhone = { it }
        )
    }
}

@Preview(showBackground = true, name = "인증 성공")
@Composable
private fun Preview_FindPassword_Verify_Success() {
    MaterialTheme {
        FindPasswordScreen(
            uiState = FindPasswordUiState(
                phone = "010-1234-5678",
                isPhoneValid = true,
                isCodeRequested = true,
                code = "123456",
                codeLength = 6,
                remainingSec = 150,
                codeVerifyStatus = CodeVerifyStatus.Success,
                isResetEnabled = true
            ),
            onEvent = {},
            formatPhone = { it }
        )
    }
}
