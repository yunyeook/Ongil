package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * VoIP 통화 "세션" 정보 (실시간)
 * /api/v1/calls, /api/v1/calls/{callId}, /status 응답에 사용
 */

@Serializable
data class VoipCallDto(
    val id: Long,                      // callId
    val caller: UserInfo? = null,      // 발신자 정보 객체
    val receiver: UserInfo? = null,    // 수신자 정보 객체
    val callType: String? = null,      // NORMAL / EMERGENCY
    val status: String? = null,        // CREATED / RINGING / CONNECTED / ENDED / ...
    val sessionId: String? = null,     // WebRTC 세션 ID
    val startedAt: String? = null,     // ISO-8601
    val connectedAt: String? = null,
    val endedAt: String? = null,
    val duration: Long? = null
) {
    val callerId: Long? get() = caller?.id
    val receiverId: Long? get() = receiver?.id
}

@Serializable
data class UserInfo(
    val id: Long,
    val name: String? = null,
    val phoneNumber: String? = null
)