package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * 통화 기록 조회 API 응답
 * GET /api/v1/calls/log
 */
@Serializable
data class CallLogResponse(
    val message: String,
    val data: CallLogData
)

@Serializable
data class CallLogData(
    val totalCount: Int,
    val callLogs: List<CallLogDto>
)

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

/**
 * 통화 위치 정보
 */
@Serializable
data class CallLocationDto(
    val latitude: Double,
    val longitude: Double
)
