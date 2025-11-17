package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kr.co.ongil.data.datasource.remote.api.SafeZoneApi
import kr.co.ongil.data.model.safezone.SafeZoneData
import kr.co.ongil.data.model.safezone.SafeZoneUpdateRequest
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.SafeZoneRepository
import javax.inject.Inject

class SafeZoneRepositoryImpl @Inject constructor(
    private val safeZoneApi: SafeZoneApi
) : SafeZoneRepository {

    companion object {
        private const val TAG = "SafeZoneRepositoryImpl"
    }

    override fun getSafeZone(
        patientId: Long
    ): Flow<Result<SafeZoneData>> = flow {
//        Log.d(TAG, "📡 Repository: getSafeZone() 호출 - patientId: $patientId")
        val response = safeZoneApi.getSafeZone(patientId)
//        Log.d(TAG, "✅ Repository: API 응답 성공")
        emit(Result.success(response.data))
    }.catch { e ->
        Log.e(TAG, "❌ Repository: API 호출 실패 - ${e.message}")
        emit(Result.failure(ErrorHandler.handleException(e as Exception)))
    }

    override fun updateSafeZone(
        patientId: Long,
        firstBoundary: Double,
        secondBoundary: Double,
        thirdBoundary: Double,
        firstTime: Int,
        secondTime: Int,
        thirdTime: Int
    ): Flow<Result<SafeZoneData>> = flow {
//        Log.d(TAG, "📡 Repository: updateSafeZone() 호출 - patientId: $patientId")

        val request = SafeZoneUpdateRequest(
            firstBoundary = firstBoundary,
            secondBoundary = secondBoundary,
            thirdBoundary = thirdBoundary,
            firstTime = firstTime,
            secondTime = secondTime,
            thirdTime = thirdTime
        )

        val response = safeZoneApi.updateSafeZone(patientId, request)
        Log.d(TAG, "✅ Repository: API 응답 성공")
        emit(Result.success(response.data))
    }.catch { e ->
        Log.e(TAG, "❌ Repository: API 호출 실패 - ${e.message}")
        emit(Result.failure(ErrorHandler.handleException(e as Exception)))
    }
}