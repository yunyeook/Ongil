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
    val type: String,                   // SignalingType 문자열
    val callId: Long? = null,           // 통화 ID
    val sessionId: String? = null,      // 세션 ID (백엔드 요구사항)
    val senderId: Long? = null,         // 발신자 ID
    val receiverId: Long? = null,       // 수신자 ID
    val callerName: String? = null,     // 발신자 이름 (INCOMING)
    val callerPhone: String? = null,    // 발신자 전화번호 (INCOMING)
    val callType: String? = null,       // 통화 타입 (INCOMING)
    val sdp: String? = null,            // SDP (OFFER/ANSWER)
    val sdpType: String? = null,        // SDP 타입 ("offer" 또는 "answer")
    val candidate: String? = null,      // ICE Candidate 문자열
    val sdpMid: String? = null,         // SDP media stream ID
    val sdpMLineIndex: Int? = null,     // SDP m-line index
    val reason: String? = null          // 종료 사유 (HANGUP)
)
