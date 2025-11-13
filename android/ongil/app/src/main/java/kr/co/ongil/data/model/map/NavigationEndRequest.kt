package kr.co.ongil.data.model.map

import kotlinx.serialization.Serializable

/**
 * 길안내 종료 요청
 * POST /api/v1/navigation/end
 */
@Serializable
data class NavigationEndRequest(
    val patientId: Long,
    val navigationId: Int,
    val isSuccessful: Boolean
)
