package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.datasource.remote.LocationSseDataSource
import kr.co.ongil.data.datasource.remote.api.LocationApi
import kr.co.ongil.data.model.location.Coordinate
import kr.co.ongil.data.model.location.SseEvent
import kr.co.ongil.domain.repository.LocationSseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationSseRepositoryImpl @Inject constructor(
    private val locationSseDataSource: LocationSseDataSource,
    private val locationApi: LocationApi
) : LocationSseRepository {

    override fun subscribeSseEvents(): Flow<SseEvent> {
        return locationSseDataSource.connectSseStream()
    }

    // ✅ 추가
    override suspend fun getPatientLocation(patientId: Long): Result<Coordinate> {
        return try {
            val response = locationApi.getPatientLocation(patientId)

            // ✅ data가 있으면 성공
            response.data?.let { coordinateDto ->
                Result.success(
                    Coordinate(
                        latitude = coordinateDto.latitude,
                        longitude = coordinateDto.longitude
                    )
                )
            } ?: Result.failure(Exception(response.message ?: "위치 데이터 없음"))

        } catch (e: Exception) {
            Log.e("LocationSseRepository", "환자 위치 조회 실패", e)
            Result.failure(e)
        }
    }
}
