package kr.co.ongil.presentation.ui.common

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.theme.ongilColors


@Composable
fun GreenButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    isActive: Boolean = true,  // 얘는 기본목적지 설정처럼 토글 느낌으로
    enabled: Boolean = true,
    containerColor: Color = ongilColors.accent,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(327.dp)
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) containerColor else Color(0xFFD3D3D3),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFD3D3D3)
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreenButtonPreview_On() {
    GreenButton(text = "기본 목적지", isActive = true, onClick = {})
}

@Preview(showBackground = true)
@Composable
fun GreenButtonPreview_Off() {
    GreenButton(text = "기본 목적지", isActive = false, onClick = {})
}