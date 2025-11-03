package kr.co.ongil.presentation.ui.myinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kr.co.ongil.presentation.uistate.PhoneUiState

private val PhoneAccent = Color(0xFF8CA898)

/**
 * Stateless 휴대폰 인증 섹션
 * 모든 상태는 파라미터로 받음
 */
@Composable
fun PhoneVerificationSection(
    phoneUiState: PhoneUiState,
    currentPhone: String,
    newPhone: String,
    verificationCode: String,
    verificationResult: Boolean?,
    secondsLeft: Int,
    onChangeClick: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onResendClick: () -> Unit,
    onCodeChange: (String) -> Unit,
    onVerifyClick: () -> Unit
) {
    Column {
        Text(
            text = "휴대폰 번호",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF6B767A),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp)
        )

        when (phoneUiState) {
            PhoneUiState.Idle -> {
                PhoneIdleState(
                    currentPhone = currentPhone,
                    onChangeClick = onChangeClick
                )
            }

            PhoneUiState.Editing -> {
                PhoneEditingState(
                    newPhone = newPhone,
                    onPhoneChange = onPhoneChange,
                    onSendClick = onSendClick
                )
            }

            PhoneUiState.Verifying -> {
                PhoneVerifyingState(
                    newPhone = newPhone,
                    code = verificationCode,
                    verificationResult = verificationResult,
                    secondsLeft = secondsLeft,
                    onPhoneChange = onPhoneChange,
                    onCodeChange = onCodeChange,
                    onResendClick = onResendClick,
                    onVerifyClick = onVerifyClick
                )
            }
        }
    }
}

@Composable
private fun PhoneIdleState(
    currentPhone: String,
    onChangeClick: () -> Unit
) {
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
            onClick = onChangeClick,
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(56.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text("변경하기", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PhoneEditingState(
    newPhone: String,
    onPhoneChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newPhone,
            onValueChange = { onPhoneChange(it.filter { ch -> ch.isDigit() || ch == '-' }) },
            placeholder = { Text("새로운 번호를 입력하세요") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
            onClick = onSendClick,
            enabled = newPhone.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(56.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("인증번호 발송", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PhoneVerifyingState(
    newPhone: String,
    code: String,
    verificationResult: Boolean?,
    secondsLeft: Int,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onResendClick: () -> Unit,
    onVerifyClick: () -> Unit
) {
    Column {
        // 번호 입력 + 재발송 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newPhone,
                onValueChange = { onPhoneChange(it.filter { ch -> ch.isDigit() || ch == '-' }) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                onClick = onResendClick,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp).width(100.dp)
            ) {
                Text("재발송", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 인증번호 라벨
        Text(
            text = "인증번호",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF6B767A),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // 인증번호 입력 + 확인 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) onCodeChange(it.filter(Char::isDigit)) },
                singleLine = true,
                placeholder = { Text("인증번호 6자리") },
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                onClick = onVerifyClick,
                enabled = code.length == 6,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp).width(100.dp)
            ) {
                Text("확인", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(8.dp))

        // 검증 결과 또는 타이머
        VerificationResultOrTimer(
            verificationResult = verificationResult,
            secondsLeft = secondsLeft
        )
    }
}

@Composable
private fun VerificationResultOrTimer(
    verificationResult: Boolean?,
    secondsLeft: Int
) {
    when (verificationResult) {
        true -> {
            Text(
                text = "인증번호 등록에 성공했습니다.",
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        false -> {
            Text(
                text = "올바른 인증번호가 아닙니다.",
                color = Color(0xFFD85B4E),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        null -> {
            val mm = (secondsLeft / 60).toString().padStart(2, '0')
            val ss = (secondsLeft % 60).toString().padStart(2, '0')
            val timeColor = if (secondsLeft in 1..180) Color(0xFFD85B4E) else Color(0xFF9AA3A7)

            Row(Modifier.fillMaxWidth()) {
                Text("남은 시간: ", color = Color(0xFF6B767A))
                Text("$mm:$ss", color = timeColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
