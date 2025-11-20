package kr.co.ongil.data.model.map

import kotlinx.serialization.Serializable

/**
 * 길안내 종료 응답
 */
@Serializable
data class NavigationEndResponse(
    val message: String,
    val data: NavigationEndData
)

@Serializable
data class NavigationEndData(
    val navigationId: String,
    val startedAt: String,
    val endedAt: String,
    val isSuccessful: Boolean,
    val durationSeconds: Long
)
