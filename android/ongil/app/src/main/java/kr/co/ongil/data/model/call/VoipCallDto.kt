package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * 통화 참여자 정보
 */
@Serializable
data class CallUserDto(
    val id: Long,
    val name: String? = null,
    val phoneNumber: String? = null
)

/**
 * VoIP 통화 "세션" 정보 (실시간)
 * /api/v1/calls, /api/v1/calls/{callId}, /status 응답에 사용
 */
@Serializable
data class VoipCallDto(
    val id: Long,                          // callId
    val caller: CallUserDto? = null,       // 발신자 정보
    val receiver: CallUserDto? = null,     // 수신자 정보
    val callType: String? = null,          // NORMAL / EMERGENCY
    val status: String? = null,            // CREATED / RINGING / CONNECTED / ENDED / ...
    val sessionId: String? = null,         // WebRTC 세션 ID
    val startedAt: String? = null,         // ISO-8601
    val connectedAt: String? = null,
    val endedAt: String? = null,
    val duration: Long? = null
) {
    // 기존 코드 호환성을 위한 computed properties
    val callerId: Long? get() = caller?.id
    val receiverId: Long? get() = receiver?.id
}
