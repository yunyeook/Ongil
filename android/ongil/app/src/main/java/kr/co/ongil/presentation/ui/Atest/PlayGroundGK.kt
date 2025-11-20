package kr.co.ongil.presentation.ui.Atest

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.ongil.domain.model.FcmMessage
import kr.co.ongil.domain.model.MessageType
import kr.co.ongil.presentation.handler.SosActionHandlerImpl
import kr.co.ongil.presentation.ui.map.TMapComposable

@Composable
fun PlayGroundGK() {
    val context = LocalContext.current
    val sosHandler = SosActionHandlerImpl()

    Box(modifier = Modifier.fillMaxSize()) {
        TMapComposable(
            zoomLevel = 15
        )

        // SOS 테스트 버튼
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    // 테스트용 FCM 메시지 생성
                    val testMessage = FcmMessage(
                        type = MessageType.SOS,
                        title = "테스트 SOS",
                        senderId = "1",
                        receiverId = "2",
                        content = null,
                        notificationId = null,
                        relatedTableId = null
                    )
                    sosHandler.startSosAction(context, testMessage)
                }
            ) {
                Text("🚨 SOS 사운드 테스트 (100번 반복)")
            }

            Text(
                text = "버튼을 누르면 SOS 사운드가 100번 반복 재생됩니다",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayGroundGKPreview() {
    PlayGroundGK()
}