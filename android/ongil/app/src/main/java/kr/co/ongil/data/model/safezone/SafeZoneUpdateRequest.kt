package kr.co.ongil.data.model.safezone

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * 안전범위 수정 요청 DTO
 */
@Serializable
data class SafeZoneUpdateRequest(
    val firstBoundary: Double,
    val secondBoundary: Double,
    val thirdBoundary: Double,
    val firstTime: Int,
    val secondTime: Int,
    val thirdTime: Int
)
