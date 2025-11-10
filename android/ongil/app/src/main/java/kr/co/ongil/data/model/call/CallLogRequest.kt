package kr.co.ongil.data.model.call

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 통화 로그 생성 요청 DTO
 */
@Serializable
data class CallLogRequest(
    @SerialName("receiverId")
    val receiverId: Long? = null,

    @SerialName("receiverPhoneNumber")
    val receiverPhoneNumber: String? = null,

    @SerialName("callType")
    val callType: String,

    @SerialName("source")
    val source: String,

    @SerialName("patientState")
    val patientState: String,

    @SerialName("patientLocation")
    val patientLocation: CallLocationDto,

    @SerialName("startedAt")
    val startedAt: String,

    @SerialName("endedAt")
    val endedAt: String? = null,

    @SerialName("duration")
    val duration: Int? = null,

    @SerialName("memo")
    val memo: String? = null
)
