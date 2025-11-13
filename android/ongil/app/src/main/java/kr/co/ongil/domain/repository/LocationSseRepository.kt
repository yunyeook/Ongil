package kr.co.ongil.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.location.GpsUpdateEvent

/**
 * 환자 위치 SSE Repository
 */
interface LocationSseRepository {
    /**
     * 환자들의 실시간 위치 스트림 구독
     */
    fun subscribePatientLocations(): Flow<GpsUpdateEvent>
}
