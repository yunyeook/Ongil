package kr.co.ongil.wear.data.repository

import android.util.Log
import kr.co.ongil.wear.data.datasource.remote.api.WearLocationApi
import kr.co.ongil.wear.data.model.location.NavigationEndRequest
import kr.co.ongil.wear.data.model.location.NavigationLocationDto
import kr.co.ongil.wear.data.model.location.NavigationStartRequest
import kr.co.ongil.wear.data.model.location.UpdateLocationRequest
import kr.co.ongil.wear.domain.model.NavigationLocation
import kr.co.ongil.wear.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationApi: WearLocationApi
) : LocationRepository {

    companion object {
        private const val TAG = "LocationRepositoryImpl"
    }

    override suspend fun updatePatientLocation(
        patientId: Long,
        latitude: Double,
        longitude: Double
    ): Result<String> {
        return try {
            val response = locationApi.updatePatientLocation(
                patientId = patientId,
                request = UpdateLocationRequest(
                    latitude = latitude,
                    longitude = longitude
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Location updated: $latitude, $longitude")
                Result.success(response.body()!!.message)
            } else {
                Log.e(TAG, "Failed to update location: ${response.code()}")
                Result.failure(Exception("Failed to update location: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating location", e)
            Result.failure(e)
        }
    }

    override suspend fun startNavigation(
        patientId: Long,
        startLocation: NavigationLocation,
        endLocation: NavigationLocation,
        initiatedBy: String
    ): Result<Long> {
        return try {
            val response = locationApi.startNavigation(
                request = NavigationStartRequest(
                    patientId = patientId,
                    startLocation = NavigationLocationDto(
                        latitude = startLocation.latitude,
                        longitude = startLocation.longitude,
                        name = startLocation.name
                    ),
                    endLocation = NavigationLocationDto(
                        latitude = endLocation.latitude,
                        longitude = endLocation.longitude,
                        name = endLocation.name
                    ),
                    initiatedBy = initiatedBy
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val navigationId = response.body()!!.data.navigationId
                Log.d(TAG, "Navigation started: $navigationId")
                Result.success(navigationId)
            } else {
                Log.e(TAG, "Failed to start navigation: ${response.code()}")
                Result.failure(Exception("Failed to start navigation: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting navigation", e)
            Result.failure(e)
        }
    }

    override suspend fun endNavigation(
        navigationId: Long,
        endedBy: String
    ): Result<String> {
        return try {
            val response = locationApi.endNavigation(
                request = NavigationEndRequest(
                    navigationId = navigationId,
                    endedBy = endedBy
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Navigation ended: $navigationId")
                Result.success(response.body()!!.message)
            } else {
                Log.e(TAG, "Failed to end navigation: ${response.code()}")
                Result.failure(Exception("Failed to end navigation: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ending navigation", e)
            Result.failure(e)
        }
    }
}
