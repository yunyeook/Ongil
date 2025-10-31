package kr.co.ongil.presentation.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.ui.common.GreenButton

@Composable
fun HomeScreen(onGoFavoriteClick: () -> Unit) {



        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "홈 화면",
                fontSize = 20.sp,
                color = Color(0xFF111827)
            )
        }
        GreenButton(
            text = "즐겨찾기로 이동",
            onClick = onGoFavoriteClick

        )
    }


@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onGoFavoriteClick = {}
    )
}