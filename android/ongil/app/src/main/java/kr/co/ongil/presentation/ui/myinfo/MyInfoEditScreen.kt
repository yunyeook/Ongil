package kr.co.ongil.presentation.ui.myinfo

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kr.co.ongil.presentation.ui.components.LabeledOutlinedField
import kr.co.ongil.presentation.uistate.MyInfoEditEvent
import kr.co.ongil.presentation.uistate.MyInfoEditUiState
import kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel
import java.util.*

private val Accent = Color(0xFF8CA898)

/**
 * 날짜 형식 변환 함수
 */
// YYYYMMDD → YYYY.MM.DD
private fun formatDateForDisplay(date: String): String {
    if (date.length != 8) return date
    return "${date.substring(0, 4)}.${date.substring(4, 6)}.${date.substring(6, 8)}"
}

// YYYY.MM.DD → YYYYMMDD
private fun formatDateForStorage(date: String): String {
    return date.replace(".", "")
}

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

    // 이미지 선택 런처 (한국어 타이틀)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            onEvent(MyInfoEditEvent.OnImageSelected(it.toString()))
        } ?: onEvent(MyInfoEditEvent.DismissImagePicker)
    }

    // showImagePicker 상태 감지
    LaunchedEffect(uiState.showImagePicker) {
        if (uiState.showImagePicker) {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    // 날짜 선택 다이얼로그
    if (uiState.showDatePicker) {
        BirthDatePickerDialog(
            onDateSelected = { dateString ->
                onEvent(MyInfoEditEvent.OnDateSelected(dateString))
            },
            onDismiss = {
                onEvent(MyInfoEditEvent.DismissDatePicker)
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
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
            value = formatDateForDisplay(uiState.birth),
            onValueChange = { newValue ->
                // 사용자가 직접 입력하는 경우 처리 (선택 사항)
                val formatted = formatDateForStorage(newValue)
                onEvent(MyInfoEditEvent.UpdateBirth(formatted))
            },
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
}

/**
 * 생년월일 선택 다이얼로그 (한국어)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val locale = Locale.KOREA

    // 한국어 환경 설정
    val configuration = Configuration()
    configuration.setLocale(locale)

    val context = LocalContext.current
    val localizedContext = remember(locale) {
        context.createConfigurationContext(configuration)
    }

    // DatePicker 상태
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    CompositionLocalProvider(
        LocalContext provides localizedContext
    ) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance(locale)
                            calendar.timeInMillis = millis
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH) + 1
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            // YYYYMMDD 형식으로 변환 (저장용)
                            val dateString = String.format(locale, "%04d%02d%02d", year, month, day)
                            onDateSelected(dateString)
                        } ?: onDismiss()
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text("생년월일 선택", modifier = Modifier.padding(16.dp))
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMyInfoEditScreen_All() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val previewViewModel = MyInfoEditViewModel(
            initialName = "김민수",
            initialBirth = "19721029",  // YYYYMMDD 형식으로 저장
            initialPhone = "010-4321-8765",
            initialRoleLabel = "보호자"
        )
        MyInfoEditScreen(
            viewModel = previewViewModel
        )
    }
}

