package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.presentation.uistate.*

/**
 * 통화 상세 정보 화면 ViewModel
 */
class CallDetailViewModel(
    // TODO: DI(Hilt/Koin)로 CallRepository 주입
    // private val callRepository: CallRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallDetailUiState())
    val uiState: StateFlow<CallDetailUiState> = _uiState.asStateFlow()

    /**
     * 이벤트 처리
     */
    fun onEvent(event: CallDetailEvent) {
        when (event) {
            is CallDetailEvent.LoadCallDetail -> {
                loadCallDetail(event.callLogId)
            }
        }
    }

    /**
     * 통화 상세 정보 로드
     */
    private fun loadCallDetail(callLogId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // TODO: Repository에서 통화 상세 정보 가져오기
                // val result = callRepository?.getCallDetail(callLogId)

                // Mock 데이터 사용
                val mockContactInfo = ContactInfo(
                    initials = "이지",
                    name = "이지현",
                    phone = "010-1234-5678",
                    badge = "VoIP",
                    isEmergency = true
                )

                val mockCallSummary = CallSummary(
                    direction = "발신 통화",
                    duration = "5분 12초",
                    location = "서울시 강남구 역삼동",
                    date = "2024년 10월 28일",
                    startTime = "오후 3:24:15",
                    endTime = "오후 3:29:27"
                )

                val mockMessages = listOf(
                    ChatMessage("CareLink 응급상담센터입니다. 어떤 도움이 필요하신가요?", "15:24", isMine = false),
                    ChatMessage("아, 안녕하세요. 갑자기 가슴이 너무 아파서… 숨쉬기도 힘들어요.", "15:24", isMine = true),
                    ChatMessage("네, 상황을 파악하겠습니다. 현재 몇 살이시고, 기존에 앓고 계신 질병이 있으신가요?", "15:24", isMine = false),
                    ChatMessage("57세이고… 고혈압 약을 먹고 있어요. 가슴 가운데가 꽉 조이는 것 같아요.", "15:25", isMine = true),
                    ChatMessage("알겠습니다. 지금 계시는 위치를 알려주시고, 즉시 119에 연계해드리겠습니다.", "15:25", isMine = false),
                    ChatMessage("서울시 강남구 역삼동이에요… 제발 빨리 도와주세요.", "15:26", isMine = true)
                )

                _uiState.update {
                    it.copy(
                        contactInfo = mockContactInfo,
                        callSummary = mockCallSummary,
                        messages = mockMessages,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "통화 상세 정보를 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }
}
