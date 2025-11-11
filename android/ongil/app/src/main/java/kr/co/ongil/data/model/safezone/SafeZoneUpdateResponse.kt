package kr.co.ongil.data.model.safezone

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * 안전범위 수정 응답 DTO
 */

@Serializable
data class SafeZoneUpdateResponse(
    val message: String,
    val data: SafeZoneData
)
@Serializable
data class SafeZoneData(
    val patientId: Long,
    val boundaries: Boundaries,
    val updatedAt: String
)
@Serializable
data class Boundaries(
    val first: BoundaryLevel,
    val second: BoundaryLevel,
    val third: BoundaryLevel
)
@Serializable
data class BoundaryLevel(
    val radius: Double,
    val time: Int
)
