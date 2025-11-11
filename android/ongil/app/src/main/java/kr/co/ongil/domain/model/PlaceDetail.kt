package kr.co.ongil.domain.model

/**
 * 장소 상세 정보 도메인 모델
 */
data class PlaceDetail(
    val id: String,
    val name: String,
    val roadAddress: String?,
    val jibunAddress: String?,
    val latitude: Double,
    val longitude: Double,
    val category: String?,
    val phoneNumber: String?,
    val description: String?,
    val zipCode: String?,
    val parking: Boolean?,
    val businessHours: String?,
    val closedDays: String?,
    val is24Hours: Boolean?,
    val isYearRound: Boolean?
)
