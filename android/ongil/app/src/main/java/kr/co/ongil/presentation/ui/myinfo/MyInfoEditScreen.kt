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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Accent = Color(0xFF8CA898)

enum class PhoneUiState { Idle, Editing, Verifying } // Idle: 표시, Editing: 새 번호 + 발송, Verifying: 코드입력

@Composable
fun MyInfoEditScreen(
    roleLabel: String,
    nameInit: String,
    birthInit: String,
    phoneInit: String,
    profileImageUrl: String? = null,
    onPickProfileImage: () -> Unit = {},
    onBirthPickClick: () -> Unit = {},
    onSendVerifyCode: (newPhone: String) -> Unit = {},
    onResendCode: (newPhone: String) -> Unit = {},
    onVerifyCode: (newPhone: String, code6: String) -> Unit = { _, _ -> },
    onChangePasswordClick: () -> Unit = {},
    onSaveClick: (name: String, birth: String, phone: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf(nameInit) }
    var birth by rememberSaveable { mutableStateOf(birthInit) }
    var phone by rememberSaveable { mutableStateOf(phoneInit) }

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
                    if (!profileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profileImageUrl,
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
                        .clickable { onPickProfileImage() }
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = "사진 변경", tint = Color.White)
                    }
                }
            }
        }

        Text(text = roleLabel, color = Accent, style = MaterialTheme.typography.titleMedium)

        // 이름
        LabeledOutlinedField(
            label = "이름",
            value = name,
            onValueChange = { name = it }
        )

        // 생년월일
        LabeledOutlinedField(
            label = "생년월일",
            value = birth,
            onValueChange = { birth = it },
            trailing = {
                IconButton(onClick = onBirthPickClick) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = "날짜", tint = Color(0xFF7B8A8D))
                }
            }
        )

        // 휴대폰 번호(변경/인증 전체 섹션)
        Spacer(Modifier.height(6.dp))
        PhoneVerificationSection(
            currentPhone = phone,
            onRequestSend = { newPhone ->
                onSendVerifyCode(newPhone)
            },
            onResend = { newPhone ->
                onResendCode(newPhone)
            },
            onVerified = { newPhone, _ ->
                phone = newPhone // 인증 성공 시 화면의 표시 번호 갱신
            },
            onVerifyCode = onVerifyCode
        )

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onChangePasswordClick) {
            Text("비밀번호 변경하기", color = Accent, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSaveClick(name, birth, phone) },
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("저장하기", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PhoneVerificationSection(
    currentPhone: String,
    onRequestSend: (newPhone: String) -> Unit,              // 첫 발송
    onResend: (newPhone: String) -> Unit,                   // 재발송
    onVerifyCode: (newPhone: String, code6: String) -> Unit,// 서버 검증
    onVerified: (newPhone: String, code6: String) -> Unit,  // 검증 성공 시 UI 반영 콜백
) {
    var uiState by rememberSaveable { mutableStateOf(PhoneUiState.Idle) }
    var newPhone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }

    // 타이머 상태
    var secondsLeft by rememberSaveable { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) }

    fun startTimer(totalSec: Int = 180) {
        timerJob?.cancel()
        secondsLeft = totalSec
        timerJob = scope.launch {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
        }
    }

    Column {
        // 라벨
        Text(
            text = "휴대폰 번호",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF6B767A),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp)
        )

        when (uiState) {
            PhoneUiState.Idle -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentPhone,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFCBD5D0),
                            unfocusedBorderColor = Color(0xFFE3E7E5),
                            cursorColor = Color(0xFF6B767A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Button(
                        onClick = { uiState = PhoneUiState.Editing },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text("변경하기", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

        PhoneUiState.Editing -> {
            OutlinedTextField(
                value = newPhone,
                onValueChange = { newPhone = it.filter { ch -> ch.isDigit() || ch == '-' } },
                placeholder = { Text("새로운 번호를 입력하세요") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Button(
                        onClick = {
                            if (newPhone.isNotBlank()) {
                                onRequestSend(newPhone)
                                uiState = PhoneUiState.Verifying
                                startTimer()
                            }
                        },
                        enabled = newPhone.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) { Text("인증번호 발송") }
                }
            )
        }

        PhoneUiState.Verifying -> {
            // 입력 필드: 번호 + 재발송
            OutlinedTextField(
                value = newPhone,
                onValueChange = { newPhone = it.filter { ch -> ch.isDigit() || ch == '-' } },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Button(
                        onClick = {
                            if (newPhone.isNotBlank()) {
                                onResend(newPhone)
                                startTimer()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) { Text("재발송") }
                }
            )

            Spacer(Modifier.height(12.dp))

            // 인증번호 입력 + 확인
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it.filter(Char::isDigit) },
                singleLine = true,
                placeholder = { Text("인증번호 6자리") },
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Button(
                        onClick = {
                            if (code.length == 6) {
                                onVerifyCode(newPhone, code)
                                onVerified(newPhone, code) // 서버 성공 콜백에서만 호출하도록 바꿔도 됨
                                timerJob?.cancel()
                            }
                        },
                        enabled = code.length == 6,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) { Text("확인") }
                }
            )

            // 남은 시간 표시
            val mm = (secondsLeft / 60).toString().padStart(2, '0')
            val ss = (secondsLeft % 60).toString().padStart(2, '0')
            val timeColor = if (secondsLeft in 1..180) Color(0xFFD85B4E) else Color(0xFF9AA3A7)

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("남은 시간: ", color = Color(0xFF6B767A))
                Text("$mm:$ss", color = timeColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    }
}

@Composable
private fun LabeledOutlinedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFF6B767A),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 8.dp)
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        trailingIcon = trailing,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFCBD5D0),
            unfocusedBorderColor = Color(0xFFE3E7E5),
            cursorColor = Color(0xFF6B767A),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewMyInfoEditScreen_All() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MyInfoEditScreen(
            roleLabel = "보호자",
            nameInit = "김민수",
            birthInit = "1972.10.29",
            phoneInit = "010-4321-8765",
        )
    }
}
