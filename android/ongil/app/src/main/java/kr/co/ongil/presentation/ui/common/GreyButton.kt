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







@Composable
fun GreyButton(
    modifier: Modifier = Modifier,
    text: String, // 버튼 안에 들어갈 내용
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(327.dp)
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFDFE0E2),
            contentColor = Color.Black,
        )
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
fun GreyButtonPreview() {
    GreyButton(text = "버튼", onClick = {})
}