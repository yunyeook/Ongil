package kr.co.ongil.wear.data.repository

import android.util.Log
import kr.co.ongil.wear.data.datasource.remote.api.WearSosApi
import kr.co.ongil.wear.data.model.sos.SendSosAlertRequest
import kr.co.ongil.wear.domain.model.SosAlertResult
import kr.co.ongil.wear.domain.repository.SosRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepositoryImpl @Inject constructor(
    private val sosApi: WearSosApi
) : SosRepository {

    companion object {
        private const val TAG = "SosRepositoryImpl"
    }

    override suspend fun sendSosAlert(
        patientId: Int,
        latitude: Double,
        longitude: Double,
        message: String?
    ): Result<SosAlertResult> {
        return try {
            val response = sosApi.sendSosAlert(
                patientId = patientId,
                request = SendSosAlertRequest(
                    latitude = latitude,
                    longitude = longitude,
                    message = message
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val sosData = response.body()!!.data
                Log.d(TAG, "SOS alert sent: ${sosData.sosId}")
                Result.success(
                    SosAlertResult(
                        success = true,
                        sosId = sosData.sosId,
                        message = response.body()!!.message
                    )
                )
            } else {
                Log.e(TAG, "Failed to send SOS alert: ${response.code()}")
                Result.failure(Exception("Failed to send SOS alert: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SOS alert", e)
            Result.failure(e)
        }
    }

    override suspend fun stopSosAlert(patientId: Int): Result<String> {
        return try {
            val response = sosApi.stopSosAlert(patientId)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "SOS alert stopped")
                Result.success(response.body()!!.message)
            } else {
                Log.e(TAG, "Failed to stop SOS alert: ${response.code()}")
                Result.failure(Exception("Failed to stop SOS alert: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping SOS alert", e)
            Result.failure(e)
        }
    }
}
