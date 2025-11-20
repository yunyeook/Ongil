package kr.co.ongil.wear.data.repository

import android.util.Log
import kr.co.ongil.wear.data.datasource.sync.WearDataClient
import kr.co.ongil.wear.domain.model.NavigationLocation
import kr.co.ongil.wear.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Location Repository 구현 (블루투스 모델)
 *
 * Watch → Phone → Server 경로로 위치 전송
 * - Watch: WearDataClient로 Phone에 메시지 전송
 * - Phone: WearMessageListenerService에서 수신 → 서버로 전송
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val wearDataClient: WearDataClient
) : LocationRepository {

    companion object {
        private const val TAG = "LocationRepositoryImpl"
    }

    /**
     * 환자 위치 업데이트 (Phone으로 전송)
     *
     * @param patientId 환자 ID (현재는 사용 안함, Phone에서 처리)
     * @param latitude 위도
     * @param longitude 경도
     */
    override suspend fun updatePatientLocation(
        patientId: Long,
        latitude: Double,
        longitude: Double
    ): Result<String> {
        return try {
            // Phone으로 위치 전송 (Phone이 서버로 relay)
            val success = wearDataClient.sendLocation(latitude, longitude)

            if (success) {
                Log.d(TAG, "Location sent to Phone: $latitude, $longitude")
                Result.success("Location sent to Phone")
            } else {
                Log.e(TAG, "Failed to send location to Phone")
                Result.failure(Exception("Failed to send location to Phone"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location", e)
            Result.failure(e)
        }
    }

    /**
     * 네비게이션 시작 (Phone으로 전송)
     *
     * TODO: Phone으로 네비게이션 시작 메시지 전송
     * 현재는 로컬에서만 관리 (Phone relay 필요 시 구현)
     */
    override suspend fun startNavigation(
        patientId: Long,
        startLocation: NavigationLocation,
        endLocation: NavigationLocation,
        initiatedBy: String
    ): Result<Long> {
        return try {
            // TODO: Phone으로 네비게이션 시작 메시지 전송
            Log.d(TAG, "Navigation start (local only): ${startLocation.name} → ${endLocation.name}")
            Log.w(TAG, "TODO: Phone relay for navigation start not implemented yet")

            // 임시로 성공 반환 (navigationId는 임의값)
            Result.success(0L)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting navigation", e)
            Result.failure(e)
        }
    }

    /**
     * 네비게이션 종료 (Phone으로 전송)
     *
     * TODO: Phone으로 네비게이션 종료 메시지 전송
     * 현재는 로컬에서만 관리 (Phone relay 필요 시 구현)
     */
    override suspend fun endNavigation(
        navigationId: Long,
        endedBy: String
    ): Result<String> {
        return try {
            // TODO: Phone으로 네비게이션 종료 메시지 전송
            Log.d(TAG, "Navigation end (local only): navigationId=$navigationId")
            Log.w(TAG, "TODO: Phone relay for navigation end not implemented yet")

            Result.success("Navigation ended (local only)")
        } catch (e: Exception) {
            Log.e(TAG, "Error ending navigation", e)
            Result.failure(e)
        }
    }
}
