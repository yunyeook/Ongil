package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * 통화 로그 DTO
 */

@Serializable
data class CallLogDto(
    val callLogId: Long,
    val senderId: Long,
    val receiverId: Long,
    val callType: String,                   // NORMAL / EMERGENCY
    val source: String? = null,             // APP (VoIP) / SYSTEM_DIALER (일반전화)
    val status: String? = null,             // COMPLETED / CANCELED / FAILED / REJECTED / MISSED (기존 스펙)
    val patientState: String? = null,       // NORMAL / NAVIGATING / ABNORMAL
    val patientLocation: String? = null,    // 위치 문자열
    val location: CallLocationDto? = null,  // 기존 스펙 (위치 객체)
    val startedAt: String,                  // ISO8601 format
    val endedAt: String,                    // ISO8601 format
    val duration: Int                       // 통화 시간(초)
)