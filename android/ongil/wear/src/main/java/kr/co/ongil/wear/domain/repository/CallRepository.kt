package kr.co.ongil.wear.domain.repository

import kr.co.ongil.wear.domain.model.CallState
import kr.co.ongil.wear.domain.model.TurnCredentials

/**
 * 통화 Repository Interface
 */
interface CallRepository {

    /**
     * VoIP 통화 생성
     */
    suspend fun createCall(
        targetUserId: String,
        targetName: String,
        targetPhone: String
    ): Result<Long> // callId 반환

    /**
     * 통화 상태 변경
     */
    suspend fun updateCallStatus(
        callId: Long,
        status: String
    ): Result<Unit>

    /**
     * 통화 정보 조회
     */
    suspend fun getCall(
        callId: Long
    ): Result<CallState>

    /**
     * TURN 서버 인증 정보 조회
     */
    suspend fun getTurnCredentials(): Result<TurnCredentials>
}
