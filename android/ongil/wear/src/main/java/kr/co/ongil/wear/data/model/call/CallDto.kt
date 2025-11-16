package kr.co.ongil.wear.data.model.call

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 통화 생성 요청 DTO
 * POST /api/v1/calls
 */
@Serializable
data class CallCreateRequest(
    @SerialName("targetUserId")
    val targetUserId: String,

    @SerialName("targetName")
    val targetName: String,

    @SerialName("targetPhone")
    val targetPhone: String
)

/**
 * API 공통 응답 래퍼
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: T
)

/**
 * VoIP 통화 DTO
 */
@Serializable
data class VoipCallDto(
    @SerialName("id")
    val id: Long,

    @SerialName("callId")
    val callId: String,

    @SerialName("callerUserId")
    val callerUserId: String,

    @SerialName("callerName")
    val callerName: String,

    @SerialName("calleeUserId")
    val calleeUserId: String,

    @SerialName("calleeName")
    val calleeName: String,

    @SerialName("status")
    val status: String,

    @SerialName("startedAt")
    val startedAt: String? = null,

    @SerialName("endedAt")
    val endedAt: String? = null
)

/**
 * 통화 상태 변경 요청 DTO
 * PUT /api/v1/calls/{callId}/status
 */
@Serializable
data class CallStatusUpdateRequest(
    @SerialName("status")
    val status: String
)

/**
 * TURN 서버 인증 정보 DTO
 * GET /api/v1/calls/rtc/turn-credentials
 */
@Serializable
data class TurnCredentialsDto(
    @SerialName("urls")
    val urls: List<String>,

    @SerialName("username")
    val username: String,

    @SerialName("credential")
    val credential: String
)
