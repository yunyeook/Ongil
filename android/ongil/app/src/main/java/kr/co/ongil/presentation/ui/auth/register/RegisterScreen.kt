package kr.co.ongil.presentation.ui.auth.register

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.ui.common.AlertModal
import kr.co.ongil.presentation.ui.common.AlertType
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.InputBox
import java.util.*
import kr.co.ongil.presentation.ui.auth.register.RegisterUiState
import kr.co.ongil.presentation.ui.auth.register.UserType



@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onBackClick: () -> Unit,
    onProfileImageClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onBirthClick: () -> Unit,
    onSetBirth: (String?) -> Unit,
    onDismissDatePicker: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onRequestVerificationCode: () -> Unit,
    onVerificationTokenChange: (String) -> Unit,
    onVerifyTokenClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onTogglePasswordConfirmVisible: () -> Unit,
    onSelectGuardian: () -> Unit,
    onSelectPatient: () -> Unit,
    onSubmitRegister: () -> Unit,
    onDismissSuccessModal: () -> Unit,
    onDismissErrorModal: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
        ) {
            // 상단 여백
            Spacer(modifier = Modifier.height(32.dp))

            // 본문 내용
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {

                // 프로필 영역
                ProfileSection(
                    profileImageUrl = uiState.profileImageUrl,
                    onProfileImageClick = onProfileImageClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 이름
                LabeledField(label = "이름") {
                    InputBox(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = "",
                        placeholder = "실명을 입력해주세요",
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 생년월일
                LabeledField(label = "생년월일") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color(0xFFDDE0E4),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clickable { onBirthClick() }
                    ) {
                        Text(
                            text = uiState.birth ?: "생년월일을 입력해주세요",
                            color = if (uiState.birth != null) Color(0xFF1A1D21) else Color(0xFF9CA1A9),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                        )

                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "calendar",
                            tint = Color(0xFF707680),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 휴대폰 번호 + 인증번호 발송 / 재발송
                LabeledField(label = "휴대폰 번호") {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InputBox(
                            value = uiState.phoneNumber,
                            onValueChange = onPhoneChange,
                            label = "",
                            placeholder = "010-1234-5678",
                            modifier = Modifier.weight(1f)
                        )

                        GreenButton(
                            text = if (uiState.isCodeRequested) "재발송" else "발송",
                            enabled = true,
                            onClick = onRequestVerificationCode,
                            modifier = Modifier
                                .weight(0.6f)
                                .height(56.dp)
                        )
                    }
                }

                // 인증번호 입력/확인/타이머/결과 메시지
                if (uiState.isCodeRequested) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InputBox(
                                value = uiState.verificationCode,
                                onValueChange = onVerificationTokenChange,
                                label = "인증번호",
                                placeholder = "인증번호 6자리",
                                modifier = Modifier.weight(1f)
                            )

                            GreenButton(
                                text = if (uiState.isCodeVerified) "확인됨" else "확인",
                                enabled = !uiState.isCodeVerified,
                                onClick = onVerifyTokenClick,
                                modifier = Modifier
                                    .weight(0.6f)
                                    .height(56.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 남은 시간
                            if (uiState.showTimerText) {
                                Text(
                                    text = "남은 시간: ${uiState.remainingTimeText}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD32F2F)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            // 성공 / 실패 메시지
                            if (uiState.verificationStatusMessage.isNotEmpty()) {
                                Text(
                                    text = uiState.verificationStatusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (uiState.isCodeVerified)
                                        Color(0xFF2E7D32)
                                    else
                                        Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 비밀번호
                LabeledField(label = "비밀번호") {
                    Column {
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { value ->
                                // 공백 제거
                                val filtered = value.replace(" ", "")
                                onPasswordChange(filtered)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "영문, 숫자, 특수문자 포함 8-16자",
                                    color = Color(0xFF9CA1A9),
                                    fontSize = 16.sp
                                )
                            },
                            visualTransformation = if (uiState.isPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordVisible) {
                                    Icon(
                                        imageVector = if (uiState.isPasswordVisible)
                                            Icons.Filled.VisibilityOff
                                        else
                                            Icons.Filled.Visibility,
                                        contentDescription = "toggle password visibility",
                                        tint = Color(0xFF707680)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.clearFocus() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFDDE0E4),
                                focusedBorderColor = Color(0xFF788F7E),
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // 비밀번호 규칙 안내
                        Text(
                            text = "영문, 숫자, 특수문자 포함 8-16자",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA1A9),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 비밀번호 확인
                LabeledField(label = "비밀번호 확인") {
                    Column {
                        OutlinedTextField(
                            value = uiState.passwordConfirm,
                            onValueChange = { value ->
                                // 공백 제거
                                val filtered = value.replace(" ", "")
                                onPasswordConfirmChange(filtered)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "비밀번호를 다시 입력해주세요",
                                    color = Color(0xFF9CA1A9),
                                    fontSize = 16.sp
                                )
                            },
                            visualTransformation = if (uiState.isConfirmPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordConfirmVisible) {
                                    Icon(
                                        imageVector = if (uiState.isConfirmPasswordVisible)
                                            Icons.Filled.VisibilityOff
                                        else
                                            Icons.Filled.Visibility,
                                        contentDescription = "toggle password visibility",
                                        tint = Color(0xFF707680)
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
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFDDE0E4),
                                focusedBorderColor = Color(0xFF788F7E),
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // 비밀번호 일치/불일치 메시지
                        if (uiState.passwordConfirm.isNotEmpty() && uiState.password.isNotEmpty()) {
                            Text(
                                text = if (uiState.password == uiState.passwordConfirm)
                                    "비밀번호가 일치합니다."
                                else
                                    "비밀번호가 일치하지 않습니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.password == uiState.passwordConfirm)
                                    Color(0xFF2E7D32)  // 초록색
                                else
                                    Color(0xFFD32F2F),  // 빨간색
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 회원 유형
                LabeledField(label = "회원 유형") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserTypeButton(
                            text = "보호자",
                            isSelected = uiState.userType == UserType.GUARDIAN,
                            onClick = onSelectGuardian
                        )
                        UserTypeButton(
                            text = "환자",
                            isSelected = uiState.userType == UserType.PATIENT,
                            onClick = onSelectPatient
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 가입하기 버튼
                GreenButton(
                    text = "가입하기",
                    enabled = uiState.canSubmit,
                    onClick = onSubmitRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(modifier = Modifier.height(100.dp)) // 하단 여유 공간
            }
        }

        // DatePicker 다이얼로그
        if (uiState.showDatePicker) {
            BirthDatePickerDialog(
                onDateSelected = onSetBirth,
                onDismiss = onDismissDatePicker
            )
        }

        // 성공 모달
        if (uiState.showSuccessModal) {
            AlertModal(
                onDismiss = onDismissSuccessModal,
                icon = null,
                message = uiState.successMessage ?: "회원가입이 성공적으로 완료되었습니다.",
                buttonText = "확인",
                onButtonClick = onDismissSuccessModal,
                type = AlertType.SUCCESS
            )
        }

        // 실패 모달
        if (uiState.showErrorModal) {
            AlertModal(
                onDismiss = onDismissErrorModal,
                icon = null,
                message = uiState.errorMessage ?: "회원가입에 실패했습니다.\n다시한번 시도해주세요.",
                buttonText = "확인",
                onButtonClick = onDismissErrorModal,
                type = AlertType.ERROR
            )
        }
    }
}

/**
 * 생년월일 선택 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDatePickerDialog(
    onDateSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val locale = Locale.KOREA
    val configuration = Configuration().apply { setLocale(locale) }
    val context = LocalContext.current
    val localizedContext = remember(locale) {
        context.createConfigurationContext(configuration)
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    CompositionLocalProvider(LocalContext provides localizedContext) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance(locale).apply { timeInMillis = millis }
                        val yyyyMMdd = String.format(
                            locale, "%04d%02d%02d",
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH)
                        )
                        onDateSelected(yyyyMMdd)
                    }
                    onDismiss()
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDateSelected(null)
                    onDismiss()
                }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 프로필 사진 업로드 UI
 */
@Composable
private fun ProfileSection(
    @Suppress("UNUSED_PARAMETER") profileImageUrl: String?,
    onProfileImageClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp)
        ) {
            // 원형 영역 (placeholder or uploaded image)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFDDE0E4),
                        shape = CircleShape
                    )
                    .background(
                        color = Color(0xFFF7F8F9),
                        shape = CircleShape
                    )
                    .clickable { onProfileImageClick() },
                contentAlignment = Alignment.Center
            ) {
                // TODO: 실제 프로필 이미지 로드 시 coil 등 사용
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "profile",
                    tint = Color(0xFF707680),
                    modifier = Modifier.size(40.dp)
                )
            }

            // 카메라 아이콘 배지
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .background(
                        color = Color(0xFF788F7E),
                        shape = CircleShape
                    )
                    .clickable { onProfileImageClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "upload profile image",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "프로필 사진 (선택사항)",
            color = Color(0xFF707680),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 라벨 + 필드 묶음
 */
@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF1A1D21),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

/**
 * 회원 유형 단일 Pill 버튼
 */
@Composable
private fun RowScope.UserTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) Color(0xFF788F7E) else Color.White
    val borderColor = if (isSelected) Color(0xFF788F7E) else Color(0xFFDDE0E4)
    val textColor = if (isSelected) Color.White else Color(0xFF1A1D21)

    Box(
        modifier = Modifier
            .weight(1f)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = bg,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRegisterScreen() {
    RegisterScreen(
        uiState = RegisterUiState(
            name = "",
            birth = null,
            phoneNumber = "",
            isCodeRequested = true,
            verificationToken = "",
            isCodeVerified = false,
            showTimerText = true,
            remainingTimeText = "02:45",
            verificationStatusMessage = "인증번호 등록에 성공했습니다.",
            userType = UserType.GUARDIAN
        ),
        onBackClick = {},
        onProfileImageClick = {},
        onNameChange = {},
        onBirthClick = {},
        onSetBirth = {},
        onDismissDatePicker = {},
        onPhoneChange = {},
        onRequestVerificationCode = {},
        onVerificationTokenChange = {},
        onVerifyTokenClick = {},
        onPasswordChange = {},
        onTogglePasswordVisible = {},
        onPasswordConfirmChange = {},
        onTogglePasswordConfirmVisible = {},
        onSelectGuardian = {},
        onSelectPatient = {},
        onSubmitRegister = {},
        onDismissSuccessModal = {},
        onDismissErrorModal = {},
    )
}

@Preview(showBackground = true, name = "Register - 초기 상태")
@Composable
private fun PreviewRegisterScreen_Initial() {
    RegisterScreen(
        uiState = RegisterUiState(),
        onBackClick = {},
        onProfileImageClick = {},
        onNameChange = {},
        onBirthClick = {},
        onSetBirth = {},
        onDismissDatePicker = {},
        onPhoneChange = {},
        onRequestVerificationCode = {},
        onVerificationTokenChange = {},
        onVerifyTokenClick = {},
        onPasswordChange = {},
        onTogglePasswordVisible = {},
        onPasswordConfirmChange = {},
        onTogglePasswordConfirmVisible = {},
        onSelectGuardian = {},
        onSelectPatient = {},
        onSubmitRegister = {},
        onDismissSuccessModal = {},
        onDismissErrorModal = {},
    )
}

@Preview(showBackground = true, name = "Register - 인증 진행중")
@Composable
private fun PreviewRegisterScreen_CodeRequested() {
    RegisterScreen(
        uiState = RegisterUiState(
            name = "홍길동",
            birth = "19990101",
            phoneNumber = "010-1234-5678",
            isCodeRequested = true,
            showTimerText = true,
            remainingTimeText = "02:45",
            verificationStatusMessage = "인증번호가 전송되었습니다.",
            userType = UserType.GUARDIAN
        ),
        onBackClick = {},
        onProfileImageClick = {},
        onNameChange = {},
        onBirthClick = {},
        onSetBirth = {},
        onDismissDatePicker = {},
        onPhoneChange = {},
        onRequestVerificationCode = {},
        onVerificationTokenChange = {},
        onVerifyTokenClick = {},
        onPasswordChange = {},
        onTogglePasswordVisible = {},
        onPasswordConfirmChange = {},
        onTogglePasswordConfirmVisible = {},
        onSelectGuardian = {},
        onSelectPatient = {},
        onSubmitRegister = {},
        onDismissSuccessModal = {},
        onDismissErrorModal = {},
    )
}

@Preview(showBackground = true, name = "Register - 인증 완료 & 제출 가능")
@Composable
private fun PreviewRegisterScreen_CodeVerified() {
    RegisterScreen(
        uiState = RegisterUiState(
            name = "홍길동",
            birth = "19990101",
            phoneNumber = "010-1234-5678",
            isCodeRequested = true,
            isCodeVerified = true,
            showTimerText = false,
            remainingTimeText = "00:00",
            verificationStatusMessage = "인증이 완료되었습니다.",
            userType = UserType.GUARDIAN,
            canSubmit = true
        ),
        onBackClick = {},
        onProfileImageClick = {},
        onNameChange = {},
        onBirthClick = {},
        onSetBirth = {},
        onDismissDatePicker = {},
        onPhoneChange = {},
        onRequestVerificationCode = {},
        onVerificationTokenChange = {},
        onVerifyTokenClick = {},
        onPasswordChange = {},
        onTogglePasswordVisible = {},
        onPasswordConfirmChange = {},
        onTogglePasswordConfirmVisible = {},
        onSelectGuardian = {},
        onSelectPatient = {},
        onSubmitRegister = {},
        onDismissSuccessModal = {},
        onDismissErrorModal = {},
    )
}

@Preview(showBackground = true, name = "Register - 성공 모달")
@Composable
private fun PreviewRegisterScreen_SuccessModal() {
    RegisterScreen(
        uiState = RegisterUiState(
            showSuccessModal = true,
            canSubmit = false
        ),
        onBackClick = {},
        onProfileImageClick = {},
        onNameChange = {},
        onBirthClick = {},
        onSetBirth = {},
        onDismissDatePicker = {},
        onPhoneChange = {},
        onRequestVerificationCode = {},
        onVerificationTokenChange = {},
        onVerifyTokenClick = {},
        onPasswordChange = {},
        onTogglePasswordVisible = {},
        onPasswordConfirmChange = {},
        onTogglePasswordConfirmVisible = {},
        onSelectGuardian = {},
        onSelectPatient = {},
        onSubmitRegister = {},
        onDismissSuccessModal = {},
        onDismissErrorModal = {},
    )
}

@Preview(showBackground = true, name = "Register - 실패 모달")
@Composable
private fun PreviewRegisterScreen_ErrorModal() {
    RegisterScreen(
        uiState = RegisterUiState(
            showErrorModal = true
        ),
        onBackClick = {},
        onProfileImageClick = {},
        onNameChange = {},
        onBirthClick = {},
        onSetBirth = {},
        onDismissDatePicker = {},
        onPhoneChange = {},
        onRequestVerificationCode = {},
        onVerificationTokenChange = {},
        onVerifyTokenClick = {},
        onPasswordChange = {},
        onTogglePasswordVisible = {},
        onPasswordConfirmChange = {},
        onTogglePasswordConfirmVisible = {},
        onSelectGuardian = {},
        onSelectPatient = {},
        onSubmitRegister = {},
        onDismissSuccessModal = {},
        onDismissErrorModal = {},
    )
}

@Preview(showBackground = true, name = "Register - 다크 모드", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewRegisterScreen_Dark() {
    RegisterScreen(
        uiState = RegisterUiState(
            isCodeRequested = true,
            showTimerText = true,
            remainingTimeText = "02:45",
            userType = UserType.PATIENT
        ),
        onBackClick = {},
        onProfileImageClick = {},
        onNameChange = {},
        onBirthClick = {},
        onSetBirth = {},
        onDismissDatePicker = {},
        onPhoneChange = {},
        onRequestVerificationCode = {},
        onVerificationTokenChange = {},
        onVerifyTokenClick = {},
        onPasswordChange = {},
        onTogglePasswordVisible = {},
        onPasswordConfirmChange = {},
        onTogglePasswordConfirmVisible = {},
        onSelectGuardian = {},
        onSelectPatient = {},
        onSubmitRegister = {},
        onDismissSuccessModal = {},
        onDismissErrorModal = {},
    )
}

@Preview(showBackground = true, name = "ProfileSection")
@Composable
private fun Preview_ProfileSection() {
    ProfileSection(
        profileImageUrl = null,
        onProfileImageClick = {}
    )
}

@Preview(showBackground = true, name = "LabeledField + InputBox")
@Composable
private fun Preview_LabeledField() {
    LabeledField(label = "이름") {
        InputBox(
            value = "",
            onValueChange = {},
            label = "",
            placeholder = "실명을 입력해주세요"
        )
    }
}

@Preview(showBackground = true, name = "UserType 버튼들")
@Composable
private fun Preview_UserTypeButtons() {
    Row(modifier = Modifier.fillMaxWidth()) {
        UserTypeButton(text = "보호자", isSelected = true, onClick = {})
        Spacer(modifier = Modifier.width(12.dp))
        UserTypeButton(text = "환자", isSelected = false, onClick = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "BirthDatePickerDialog")
@Composable
private fun Preview_BirthDatePickerDialog() {
    BirthDatePickerDialog(
        onDateSelected = {},
        onDismiss = {}
    )
}