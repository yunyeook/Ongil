package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * VoIP 통화 "세션" 정보 (실시간)
 * /api/v1/calls, /api/v1/calls/{callId}, /status 응답에 사용
 */

@Serializable
data class VoipCallDto(
    val id: Long,                      // callId
    val callerId: Long? = null,        // nullable로 변경
    val receiverId: Long? = null,      // nullable로 변경
    val callType: String? = null,      // NORMAL / EMERGENCY
    val status: String? = null,        // CREATED / RINGING / CONNECTED / ENDED / ...
    val sessionId: String? = null,     // WebRTC 세션 ID
    val startedAt: String? = null,     // ISO-8601
    val connectedAt: String? = null,
    val endedAt: String? = null,
    val duration: Long? = null
)