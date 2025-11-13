package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * 통화 기록 조회 API 응답
 * GET /api/v1/calls/log
 */
@Serializable
data class CallLogResponse(
    val message: String,
    val data: List<CallLogDto> = emptyList()  // 서버는 data를 직접 배열로 반환
)