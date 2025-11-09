package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

@Serializable
data class TurnCredentialsDto(
    val username: String,
    val credential: String,
    val ttl: Long,
    val uris: List<String>
)