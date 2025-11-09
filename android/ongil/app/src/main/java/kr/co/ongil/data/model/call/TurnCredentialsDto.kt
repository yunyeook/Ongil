package kr.co.ongil.data.model.call

data class TurnCredentialsDto(
    val username: String,
    val credential: String,
    val ttl: Long,
    val uris: List<String>
)