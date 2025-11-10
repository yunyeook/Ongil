package kr.co.ongil.presentation.ui.call

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun VoipCallTestScreen(
    viewModel: VoipCallViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var receiverId by remember { mutableStateOf("2") }
    var callIdInput by remember { mutableStateOf("") }
    var selectedUserType by remember { mutableStateOf("CAREGIVER") }
    var selectedCallType by remember { mutableStateOf("NORMAL") }

    // 위치 권한 요청 런처
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // 권한이 허용되면 위치 가져오기
            viewModel.fetchCurrentLocation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "VoIP 통화 테스트",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        // 테스트 설정 섹션
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("테스트 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                // Receiver ID 입력 (발신용)
                OutlinedTextField(
                    value = receiverId,
                    onValueChange = { receiverId = it },
                    label = { Text("수신자 ID (발신용)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 2") }
                )

                // Call ID 입력 (수신용)
                OutlinedTextField(
                    value = callIdInput,
                    onValueChange = { callIdInput = it },
                    label = { Text("통화 ID (수신용)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 123") }
                )

                // User Type 선택
                Text("내 역할:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedUserType == "CAREGIVER",
                        onClick = { selectedUserType = "CAREGIVER" },
                        label = { Text("보호자") }
                    )
                    FilterChip(
                        selected = selectedUserType == "PATIENT",
                        onClick = { selectedUserType = "PATIENT" },
                        label = { Text("환자 (위치전송)") }
                    )
                }

                // Call Type 선택
                Text("통화 타입:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCallType == "NORMAL",
                        onClick = { selectedCallType = "NORMAL" },
                        label = { Text("일반") }
                    )
                    FilterChip(
                        selected = selectedCallType == "EMERGENCY",
                        onClick = { selectedCallType = "EMERGENCY" },
                        label = { Text("긴급") }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text("테스트 순서", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // 발신 테스트 버튼
        Button(
            onClick = {
                val id = receiverId.toLongOrNull() ?: 2L
                viewModel.startVoipCall(
                    receiverId = id,
                    userType = selectedUserType,
                    callType = selectedCallType
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("1. 통화 생성 (startVoipCall)")
        }

        // 수신자 입장에서 callId로 조회 테스트
        Button(
            onClick = {
                // callIdInput 우선, 없으면 state.call?.id 사용
                val id = callIdInput.toLongOrNull() ?: state.call?.id
                if (id != null) {
                    viewModel.loadIncomingCall(id)
                }
            },
            enabled = callIdInput.isNotBlank() || state.call != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("2. 통화 조회 (loadIncomingCall)")
        }

        // 수락 테스트
        Button(
            onClick = {
                viewModel.acceptCall(userType = selectedUserType)
            },
            enabled = state.call != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("3. 통화 수락 (acceptCall)")
        }

        // 종료 테스트
        Button(
            onClick = {
                viewModel.endCall()
            },
            enabled = state.call != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD85B4E))
        ) {
            Text("4. 통화 종료 (endCall)")
        }

        Spacer(Modifier.height(16.dp))

        // 현재 위치 정보 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📍 현재 위치",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            // 위치 권한 요청
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("새로고침", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text(
                    state.currentLocation ?: "위치 정보 없음 (새로고침 버튼을 눌러주세요)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.currentLocation != null) Color(0xFF1565C0) else Color.Gray
                )
            }
        }

        // 현재 상태 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (state.error != null) Color(0xFFFFF3F3)
                else if (state.message != null) Color(0xFFF0F9F4)
                else Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "현재 상태",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (state.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("로딩 중...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (state.call != null) {
                    HorizontalDivider()
                    StatusRow("통화 ID", state.call?.id?.toString() ?: "-")
                    StatusRow("상태", state.call?.status ?: "-",
                        color = when(state.call?.status) {
                            "CREATED" -> Color(0xFF6B767A)
                            "RINGING" -> Color(0xFF2196F3)
                            "CONNECTED" -> Color(0xFF4CAF50)
                            "ENDED" -> Color(0xFF9E9E9E)
                            else -> Color.Unspecified
                        }
                    )
                    StatusRow("발신자 ID", state.call?.callerId?.toString() ?: "-")
                    StatusRow("수신자 ID", state.call?.receiverId?.toString() ?: "-")
                    StatusRow("통화 타입", state.call?.callType ?: "-")
                    if (state.call?.sessionId != null) {
                        StatusRow("세션 ID", state.call?.sessionId ?: "-")
                    }
                }

                if (state.message != null) {
                    HorizontalDivider()
                    Text(
                        "✓ ${state.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32)
                    )
                }

                if (state.error != null) {
                    HorizontalDivider()
                    Text(
                        "✗ ${state.error}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD32F2F)
                    )
                }

                if (state.call == null && !state.isLoading && state.error == null) {
                    Text(
                        "통화 정보 없음",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 테스트 가이드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "📝 테스트 가이드",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    """
                    [발신자 (통화 거는 쪽)]
                    1. 수신자 ID에 실제 존재하는 사용자 ID 입력
                    2. 역할과 통화 타입 선택
                    3. "1. 통화 생성" 클릭
                    4. 화면에 표시된 통화 ID 확인

                    [수신자 (통화 받는 쪽)]
                    1. 통화 ID 입력란에 발신자 화면의 통화 ID 입력
                    2. "2. 통화 조회" 클릭
                    3. "3. 통화 수락" 클릭하면 "CONNECTED"로 변경
                    4. 환자 역할 선택 시 자동으로 위치 전송됨
                    5. "4. 통화 종료"로 테스트 완료
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF856404)
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
