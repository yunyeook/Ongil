package kr.co.ongil.presentation.ui.Atest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kr.co.ongil.presentation.ui.map.TMapComposable

@Composable
fun PlayGroundGK() {
    Box(modifier = Modifier.fillMaxSize()) {
        TMapComposable(
            latitude = 37.5665,  // 서울 시청
            longitude = 126.9780,
            zoomLevel = 15
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayGroundGKPreview() {
    PlayGroundGK()
}