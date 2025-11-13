package kr.co.ongil.data.model.sosalert

import kotlinx.serialization.Serializable

@Serializable
data class SendSosAlertResponse(
    val message: String,
    val data: SosData?
)

@Serializable
data class SosData(
    val sosId: Long,
    val senderId: Int,
    val receiverId: Int,
    val createdAt: String
)
