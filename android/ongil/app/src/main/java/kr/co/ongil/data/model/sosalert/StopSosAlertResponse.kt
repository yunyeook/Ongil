package kr.co.ongil.data.model.sosalert

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class StopSosAlertResponse(
    val message: String,
    val data: JsonElement? = null  // 서버가 빈 문자열이나 객체를 보낼 수 있음
)
