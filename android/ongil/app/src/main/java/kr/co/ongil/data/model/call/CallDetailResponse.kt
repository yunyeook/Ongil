package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

/**
 * 통화 상세 정보 조회 API 응답
 * GET /api/v1/calls/log/{callLogId}
 */
@Serializable
data class CallDetailResponse(
    val message: String,
    val data: CallDetailDto
)