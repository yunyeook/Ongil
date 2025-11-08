package kr.co.ongil.presentation.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import kr.co.ongil.presentation.ui.common.GreenButton

@Composable
fun HomeScreen(
    onGoSearchUserClick: () -> Unit = {},
    onGoSignupClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9FAFB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "홈 화면",
                fontSize = 20.sp,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(32.dp))

            GreenButton(
                text = "사용자 찾기",
                onClick = onGoSearchUserClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            GreenButton(
                text = "회원가입",
                onClick = onGoSignupClick
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onGoSearchUserClick = {},
        onGoSignupClick = {}
    )
}