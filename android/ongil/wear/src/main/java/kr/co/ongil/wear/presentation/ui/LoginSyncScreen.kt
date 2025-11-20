package kr.co.ongil.wear.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

/**
 * 로그인 동기화 대기 화면
 *
 * 앱에서 로그인 정보를 받을 때까지 표시되는 화면
 *
 * 스프링의 로딩 페이지와 비슷
 */
@Composable
fun LoginSyncScreen() {
    // 화면 전체를 차지하는 Column (세로 정렬)
    Column(
        modifier = Modifier
            .fillMaxSize()  // 화면 전체 크기
            .padding(16.dp),  // 여백 16dp
        horizontalAlignment = Alignment.CenterHorizontally,  // 가로 중앙 정렬
        verticalArrangement = Arrangement.Center  // 세로 중앙 정렬
    ) {
        // 로딩 스피너
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )

        // 간격
        Spacer(modifier = Modifier.height(16.dp))

        // 안내 텍스트
        Text(
            text = "로그인 정보\n동기화 중...",
            style = MaterialTheme.typography.body1,  // 본문 텍스트 스타일
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onBackground // 배경 위 텍스트 색상
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 부가 설명
        Text(
            text = "폰 앱에서\n로그인해주세요",
            style = MaterialTheme.typography.caption2,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
        )
    }
}