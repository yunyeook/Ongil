package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * 통화 로그 DTO (서버 응답에 맞게 수정)
 */
@Serializable
data class CallLogDto(
    val id: Long,
    val caller: CallUserInfo,
    val receiver: CallUserInfo,
    val callType: String,                    // NORMAL / EMERGENCY
    val source: String,                      // APP (VoIP) / SYSTEM_DIALER (일반전화)
    val patientState: String? = null,        // NORMAL / NAVIGATING / ABNORMAL
    val patientLocation: LocationInfo? = null,
    val startedAt: String,                   // ISO8601 format
    val endedAt: String? = null,             // ISO8601 format
    val duration: Int? = null,               // 통화 시간(초) - nullable 처리
    val memo: String? = null,
    val createdAt: String                    // ISO8601 format
)

@Serializable
data class CallUserInfo(
    val id: Long,
    val name: String,
    val phoneNumber: String
)

@Serializable
data class LocationInfo(
    val latitude: Double,
    val longitude: Double
)