package kr.co.ongil.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource


@Composable
fun AlertModal(
    onDismiss: () -> Unit,
    icon: Painter? = null,               // 상단 아이콘 이미지 (nullable)
    message: String,                     // 안내 문구
    buttonText: String = "확인",          // 버튼 텍스트
    onButtonClick: () -> Unit,           // 버튼 클릭 이벤트
) {
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
                // 상단 원형 아이콘 -> 여기에 체크표시나 뭐 다른거 넣어야됨
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Color(0xFF8CA898), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Image(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 안내 문구
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(horizontal = 12.dp),
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 하단 버튼
                Button(
                    onClick = onButtonClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8CA898),
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

@Preview(showBackground = true)
@Composable
fun AlertModalPreview() {
    AlertModal(icon = null, message = "안내 문구", onDismiss = {}, onButtonClick = {})
}