package kr.co.ongil.wear.domain.model

/**
 * SOS 알림 상태 Domain Model
 */
data class SosState(
    val sosId: Long? = null,
    val isActive: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val message: String? = null,
    val timestamp: Long? = null
)

/**
 * SOS 알림 결과 Domain Model
 */
data class SosAlertResult(
    val success: Boolean,
    val sosId: Long? = null,
    val message: String? = null
)
