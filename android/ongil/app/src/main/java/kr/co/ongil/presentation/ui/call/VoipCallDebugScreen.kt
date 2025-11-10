package kr.co.ongil.presentation.ui.call

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 네비게이션 붙이기 전용: VOIP 통화 기능 통합 테스트 화면
 * - 발신 / 수신 / 수락 / 종료 / 위치 전송 등을 수동으로 눌러볼 수 있음
 */
@Composable
fun VoipCallDebugScreen(
    viewModel: VoipCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // TODO: 실제 테스트 시 존재하는 ID / 타입으로 교체
    val testReceiverId = 2L       // 상대 userId
    val testCallId = uiState.call?.id ?: 0L // 생성된 callId로도 테스트 가능
    val userType = "PATIENT"      // 또는 "CAREGIVER" / "GUARDIAN"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("VOIP Debug Screen")

        Spacer(Modifier.height(16.dp))

        // 1. 발신: 통화 세션 생성
        Button(onClick = {
            viewModel.startVoipCall(
                receiverId = testReceiverId,
                userType = userType
            )
        }) {
            Text("▶ 앱 통화 발신 (startVoipCall)")
        }

        Spacer(Modifier.height(8.dp))

        // 2. 수신: callId 기준 조회 (여기선 uiState.call.id 또는 임시 값)
        Button(onClick = {
            if (testCallId != 0L) {
                viewModel.loadIncomingCall(testCallId)
            }
        }) {
            Text("📥 통화 정보 조회 (loadIncomingCall)")
        }

        Spacer(Modifier.height(8.dp))

        // 3. 통화 수락
        Button(onClick = {
            viewModel.acceptCall(userType = userType)
        }) {
            Text("✅ 통화 수락 (acceptCall)")
        }

        Spacer(Modifier.height(8.dp))

        // 4. 통화 종료
        Button(onClick = {
            viewModel.endCall()
        }) {
            Text("⏹ 통화 종료 (endCall)")
        }

        Spacer(Modifier.height(8.dp))

        // 5. 위치 테스트
        Button(onClick = {
            viewModel.fetchCurrentLocation()
        }) {
            Text("📍 현재 위치 가져오기 (fetchCurrentLocation)")
        }

        Spacer(Modifier.height(24.dp))

        Text("통화 ID: ${uiState.call?.id ?: "-"}")
        Text("상태: ${uiState.call?.status ?: "-"}")
        Text("메시지: ${uiState.message ?: "-"}")
        Text("에러: ${uiState.error ?: "-"}")
        Text("위치: ${uiState.currentLocation ?: "-"}")
    }
}