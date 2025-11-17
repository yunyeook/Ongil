package kr.co.ongil.domain.repository

import kr.co.ongil.data.model.call.*
import kr.co.ongil.data.model.call.CallDetailDto
import kr.co.ongil.data.model.call.CallLogDto

/**
 * 통화 Repository 인터페이스
 */
interface CallRepository {

    /**
     * 나의 통화 기록 목록 조회
     */
    suspend fun getCallLogs(): Result<List<CallLogDto>>

    /**
     * 특정 통화 기록 상세 조회
     */
    suspend fun getCallDetail(callLogId: Long): Result<CallDetailDto>

    /**
     * 통화 생성 (보호자가 우리앱 통화 걸기)
     * - POST /api/v1/calls
     */
    suspend fun createVoipCall(
        receiverId: Long,
        callType: String
    ): Result<VoipCallDto>

    /**
     * 통화 상태 변경
     * - 수락(CONNECTED), 종료(ENDED), 거절(REJECTED) 등
     * - PUT /api/v1/calls/{callId}/status
     */
    suspend fun updateVoipCallStatus(
        callId: Long,
        status: String
    ): Result<VoipCallDto>

    /**
     * callId로 현재 통화 조회
     * - GET /api/v1/calls/{callId}
     * - INCOMING 화면에서 사용
     */
    suspend fun getVoipCall(
        callId: Long
    ): Result<VoipCallDto>

    /**
     * 환자 위치 1회 전송
     * - POST /api/v1/calls/{callId}/start-location
     * - 환자가 CONNECTED 직후에만 호출
     */
    suspend fun sendVoipCallStartLocation(
        callId: Long,
        request: CallStartLocationRequest
    ): Result<Unit>

    suspend fun getTurnCredentials(): Result<TurnCredentialsDto>
    suspend fun notifyCallerReady(callId: Long): Result<Unit>
}
