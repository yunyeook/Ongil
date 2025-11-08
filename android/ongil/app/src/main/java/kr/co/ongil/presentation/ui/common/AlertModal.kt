package kr.co.ongil.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

enum class AlertType {
    SUCCESS,
    ERROR
}

@Composable
fun AlertModal(
    onDismiss: () -> Unit,
    icon: Painter? = null,               // 상단 아이콘 이미지 (nullable, 커스텀 아이콘)
    message: String,                     // 안내 문구
    buttonText: String = "확인",          // 버튼 텍스트
    onButtonClick: () -> Unit,           // 버튼 클릭 이벤트
    type: AlertType = AlertType.SUCCESS  // 성공/실패 타입
) {
    // 타입에 따른 색상 설정
    val iconBackgroundColor = when (type) {
        AlertType.SUCCESS -> Color(0xFF8CA898)  // 초록색
        AlertType.ERROR -> Color(0xFFD32F2F)    // 붉은색
    }

    val buttonColor = when (type) {
        AlertType.SUCCESS -> Color(0xFF8CA898)
        AlertType.ERROR -> Color(0xFFD32F2F)
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 상단 원형 아이콘
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(iconBackgroundColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Image(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        // 기본 아이콘 (성공: 체크, 실패: X)
                        Icon(
                            imageVector = when (type) {
                                AlertType.SUCCESS -> Icons.Filled.Check
                                AlertType.ERROR -> Icons.Filled.Close
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 안내 문구 (가운데 정렬)
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 하단 버튼
                Button(
                    onClick = onButtonClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Success Modal")
@Composable
fun AlertModalSuccessPreview() {
    AlertModal(
        icon = null,
        message = "회원가입이 성공적으로 완료되었습니다.",
        onDismiss = {},
        onButtonClick = {},
        type = AlertType.SUCCESS
    )
}

@Preview(showBackground = true, name = "Error Modal")
@Composable
fun AlertModalErrorPreview() {
    AlertModal(
        icon = null,
        message = "회원가입에 실패했습니다.\n다시한번 시도해주세요.",
        onDismiss = {},
        onButtonClick = {},
        type = AlertType.ERROR
    )
}