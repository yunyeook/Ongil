package kr.co.ongil.presentation.ui.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.ui.common.AlertModal
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.GreyButton
import kr.co.ongil.presentation.ui.common.InputBox
import androidx.compose.ui.res.painterResource
/**
 * 회원가입 화면 (Preview-safe 버전)
 */
@Composable
fun SignupScreen(
    uiState: SignupUiState,
    onBackClick: () -> Unit,
    onProfileImageClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onBirthClick: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onRequestVerificationCode: () -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onVerifyCodeClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onTogglePasswordConfirmVisible: () -> Unit,
    onSelectGuardian: () -> Unit,
    onSelectPatient: () -> Unit,
    onSubmitSignup: () -> Unit,
    onDismissSuccessModal: () -> Unit,
    onDismissErrorModal: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            SignupHeader(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {

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
                            .border(1.dp, Color(0xFFDDE0E4), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clickable { onBirthClick() }
                    ) {
                        Text(
                            text = if (uiState.birth.isNotEmpty()) uiState.birth else "생년월일을 입력해주세요",
                            color = if (uiState.birth.isNotEmpty()) Color(0xFF1A1D21) else Color(0xFF9CA1A9),
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

                // 휴대폰 번호
                LabeledField(label = "휴대폰 번호") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InputBox(
                            value = uiState.phoneNumber,
                            onValueChange = onPhoneChange,
                            label = "",
                            placeholder = "010-1234-5678",
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                        )

                        GreenButton(
                            text = if (uiState.isCodeRequested) "재발송" else "인증번호 발송",
                            onClick = onRequestVerificationCode
                        )
                    }
                }

                // 인증번호 입력
                if (uiState.isCodeRequested) {
                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledField(label = "인증번호") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InputBox(
                                value = uiState.verificationCode,
                                onValueChange = onVerificationCodeChange,
                                label = "",
                                placeholder = "인증번호 6자리",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                            )

                            GreenButton(
                                text = if (uiState.isCodeVerified) "확인완료" else "확인",
                                onClick = onVerifyCodeClick,
                            )
                        }

                        if (uiState.showTimerText || uiState.verificationStatusMessage.isNotEmpty()) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                if (uiState.showTimerText) {
                                    Text(
                                        text = "남은 시간: ${uiState.remainingTimeText}",
                                        color = Color(0xFFD32F2F),
                                        fontSize = 14.sp,
                                    )
                                }

                                if (uiState.verificationStatusMessage.isNotEmpty()) {
                                    Text(
                                        text = uiState.verificationStatusMessage,
                                        color = if (uiState.isCodeVerified)
                                            Color(0xFF2E7D32)
                                        else Color(0xFFD32F2F),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 비밀번호
                LabeledField(label = "비밀번호") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFDDE0E4), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (uiState.password.isNotEmpty()) uiState.passwordMasked else "영문, 숫자 조합 8자 이상",
                            color = if (uiState.password.isNotEmpty()) Color(0xFF1A1D21) else Color(0xFF9CA1A9),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                        )

                        Icon(
                            imageVector = if (uiState.isPasswordVisible)
                                Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "toggle password visibility",
                            tint = Color(0xFF707680),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onTogglePasswordVisible() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 비밀번호 확인
                LabeledField(label = "비밀번호 확인") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFDDE0E4), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (uiState.passwordConfirm.isNotEmpty()) uiState.passwordConfirmMasked else "비밀번호를 다시 입력해주세요",
                            color = if (uiState.passwordConfirm.isNotEmpty()) Color(0xFF1A1D21) else Color(0xFF9CA1A9),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                        )

                        Icon(
                            imageVector = if (uiState.isConfirmPasswordVisible)
                                Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "toggle confirm password visibility",
                            tint = Color(0xFF707680),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onTogglePasswordConfirmVisible() }
                        )
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

                GreenButton(
                    text = "가입하기",
                    onClick = onSubmitSignup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (uiState.showSuccessModal) {
            AlertModal(
                onDismiss = onDismissSuccessModal,
                icon = null,
                message = "회원가입이 성공적으로 완료되었습니다.",
                buttonText = "확인",
                onButtonClick = onDismissSuccessModal
            )
        }

        if (uiState.showErrorModal) {
            AlertModal(
                onDismiss = onDismissErrorModal,
                icon = null,
                message = "회원가입에 실패했습니다.\n다시한번 시도해주세요.",
                buttonText = "확인",
                onButtonClick = onDismissErrorModal
            )
        }
    }
}

@Composable
private fun SignupHeader(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "back",
                tint = Color(0xFF1A1D21),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "회원가입",
                color = Color(0xFF1A1D21),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileSection(profileImageUrl: String?, onProfileImageClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(120.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, Color(0xFFDDE0E4), CircleShape)
                    .background(Color(0xFFF7F8F9), CircleShape)
                    .clickable { onProfileImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "profile",
                        tint = Color(0xFF707680),
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(android.R.drawable.ic_menu_gallery),
                        contentDescription = "profile image",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .background(Color(0xFF788F7E), CircleShape)
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

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bg, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

data class SignupUiState(
    val profileImageUrl: String? = null,
    val name: String = "",
    val birth: String = "",
    val phoneNumber: String = "",
    val isCodeRequested: Boolean = false,
    val verificationCode: String = "",
    val isCodeVerified: Boolean = false,
    val showTimerText: Boolean = false,
    val remainingTimeText: String = "",
    val verificationStatusMessage: String = "",
    val password: String = "",
    val passwordMasked: String = "",
    val passwordConfirm: String = "",
    val passwordConfirmMasked: String = "",
    val userType: UserType? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val showSuccessModal: Boolean = false,
    val showErrorModal: Boolean = false,
)

enum class UserType { GUARDIAN, PATIENT }

@Preview(showBackground = true)
@Composable
private fun PreviewSignupScreen() {
    SignupScreen(
        uiState = SignupUiState(),
        onBackClick = {},
        onProfileImageClick = {},
        onNameChange = {},
        onBirthClick = {},
        onPhoneChange = {},
        onRequestVerificationCode = {},
        onVerificationCodeChange = {},
        onVerifyCodeClick = {},
        onPasswordChange = {},
        onTogglePasswordVisible = {},
        onPasswordConfirmChange = {},
        onTogglePasswordConfirmVisible = {},
        onSelectGuardian = {},
        onSelectPatient = {},
        onSubmitSignup = {},
        onDismissSuccessModal = {},
        onDismissErrorModal = {},
    )
}
