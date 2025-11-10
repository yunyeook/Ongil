package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

@Serializable
data class CallStartLocationRequest (

    val latitude: Double,
    val longitude: Double,
    val source: String,        // GPS / NETWORK / ...
    val accuracy: Double? = null,
    val timestamp: String      // ISO-8601

)