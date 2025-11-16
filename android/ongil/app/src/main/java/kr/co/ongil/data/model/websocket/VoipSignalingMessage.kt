package kr.co.ongil.data.model.websocket

import kotlinx.serialization.Serializable

/**
 * WebRTC 시그널링 메시지 타입 (백엔드 스펙)
 */
enum class SignalingType {
    INCOMING,       // 수신 통화
    OFFER,          // SDP Offe
    ANSWER,         // SDP Answer
    ICE,            // ICE Candidate
    ACCEPT,         // 통화 수락
    REJECT,         // 통화 거절
    HANGUP          // 통화 종료
}

/**
 * WebRTC 시그널링 메시지 (백엔드 스펙)
 */
////
@Serializable
data class SignalMessage(
    val type: String,
    val callId: Long? = null,
    val sessionId: String? = null,
    val senderId: Long? = null,         // ✅ 유지 (클라이언트 → 서버)
    val receiverId: Long? = null,       // ✅ 유지 (클라이언트 → 서버)
    val fromUserId: Long? = null,       // ✅ 추가 (서버 → 클라이언트)
    val toUserId: Long? = null,         // ✅ 추가 (서버 → 클라이언트)
    val callerName: String? = null,
    val callerPhone: String? = null,
    val callType: String? = null,
    val sdp: String? = null,
    val sdpType: String? = null,
    val candidate: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    val reason: String? = null
) {
    // ✅ 실제 발신자 ID 반환 (서버에서 온 fromUserId 우선)
    val actualSenderId: Long? get() = fromUserId ?: senderId
    val actualReceiverId: Long? get() = toUserId ?: receiverId
}