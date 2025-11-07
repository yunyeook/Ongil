package kr.co.ongil.data.model.notification

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 전체 읽음 처리 API 응답
 * PATCH /api/v1/notifications/read
 * 개별 읽음 처리 API 응답
 * PATCH /api/v1/notifications/{notificationId}/read
 * 개별 삭제 API 응답
 * DELETE /api/v1/notifications/{notificationId}
 *
 * 서버가 data를 빈 문자열(""), 빈 객체({}), 또는 null로 보낼 수 있으므로
 * JsonElement?를 사용하여 유연하게 처리합니다.
 */
@Serializable
data class MarkAllAsReadResponse(
    val message: String,
    val data: JsonElement? = null
)
