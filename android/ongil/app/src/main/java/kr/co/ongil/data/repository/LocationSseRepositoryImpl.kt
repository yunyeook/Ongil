package kr.co.ongil.data.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.datasource.remote.LocationSseDataSource
import kr.co.ongil.data.model.location.SseEvent
import kr.co.ongil.domain.repository.LocationSseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationSseRepositoryImpl @Inject constructor(
    private val locationSseDataSource: LocationSseDataSource
) : LocationSseRepository {

    override fun subscribeSseEvents(): Flow<SseEvent> {
        return locationSseDataSource.connectSseStream()
    }
}
