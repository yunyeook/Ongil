package kr.co.ongil.data.repository

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.ongil.data.model.health.HealthData
import kr.co.ongil.data.model.health.HeartRateData
import kr.co.ongil.data.model.health.OxygenSaturationData
import kr.co.ongil.data.model.health.SleepData
import kr.co.ongil.data.model.health.StepsData
import kr.co.ongil.domain.repository.HealthConnectRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class HealthConnectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HealthConnectRepository {

    companion object {
        private const val TAG = "HealthConnectRepository"

        // 필요한 권한 목록
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class)
        )
    }

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    override suspend fun checkPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            Log.d(TAG, "checkPermissions() - 허용된 권한 목록: ${granted.size}개")
            granted.forEach { permission ->
                Log.d(TAG, "  - $permission")
            }
            Log.d(TAG, "checkPermissions() - 필요한 권한: ${PERMISSIONS.size}개")
            PERMISSIONS.forEach { permission ->
                val isGranted = permission in granted
                Log.d(TAG, "  - $permission: ${if (isGranted) "허용됨" else "거부됨"}")
            }
            val allGranted = PERMISSIONS.all { it in granted }
            Log.d(TAG, "checkPermissions() - 모든 권한 허용됨: $allGranted")
            allGranted
        } catch (e: Exception) {
            Log.e(TAG, "checkPermissions() - 권한 확인 실패", e)
            false
        }
    }

    override suspend fun getPermissionsToRequest(): Set<String> {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            PERMISSIONS.filterNot { it in granted }.toSet()
        } catch (e: Exception) {
            Log.e(TAG, "getPermissionsToRequest() - 권한 확인 실패", e)
            PERMISSIONS
        }
    }

    override fun getHealthData(): Flow<Result<HealthData>> = flow {
        try {
            Log.d(TAG, "getHealthData() - 건강 데이터 조회 시작")

            // 최근 30일간의 데이터 조회 (범위를 넓혀서 테스트)
            val endTime = Instant.now()
            val startTime = endTime.minus(30, ChronoUnit.DAYS)
            val timeRange = TimeRangeFilter.between(startTime, endTime)

            Log.d(TAG, "getHealthData() - 조회 기간: $startTime ~ $endTime")

            // 각 건강 데이터 조회
            val heartRateData = getHeartRateData(timeRange)
            val oxygenData = getOxygenSaturationData(timeRange)
            val sleepData = getSleepData(timeRange)
            val stepsData = getStepsData(timeRange)

            val healthData = HealthData(
                heartRate = heartRateData,
                oxygenSaturation = oxygenData,
                sleep = sleepData,
                steps = stepsData
            )

            Log.d(TAG, "getHealthData() - 건강 데이터 조회 성공: $healthData")
            emit(Result.success(healthData))
        } catch (e: Exception) {
            Log.e(TAG, "getHealthData() - 건강 데이터 조회 실패", e)
            emit(Result.failure(e))
        }
    }

    private suspend fun getHeartRateData(timeRange: TimeRangeFilter): HeartRateData? {
        return try {
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = timeRange
            )
            val response = healthConnectClient.readRecords(request)

            Log.d(TAG, "getHeartRateData() - 조회된 레코드 수: ${response.records.size}")
            if (response.records.isEmpty()) {
                Log.d(TAG, "getHeartRateData() - 레코드가 없습니다")
                return null
            }

            val bpmList = response.records.flatMap { record ->
                record.samples.map { it.beatsPerMinute }
            }

            Log.d(TAG, "getHeartRateData() - BPM 샘플 수: ${bpmList.size}")
            if (bpmList.isEmpty()) return null

            val result = HeartRateData(
                average = bpmList.average().toLong(),
                max = bpmList.maxOrNull() ?: 0,
                min = bpmList.minOrNull() ?: 0
            )
            Log.d(TAG, "getHeartRateData() - 결과: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "getHeartRateData() - 심박수 조회 실패", e)
            null
        }
    }

    private suspend fun getOxygenSaturationData(timeRange: TimeRangeFilter): OxygenSaturationData? {
        return try {
            val request = ReadRecordsRequest(
                recordType = OxygenSaturationRecord::class,
                timeRangeFilter = timeRange
            )
            val response = healthConnectClient.readRecords(request)

            if (response.records.isEmpty()) return null

            val percentageList = response.records.map { it.percentage.value }

            if (percentageList.isEmpty()) return null

            OxygenSaturationData(
                average = percentageList.average(),
                max = percentageList.maxOrNull() ?: 0.0,
                min = percentageList.minOrNull() ?: 0.0
            )
        } catch (e: Exception) {
            Log.e(TAG, "getOxygenSaturationData() - 혈중산소포화도 조회 실패", e)
            null
        }
    }

    private suspend fun getSleepData(timeRange: TimeRangeFilter): SleepData? {
        return try {
            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = timeRange
            )
            val response = healthConnectClient.readRecords(request)

            if (response.records.isEmpty()) return null

            val sleepHoursList = response.records.map { record ->
                val duration = ChronoUnit.MINUTES.between(record.startTime, record.endTime)
                duration / 60.0 // 시간 단위로 변환
            }

            if (sleepHoursList.isEmpty()) return null

            SleepData(
                average = sleepHoursList.average(),
                max = sleepHoursList.maxOrNull() ?: 0.0,
                min = sleepHoursList.minOrNull() ?: 0.0
            )
        } catch (e: Exception) {
            Log.e(TAG, "getSleepData() - 수면 데이터 조회 실패", e)
            null
        }
    }

    private suspend fun getStepsData(timeRange: TimeRangeFilter): StepsData? {
        return try {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeRange
            )
            val response = healthConnectClient.readRecords(request)

            Log.d(TAG, "getStepsData() - 조회된 레코드 수: ${response.records.size}")
            if (response.records.isEmpty()) {
                Log.d(TAG, "getStepsData() - 레코드가 없습니다")
                return null
            }

            // 일별 걸음수 계산
            val dailySteps = response.records
                .groupBy { record ->
                    record.startTime.truncatedTo(ChronoUnit.DAYS)
                }
                .mapValues { (_, records) ->
                    records.sumOf { it.count }
                }
                .values.toList()

            Log.d(TAG, "getStepsData() - 일별 걸음수: $dailySteps")
            if (dailySteps.isEmpty()) return null

            val result = StepsData(
                average = dailySteps.average().toLong(),
                max = dailySteps.maxOrNull() ?: 0,
                min = dailySteps.minOrNull() ?: 0
            )
            Log.d(TAG, "getStepsData() - 결과: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "getStepsData() - 걸음수 조회 실패", e)
            null
        }
    }
}
