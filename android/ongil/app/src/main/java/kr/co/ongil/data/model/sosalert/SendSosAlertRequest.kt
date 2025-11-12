package kr.co.ongil.data.model.sosalert

import kotlinx.serialization.Serializable

@Serializable
data class SendSosAlertRequest(
    val message: String? = null
)
