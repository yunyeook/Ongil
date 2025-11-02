package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * 통화 상세 정보 조회 응답
 */
@Serializable
data class CallDetailResponse(
    val message: String,
    val data: CallDetailDto
)

/**
 * 통화 상세 정보 DTO
 */
@Serializable
data class CallDetailDto(
    val callLogId: Long,
    val senderId: Long,
    val receiverId: Long,
    val callType: String,           // VOIP / BASIC
    val status: String,             // COMPLETED / CANCELED / FAILED / REJECTED / MISSED
    val startedAt: String,          // ISO 8601 형식
    val endedAt: String,            // ISO 8601 형식
    val duration: Int,              // 초 단위
    val location: CallLocationDto?  // 위치 정보 (nullable)
)
