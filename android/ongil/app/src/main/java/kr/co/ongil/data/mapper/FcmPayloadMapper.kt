package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.fcm.FcmPayloadDto
import kr.co.ongil.domain.model.FcmMessage
import kr.co.ongil.domain.model.MessageType

object FcmPayloadMapper {

    fun toDomain(dto: FcmPayloadDto): FcmMessage {
        return FcmMessage(
            type = when (dto.type) {
                "RELATIONSHIP_REGIST" -> MessageType.RELATIONSHIP_REGIST
                "SAFEZONE_EXIT" -> MessageType.SAFEZONE_EXIT
                "NAVIGATION_START" -> MessageType.NAVIGATION_START
                "NAVIGATION_END" -> MessageType.NAVIGATION_END
                "ABNORMAL_DETECTED" -> MessageType.ABNORMAL_DETECTED
                "CALL_REQUEST" -> MessageType.CALL_REQUEST
                "SOS" -> MessageType.SOS
                "INCOMING_CALL" -> MessageType.INCOMING_CALL
                else -> throw IllegalArgumentException("Unknown type: ${dto.type}")
            },
            title = dto.title ?: "",
            senderId = dto.senderId ?: "",
            receiverId = dto.receiverId ?: "",
            content = dto.content ?: ""
        )
    }

}