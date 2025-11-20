package kr.co.ongil.presentation.ui.myinfo

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kr.co.ongil.presentation.theme.ongilColors
import kr.co.ongil.presentation.ui.common.LabeledOutlinedField
import kr.co.ongil.presentation.uistate.MyInfoEditEvent
import kr.co.ongil.presentation.uistate.MyInfoEditUiState
import kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel
import java.util.*

/* =========================
   유틸: 날짜 포맷
   ========================= */
fun formatDateForDisplay(date: String): String {
    if (date.length != 8) return date
    return "${date.substring(0, 4)}.${date.substring(4, 6)}.${date.substring(6, 8)}"
}

fun formatDateForStorage(date: String): String = date.replace(".", "")

/* =========================
   Screen Entrypoint
   ========================= */
@Composable
fun MyInfoEditScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
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
        modifier = modifier,
        outerPaddingValues = paddingValues
    )
}

/* =========================
   Content (Stateless)
   ========================= */
@Composable
fun MyInfoEditContent(
    uiState: MyInfoEditUiState,
    onEvent: (MyInfoEditEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onChangePasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    outerPaddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // 에러 메시지 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    // 저장 성공 시 뒤로가기
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onNavigateBack()
        }
    }

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onEvent(MyInfoEditEvent.OnImageSelected(it.toString())) }
            ?: onEvent(MyInfoEditEvent.DismissImagePicker)
    }

    // 이미지 피커 트리거
    LaunchedEffect(uiState.showImagePicker) {
        if (uiState.showImagePicker) {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    // 생년월일 다이얼로그
    if (uiState.showDatePicker) {
        BirthDatePickerDialog(
            onDateSelected = { dateString -> onEvent(MyInfoEditEvent.OnDateSelected(dateString)) },
            onDismiss = { onEvent(MyInfoEditEvent.DismissDatePicker) }
        )
    }

    // 내부 Scaffold가 바깥 Scaffold 인셋을 덮지 않도록 0으로 고정
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0) // ✅ 핵심
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(outerPaddingValues)
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()) // ✅ 스크롤
                .navigationBarsPadding()               // ✅ 제스처/네비바 보정
                .imePadding(),                          // ✅ 키보드 보정
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* -------- 아바타 -------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(140.dp)) {
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
                                contentDescription = "프로필 이미지",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                    }
                    Surface(
                        color = ongilColors.accent,
                        shape = CircleShape,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(48.dp)
                            .clickable { onEvent(MyInfoEditEvent.PickProfileImage) }
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.CameraAlt,
                                contentDescription = "사진 변경",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Text(
                text = uiState.roleLabel,
                color = ongilColors.accent,
                style = MaterialTheme.typography.titleMedium
            )

            /* -------- 이름 -------- */
            LabeledOutlinedField(
                label = "이름",
                value = uiState.name,
                onValueChange = { onEvent(MyInfoEditEvent.UpdateName(it)) }
            )

            /* -------- 생년월일 -------- */
            LabeledOutlinedField(
                label = "생년월일",
                value = formatDateForDisplay(uiState.birth),
                onValueChange = { newValue ->
                    onEvent(MyInfoEditEvent.UpdateBirth(formatDateForStorage(newValue)))
                },
                trailing = {
                    IconButton(onClick = { onEvent(MyInfoEditEvent.PickBirthDate) }) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = "날짜 선택",
                            tint = Color(0xFF7B8A8D)
                        )
                    }
                }
            )

            Spacer(Modifier.height(6.dp))

            /* -------- 휴대폰 인증 섹션 -------- */
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
                onVerifyClick = {
                    onEvent(MyInfoEditEvent.VerifyCode(uiState.newPhone, uiState.verificationCode))
                }
            )

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onChangePasswordClick) {
                Text("비밀번호 변경하기", color = ongilColors.accent, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    onEvent(MyInfoEditEvent.SaveInfo)
                },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ongilColors.accent),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("저장하기", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(32.dp)) // 하단 여유
        }
    }
}

/* =========================
   생년월일 선택 다이얼로그
   ========================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDatePickerDialog(
    onDateSelected: (String) -> Unit,
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
                    } ?: onDismiss()
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("생년월일 선택", modifier = Modifier.padding(16.dp)) }
            )
        }
    }
}

/* =========================
   Preview
   ========================= */
@Preview(showBackground = true)
@Composable
fun PreviewMyInfoEditScreen_All() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val fakeUserRepository = kr.co.ongil.data.repository.fake.FakeUserRepository()
        val getUserUseCase = kr.co.ongil.domain.usecase.user.GetUserUseCase(fakeUserRepository)
        val updateUserUseCase = kr.co.ongil.domain.usecase.user.UpdateUserUseCase(fakeUserRepository)
        val previewViewModel = MyInfoEditViewModel(
            getUserUseCase = getUserUseCase,
            updateUserUseCase = updateUserUseCase,
            userRepository = fakeUserRepository
        )
        MyInfoEditScreen(viewModel = previewViewModel)
    }
}
