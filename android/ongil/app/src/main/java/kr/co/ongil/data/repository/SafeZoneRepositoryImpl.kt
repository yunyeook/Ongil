package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.ongil.data.datasource.remote.api.SafeZoneApi
import kr.co.ongil.data.model.safezone.SafeZoneData
import kr.co.ongil.data.model.safezone.SafeZoneUpdateRequest
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.SafeZoneRepository
import javax.inject.Inject

/**
 * 안전범위 설정 Repository 구현체
 *
 * 참고: Authorization 헤더는 AuthInterceptor가 TokenManager에서 자동으로 가져와 추가합니다.
 */
class SafeZoneRepositoryImpl @Inject constructor(
    private val safeZoneApi: SafeZoneApi
) : SafeZoneRepository {

    companion object {
        private const val TAG = "SafeZoneRepositoryImpl"
    }

    override fun getSafeZone(
        patientId: Long
    ): Flow<Result<SafeZoneData>> = flow {
        try {
            Log.d(TAG, "📡 Repository: getSafeZone() 호출")
            Log.d(TAG, "  - patientId: $patientId")

            Log.d(TAG, "🔄 API 호출 중...")
            val response = safeZoneApi.getSafeZone(patientId)
            Log.d(TAG, "✅ Repository: API 응답 성공")
            Log.d(TAG, "  - message: ${response.message}")
            Log.d(TAG, "  - patientId: ${response.data.patientId}")
            Log.d(TAG, "  - 1단계: ${response.data.boundaries.first.radius}m, ${response.data.boundaries.first.time}분")
            Log.d(TAG, "  - 2단계: ${response.data.boundaries.second.radius}m, ${response.data.boundaries.second.time}분")
            Log.d(TAG, "  - 3단계: ${response.data.boundaries.third.radius}m, ${response.data.boundaries.third.time}분")
            emit(Result.success(response.data))

        } catch (e: Exception) {
            Log.e(TAG, "❌ Repository: API 호출 실패", e)
            Log.e(TAG, "  - Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "  - Error message: ${e.message}")
            emit(Result.failure(ErrorHandler.handleException(e)))
        }
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
        try {
            Log.d(TAG, "📡 Repository: updateSafeZone() 호출")
            Log.d(TAG, "  - patientId: $patientId")
            Log.d(TAG, "  - firstBoundary: $firstBoundary, firstTime: $firstTime")
            Log.d(TAG, "  - secondBoundary: $secondBoundary, secondTime: $secondTime")
            Log.d(TAG, "  - thirdBoundary: $thirdBoundary, thirdTime: $thirdTime")

            val request = SafeZoneUpdateRequest(
                firstBoundary = firstBoundary,
                secondBoundary = secondBoundary,
                thirdBoundary = thirdBoundary,
                firstTime = firstTime,
                secondTime = secondTime,
                thirdTime = thirdTime
            )

            Log.d(TAG, "🔄 API 호출 중...")
            val response = safeZoneApi.updateSafeZone(patientId, request)
            Log.d(TAG, "✅ Repository: API 응답 성공")
            Log.d(TAG, "  - message: ${response.message}")
            Log.d(TAG, "  - patientId: ${response.data.patientId}")
            emit(Result.success(response.data))

        } catch (e: Exception) {
            Log.e(TAG, "❌ Repository: API 호출 실패", e)
            Log.e(TAG, "  - Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "  - Error message: ${e.message}")
            emit(Result.failure(ErrorHandler.handleException(e)))
        }
    }
}
