package kr.co.ongil.presentation.ui.myinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kr.co.ongil.presentation.ui.components.LabeledOutlinedField
import kr.co.ongil.presentation.uistate.MyInfoEditEvent
import kr.co.ongil.presentation.uistate.MyInfoEditUiState
import kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel

private val Accent = Color(0xFF8CA898)

/**
 * 내 정보 수정 화면 (ViewModel 기반)
 */
@Composable
fun MyInfoEditScreen(
    modifier: Modifier = Modifier,
    viewModel: MyInfoEditViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    MyInfoEditContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onChangePasswordClick = onChangePasswordClick,
        modifier = modifier
    )
}

/**
 * 내 정보 수정 화면 컨텐츠 (Stateless)
 */
@Composable
private fun MyInfoEditContent(
    uiState: MyInfoEditUiState,
    onEvent: (MyInfoEditEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onChangePasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 아바타
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(140.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFF1F3F4)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!uiState.profileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = uiState.profileImageUrl,
                            contentDescription = "프로필",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                }
                Surface(
                    color = Accent,
                    shape = CircleShape,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(48.dp)
                        .clickable { onEvent(MyInfoEditEvent.PickProfileImage) }
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = "사진 변경", tint = Color.White)
                    }
                }
            }
        }

        Text(text = uiState.roleLabel, color = Accent, style = MaterialTheme.typography.titleMedium)

        // 이름
        LabeledOutlinedField(
            label = "이름",
            value = uiState.name,
            onValueChange = { onEvent(MyInfoEditEvent.UpdateName(it)) }
        )

        // 생년월일
        LabeledOutlinedField(
            label = "생년월일",
            value = uiState.birth,
            onValueChange = { onEvent(MyInfoEditEvent.UpdateBirth(it)) },
            trailing = {
                IconButton(onClick = { onEvent(MyInfoEditEvent.PickBirthDate) }) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = "날짜", tint = Color(0xFF7B8A8D))
                }
            }
        )

        // 휴대폰 번호(변경/인증 전체 섹션)
        Spacer(Modifier.height(6.dp))
        PhoneVerificationSection(
            phoneUiState = uiState.phoneUiState,
            currentPhone = uiState.phone,
            newPhone = uiState.newPhone,
            verificationCode = uiState.verificationCode,
            verificationResult = uiState.verificationResult,
            secondsLeft = uiState.secondsLeft,
            onChangeClick = { onEvent(MyInfoEditEvent.StartPhoneEdit) },
            onPhoneChange = { onEvent(MyInfoEditEvent.UpdateNewPhone(it)) },
            onSendClick = { onEvent(MyInfoEditEvent.SendVerificationCode(uiState.newPhone)) },
            onResendClick = { onEvent(MyInfoEditEvent.ResendVerificationCode(uiState.newPhone)) },
            onCodeChange = { onEvent(MyInfoEditEvent.UpdateVerificationCode(it)) },
            onVerifyClick = { onEvent(MyInfoEditEvent.VerifyCode(uiState.newPhone, uiState.verificationCode)) }
        )

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onChangePasswordClick) {
            Text("비밀번호 변경하기", color = Accent, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onEvent(MyInfoEditEvent.SaveInfo(uiState.name, uiState.birth, uiState.phone))
                onNavigateBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("저장하기", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMyInfoEditScreen_All() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val previewViewModel = MyInfoEditViewModel(
            initialName = "김민수",
            initialBirth = "1972.10.29",
            initialPhone = "010-4321-8765",
            initialRoleLabel = "보호자"
        )
        MyInfoEditScreen(
            viewModel = previewViewModel
        )
    }
}

