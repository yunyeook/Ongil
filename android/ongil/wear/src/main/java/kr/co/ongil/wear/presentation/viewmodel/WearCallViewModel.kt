package kr.co.ongil.wear.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.wear.domain.model.CallState
import kr.co.ongil.wear.domain.model.CallStatus
import kr.co.ongil.wear.domain.repository.CallRepository
import javax.inject.Inject

/**
 * Wear OS VoIP 통화 ViewModel (블루투스 모델)
 *
 * 주요 기능:
 * - 통화 UI 상태 관리
 * - Watch → Phone 통화 요청 전송 (CallRepository → WearDataClient)
 * - 통화 타이머 관리
 *
 * 참고:
 * - WebRTC는 Phone에서 처리 (Watch는 UI만)
 * - Phone의 VoipCallViewModel이 Watch로 상태를 sync해야 함 (TODO)
 */
@HiltViewModel
class WearCallViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {

    companion object {
        private const val TAG = "WearCallViewModel"
    }

    // === UI 상태 관리 ===

    private val _uiState = MutableStateFlow(VoipCallUiState())
    val uiState: StateFlow<VoipCallUiState> = _uiState.asStateFlow()

    private var callTimerJob: Job? = null

    // === 통화 시작 (발신) ===

    /**
     * VoIP 통화 시작 (Phone으로 요청 전송)
     *
     * @param targetUserId 상대방 사용자 ID
     * @param targetName 상대방 이름
     * @param targetPhone 상대방 전화번호
     */
    fun startVoipCall(
        targetUserId: String,
        targetName: String,
        targetPhone: String
    ) {
        Log.d(TAG, "=== [WATCH CALLER] startVoipCall: to=$targetUserId")

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    message = "통화 연결 중...",
                    error = null
                )
            }

            try {
                // Phone으로 통화 시작 요청 전송
                val callId = callRepository.createCall(
                    targetUserId = targetUserId,
                    targetName = targetName,
                    targetPhone = targetPhone
                ).getOrThrow()

                Log.d(TAG, "✓ Call request sent to Phone: callId=$callId")

                // UI 상태 업데이트
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        callState = CallState(
                            callId = callId,
                            targetUserId = targetUserId,
                            targetName = targetName,
                            targetPhone = targetPhone,
                            status = CallStatus.CALLING,
                            isIncoming = false
                        ),
                        message = "상대방 응답 대기 중..."
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "startVoipCall failed: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "통화 시작 실패"
                    )
                }
            }
        }
    }

    // === 통화 수락 (수신) ===

    /**
     * 수신 통화 로드 (Phone에서 sync 받은 정보)
     *
     * @param callId 통화 ID
     * @param callerUserId 발신자 사용자 ID
     * @param callerName 발신자 이름
     */
    fun loadIncomingCall(
        callId: Long,
        callerUserId: String,
        callerName: String
    ) {
        Log.d(TAG, "=== [WATCH CALLEE] loadIncomingCall: callId=$callId, from=$callerUserId")

        _uiState.update {
            it.copy(
                callState = CallState(
                    callId = callId,
                    targetUserId = callerUserId,
                    targetName = callerName,
                    status = CallStatus.RINGING,
                    isIncoming = true
                ),
                message = "수신 중..."
            )
        }
    }

    /**
     * 통화 수락 (Phone으로 전송)
     */
    fun acceptCall() {
        val callState = _uiState.value.callState ?: run {
            Log.w(TAG, "acceptCall: callState is null")
            return
        }

        Log.d(TAG, "=== [WATCH CALLEE] acceptCall: callId=${callState.callId}")

        viewModelScope.launch {
            try {
                // Phone으로 통화 수락 전송
                callRepository.updateCallStatus(
                    callId = callState.callId!!,
                    status = "ACCEPTED"
                ).getOrThrow()

                Log.d(TAG, "✓ Accept signal sent to Phone")

                // UI 상태 업데이트
                _uiState.update {
                    it.copy(
                        callState = callState.copy(status = CallStatus.CONNECTED),
                        message = "통화 연결됨"
                    )
                }

                // 타이머 시작
                startTimer()

            } catch (e: Exception) {
                Log.e(TAG, "acceptCall failed: ${e.message}", e)
                _uiState.update {
                    it.copy(error = e.message ?: "통화 수락 실패")
                }
            }
        }
    }

    // === 통화 거절 ===

    /**
     * 통화 거절 (Phone으로 전송)
     */
    fun rejectCall() {
        val callState = _uiState.value.callState ?: run {
            Log.w(TAG, "rejectCall: callState is null")
            return
        }

        Log.d(TAG, "=== [WATCH CALLEE] rejectCall: callId=${callState.callId}")

        viewModelScope.launch {
            try {
                // Phone으로 통화 거절 전송
                callRepository.updateCallStatus(
                    callId = callState.callId!!,
                    status = "REJECTED"
                ).getOrThrow()

                Log.d(TAG, "✓ Reject signal sent to Phone")

                // UI 상태 초기화
                resetCallState()

            } catch (e: Exception) {
                Log.e(TAG, "rejectCall failed: ${e.message}", e)
                _uiState.update {
                    it.copy(error = e.message ?: "통화 거절 실패")
                }
            }
        }
    }

    // === 통화 종료 ===

    /**
     * 통화 종료 (Phone으로 전송)
     */
    fun endCall() {
        val callState = _uiState.value.callState ?: run {
            Log.w(TAG, "endCall: callState is null")
            return
        }

        Log.d(TAG, "=== [WATCH] endCall: callId=${callState.callId}")

        viewModelScope.launch {
            try {
                // Phone으로 통화 종료 전송
                callRepository.updateCallStatus(
                    callId = callState.callId!!,
                    status = "ENDED"
                ).getOrThrow()

                Log.d(TAG, "✓ End signal sent to Phone")

                // 타이머 중지 및 상태 초기화
                stopTimer()
                resetCallState()

            } catch (e: Exception) {
                Log.e(TAG, "endCall failed: ${e.message}", e)
                _uiState.update {
                    it.copy(error = e.message ?: "통화 종료 실패")
                }
            }
        }
    }

    // === 타이머 관리 ===

    /**
     * 통화 타이머 시작
     */
    private fun startTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            var seconds = 0L
            while (true) {
                delay(1000)
                seconds++
                _uiState.update { state ->
                    state.callState?.let { callState ->
                        state.copy(callState = callState.copy(duration = seconds))
                    } ?: state
                }
            }
        }
        Log.d(TAG, "통화 타이머 시작")
    }

    /**
     * 통화 타이머 중지
     */
    private fun stopTimer() {
        callTimerJob?.cancel()
        callTimerJob = null
        Log.d(TAG, "통화 타이머 중지")
    }

    // === 상태 초기화 ===

    /**
     * 통화 상태 초기화
     */
    private fun resetCallState() {
        _uiState.update {
            it.copy(
                callState = null,
                message = null,
                error = null
            )
        }
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // === ViewModel 파괴 ===

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        Log.d(TAG, "WearCallViewModel cleared")
    }
}

/**
 * VoIP 통화 UI 상태
 */
data class VoipCallUiState(
    val callState: CallState? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
