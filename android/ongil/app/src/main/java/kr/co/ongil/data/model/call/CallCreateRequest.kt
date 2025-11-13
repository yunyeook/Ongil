package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

@Serializable
data class CallCreateRequest (

    val receiverId: Long,
    val callType : String //NORMAL / EMARGENCY

)