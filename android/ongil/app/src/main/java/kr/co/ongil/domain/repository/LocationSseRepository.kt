package kr.co.ongil.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.location.Coordinate
import kr.co.ongil.data.model.location.SseEvent

/**
 * 환자 위치 SSE Repository
 */
interface LocationSseRepository {
    /**
     * SSE 이벤트 스트림 구독 (GPS 업데이트 + 길찾기 업데이트)
     */
    fun subscribeSseEvents(): Flow<SseEvent>
    suspend fun getPatientLocation(patientId: Long): Result<Coordinate>
}
