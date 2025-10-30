package kr.co.ongil.presentation.ui.Atest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.components.GreenButton

@Composable
fun PlayGroundMJ(
    modifier: Modifier = Modifier
) {
    // 클릭 여부 상태
    var clicked by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 현재 상태 텍스트
            Text(
                text = if (clicked) "Clicked!" else "Not clicked yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 우리가 만든 공용 컴포넌트
            GreenButton(
                text = "Click me",
                onClick = { clicked = true },
                modifier = Modifier
            )
            //새로운 컴포넌트 테스트해보고싶으면 아래에 추가하면 돼.
        }
    }
}

/**
 * 미리보기:
 * Android Studio Preview 탭에서 즉시 확인 가능
 * (앱 실행 없이도 PlayGroundMJScreen UI를 볼 수 있음)
 */
@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "PlayGroundMJ Preview"
)
@Composable
private fun PlayGroundMJPreview() {
    OngilTheme {
        PlayGroundMJ()
    }
}
