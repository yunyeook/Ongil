package kr.co.ongil.wear.presentation.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.*
import kr.co.ongil.wear.presentation.viewmodel.HelpRequestViewModel

/**
 * 도움 요청 화면
 *
 * 주요 기능:
 * 1. "도움 요청" 큰 버튼
 * 2. TTS 음성 재생 ("도와주세요!")
 * 3. Phone으로 SOS 알림 전송
 * 4. 요청 상태 표시
 */
@Composable
fun HelpRequestScreen(
    viewModel: HelpRequestViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // 성공 시 자동으로 몇 초 후 돌아가기
    LaunchedEffect(uiState.isHelpRequestSent) {
        if (uiState.isHelpRequestSent) {
            kotlinx.coroutines.delay(3000) // 3초 대기
            onBackPressed()
        }
    }

    Scaffold(
        timeText = {
            TimeText()
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 로딩 중
                uiState.isLoading -> {
                    LoadingState()
                }

                // 성공
                uiState.isHelpRequestSent -> {
                    SuccessState()
                }

                // 에러
                uiState.errorMessage != null -> {
                    ErrorState(
                        errorMessage = uiState.errorMessage ?: "오류 발생",
                        onRetry = {
                            viewModel.clearError()
                        }
                    )
                }

                // 기본 상태
                else -> {
                    DefaultState(
                        isTtsSpeaking = uiState.isTtsSpeaking,
                        ttsInitialized = uiState.ttsInitialized,
                        onHelpRequest = {
                            viewModel.requestHelp()
                        }
                    )
                }
            }
        }
    }
}

/**
 * 기본 상태 - 도움 요청 버튼
 */
@Composable
private fun DefaultState(
    isTtsSpeaking: Boolean,
    ttsInitialized: Boolean,
    onHelpRequest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 제목
        Text(
            text = "긴급 도움 요청",
            style = MaterialTheme.typography.title3,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 도움 요청 버튼
        Button(
            onClick = onHelpRequest,
            enabled = ttsInitialized && !isTtsSpeaking,
            modifier = Modifier
                .size(120.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isTtsSpeaking) "음성 재생 중..." else "도움 요청",
                    style = MaterialTheme.typography.title3,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 안내 메시지
        Text(
            text = "버튼을 누르면\n\"도와주세요!\" 음성과\n보호자에게 알림이 전송됩니다",
            style = MaterialTheme.typography.caption1,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        if (!ttsInitialized) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "음성 기능 초기화 중...",
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 로딩 상태
 */
@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "전송 중...",
            style = MaterialTheme.typography.title3,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 성공 상태
 */
@Composable
private fun SuccessState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 체크 아이콘 (텍스트로 대체)
        Text(
            text = "✓",
            style = MaterialTheme.typography.display1,
            color = Color.Green
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "도움 요청 완료",
            style = MaterialTheme.typography.title2,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "보호자에게\n알림이 전송되었습니다",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 에러 상태
 */
@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "오류 발생",
            style = MaterialTheme.typography.title3,
            color = MaterialTheme.colors.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 닫기 버튼
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
        ) {
            Text("닫기")
        }
    }
}
