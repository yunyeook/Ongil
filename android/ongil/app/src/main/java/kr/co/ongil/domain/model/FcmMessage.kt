package kr.co.ongil.domain.model

// type을 String 대신 Enum으로 만들어 타입 안정성을 극대화
enum class MessageType {
    RELATIONSHIP_REGIST,
    SAFEZONE_EXIT,
    NAVIGATION_START,
    NAVIGATION_END,
    ABNORMAL_DETECTED,
    CALL_REQUEST,
    SOS,
    SOS_STOP,
    SOS_ACK,
    INCOMING_CALL,
    SAFEZONE_UPDATE
}

data class FcmMessage(
    val type: MessageType,
    val title: String,
    val senderId: String,
    val receiverId: String,
    val content: String?,
    val notificationId: String?,
    val relatedTableId: String?
)
