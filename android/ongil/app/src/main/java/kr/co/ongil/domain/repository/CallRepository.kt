package kr.co.ongil.domain.repository

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
}
