package kr.co.ongil.presentation.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun InputBox(
    label: String,                  // 인풋박스 라벨
    placeholder: String = "",       // 플레이스홀더
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val focusManager = LocalFocusManager.current

    // 둥근 모서리 모양을 변수로 추출하여 재사용합니다.
    val shape = RoundedCornerShape(8.dp)

    // --- 수정된 부분 ---
    // enabled 상태에 따라 배경색을 결정합니다.
    // 활성화 상태: 흰색, 비활성화 상태: 연한 회색
    val backgroundColor = if (enabled) Color.White else Color(0xFFF5F5F5)
    // ------------------

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(shape) // 배경이 삐져나오지 않도록 clip 적용
                .border(1.dp, Color(0xFFD6D7DA), shape),
            shape = shape,
            colors = TextFieldDefaults.colors(
                // 위에서 결정된 backgroundColor를 각 상태에 맞게 적용합니다.
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,
                disabledContainerColor = backgroundColor,
                cursorColor = Color(0xFF374151),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true
        )
    }
}


@Preview(showBackground = true, name = "Enabled State")
@Composable
fun InputBoxEnabledPreview(){
    InputBox(label = "이름 (활성화)", placeholder = "이름을 입력하세요", value = "", onValueChange = {})
}

@Preview(showBackground = true, name = "Disabled State")
@Composable
fun InputBoxDisabledPreview(){
    InputBox(label = "전화번호 (비활성화)", value = "010-1234-5678", onValueChange = {}, enabled = false)
}
