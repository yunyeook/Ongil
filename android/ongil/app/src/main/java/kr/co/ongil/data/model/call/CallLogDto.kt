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
    val callType: String,        // VOIP / BASIC
    val status: String,           // COMPLETED / CANCELED / FAILED / REJECTED / MISSED
    val startedAt: String,        // ISO8601 format
    val endedAt: String,          // ISO8601 format
    val duration: Int,            // 통화 시간(초)
    val location: CallLocationDto? = null
)