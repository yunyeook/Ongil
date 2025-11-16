package kr.co.ongil.wear.domain.model

/**
 * 통화 상태 Domain Model
 */
data class CallState(
    val callId: Long? = null,
    val targetUserId: String? = null,
    val targetName: String? = null,
    val targetPhone: String? = null,
    val status: CallStatus = CallStatus.IDLE,
    val duration: Long = 0L, // 통화 시간 (초)
    val isIncoming: Boolean = false
)

/**
 * 통화 상태 enum
 */
enum class CallStatus {
    IDLE,           // 통화 없음
    CALLING,        // 발신 중
    RINGING,        // 수신 중
    CONNECTING,     // 연결 중
    CONNECTED,      // 통화 중
    ENDED,          // 통화 종료
    FAILED          // 통화 실패
}

/**
 * TURN 서버 인증 정보 Domain Model
 */
data class TurnCredentials(
    val urls: List<String>,
    val username: String,
    val credential: String
)
