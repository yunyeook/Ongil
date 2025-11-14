package kr.co.ongil.wear.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

/**
 * 로그인 후 메인 화면
 *
 * 로그인 성공 시 표시되는 화면
 *
 * @param userId 사용자 ID
 * @param userType 사용자 타입 (PATIENT or GUARDIAN)
 */
@Composable
fun MainScreen(
    userId: String?,
    userType: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 환영 메시지
        Text(
            text = "온길 워치",
            style = MaterialTheme.typography.title2,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 사용자 정보
        Text(
            text = when(userType) {
                "PATIENT" -> "환자"
                "GUARDIAN" -> "보호자"
                else -> "사용자"
            },
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 사용자 ID (앞 4자리만)
        userId?.let {
            Text(
                text = "ID: ${it.take(4)}...",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 안내 메시지
        Text(
            text = "로그인 완료!",
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.secondary
        )
    }
}
