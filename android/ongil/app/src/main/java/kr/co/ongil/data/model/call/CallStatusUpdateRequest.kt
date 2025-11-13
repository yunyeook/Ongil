package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

@Serializable
data class CallStatusUpdateRequest(

    val status : String
)