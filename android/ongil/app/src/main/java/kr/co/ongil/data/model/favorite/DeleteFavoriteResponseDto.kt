package kr.co.ongil.data.model.favorite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteFavoriteResponseDto(
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: String = ""  // 서버에서 빈 문자열 ""을 반환
)