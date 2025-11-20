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
import kr.co.ongil.data.model.health.LocalHealthData
import kr.co.ongil.data.model.health.HeartRateRecord as LocalHeartRateRecord
import kr.co.ongil.data.model.health.OxygenSaturationRecord as LocalOxygenSaturationRecord
import kr.co.ongil.data.model.health.SleepRecord
import kr.co.ongil.data.model.health.StepsRecord as LocalStepsRecord
import kr.co.ongil.domain.repository.HealthConnectRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class HealthConnectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HealthConnectRepository {

    companion object {
        private const val TAG = "HealthConnectRepository"

        // ISO-8601 날짜 시간 포맷터
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            .withZone(ZoneId.systemDefault())

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

    override fun getHealthData(): Flow<Result<LocalHealthData>> = flow {
        try {
            Log.d(TAG, "getHealthData() - 건강 데이터 조회 시작")

            // 최근 30일간의 데이터 조회
            val endTime = Instant.now()
            val startTime = endTime.minus(30, ChronoUnit.DAYS)
            val timeRange = TimeRangeFilter.between(startTime, endTime)

            Log.d(TAG, "getHealthData() - 조회 기간: $startTime ~ $endTime")

            // 각 건강 데이터 조회 (개별 레코드 리스트)
            val heartRateRecords = getHeartRateRecords(timeRange)
            val oxygenRecords = getOxygenSaturationRecords(timeRange)
            val sleepRecords = getSleepRecords(timeRange)
            val stepsRecords = getStepsRecords(timeRange)

            val healthData = LocalHealthData(
                heartRateRecords = heartRateRecords,
                oxygenSaturationRecords = oxygenRecords,
                sleepRecords = sleepRecords,
                stepsRecords = stepsRecords
            )

            Log.d(TAG, "getHealthData() - 건강 데이터 조회 성공: 심박수=${heartRateRecords.size}개, 산소=${oxygenRecords.size}개, 수면=${sleepRecords.size}개, 걸음수=${stepsRecords.size}개")
            emit(Result.success(healthData))
        } catch (e: Exception) {
            Log.e(TAG, "getHealthData() - 건강 데이터 조회 실패", e)
            emit(Result.failure(e))
        }
    }

    private suspend fun getHeartRateRecords(timeRange: TimeRangeFilter): List<kr.co.ongil.data.model.health.HeartRateRecord> {
        return try {
            val request = ReadRecordsRequest(
                recordType = androidx.health.connect.client.records.HeartRateRecord::class,
                timeRangeFilter = timeRange
            )
            val response = healthConnectClient.readRecords(request)

            Log.d(TAG, "getHeartRateRecords() - 조회된 레코드 수: ${response.records.size}")
            if (response.records.isEmpty()) {
                Log.d(TAG, "getHeartRateRecords() - 레코드가 없습니다")
                return emptyList()
            }

            // 각 레코드의 모든 샘플을 개별 레코드로 변환
            val records = mutableListOf<kr.co.ongil.data.model.health.HeartRateRecord>()
            response.records.forEach { record ->
                record.samples.forEach { sample ->
                    records.add(
                        kr.co.ongil.data.model.health.HeartRateRecord(
                            beatsPerMinute = sample.beatsPerMinute,
                            measuredAt = dateTimeFormatter.format(sample.time)
                        )
                    )
                }
            }

            Log.d(TAG, "getHeartRateRecords() - 변환된 레코드 수: ${records.size}")
            records
        } catch (e: Exception) {
            Log.e(TAG, "getHeartRateRecords() - 심박수 조회 실패", e)
            emptyList()
        }
    }

    private suspend fun getOxygenSaturationRecords(timeRange: TimeRangeFilter): List<kr.co.ongil.data.model.health.OxygenSaturationRecord> {
        return try {
            val request = ReadRecordsRequest(
                recordType = androidx.health.connect.client.records.OxygenSaturationRecord::class,
                timeRangeFilter = timeRange
            )
            val response = healthConnectClient.readRecords(request)

            Log.d(TAG, "getOxygenSaturationRecords() - 조회된 레코드 수: ${response.records.size}")
            if (response.records.isEmpty()) {
                Log.d(TAG, "getOxygenSaturationRecords() - 레코드가 없습니다")
                return emptyList()
            }

            // 각 레코드를 개별 OxygenSaturationRecord로 변환
            val records = response.records.map { record ->
                kr.co.ongil.data.model.health.OxygenSaturationRecord(
                    percentage = record.percentage.value,
                    measuredAt = dateTimeFormatter.format(record.time)
                )
            }

            Log.d(TAG, "getOxygenSaturationRecords() - 변환된 레코드 수: ${records.size}")
            records
        } catch (e: Exception) {
            Log.e(TAG, "getOxygenSaturationRecords() - 혈중산소포화도 조회 실패", e)
            emptyList()
        }
    }

    private suspend fun getSleepRecords(timeRange: TimeRangeFilter): List<kr.co.ongil.data.model.health.SleepRecord> {
        return try {
            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = timeRange
            )
            val response = healthConnectClient.readRecords(request)

            Log.d(TAG, "getSleepRecords() - 조회된 레코드 수: ${response.records.size}")
            if (response.records.isEmpty()) {
                Log.d(TAG, "getSleepRecords() - 레코드가 없습니다")
                return emptyList()
            }

            // 각 레코드를 개별 SleepRecord로 변환
            val records = response.records.map { record ->
                val durationHours = ChronoUnit.MINUTES.between(record.startTime, record.endTime) / 60.0

                kr.co.ongil.data.model.health.SleepRecord(
                    durationHours = durationHours,
                    measuredAt = dateTimeFormatter.format(record.startTime)
                )
            }

            Log.d(TAG, "getSleepRecords() - 변환된 레코드 수: ${records.size}")
            records
        } catch (e: Exception) {
            Log.e(TAG, "getSleepRecords() - 수면 데이터 조회 실패", e)
            emptyList()
        }
    }

    private suspend fun getStepsRecords(timeRange: TimeRangeFilter): List<kr.co.ongil.data.model.health.StepsRecord> {
        return try {
            val request = ReadRecordsRequest(
                recordType = androidx.health.connect.client.records.StepsRecord::class,
                timeRangeFilter = timeRange
            )
            val response = healthConnectClient.readRecords(request)

            Log.d(TAG, "getStepsRecords() - 조회된 레코드 수: ${response.records.size}")
            if (response.records.isEmpty()) {
                Log.d(TAG, "getStepsRecords() - 레코드가 없습니다")
                return emptyList()
            }

            // 각 레코드를 개별 StepsRecord로 변환
            val records = response.records.map { record ->
                kr.co.ongil.data.model.health.StepsRecord(
                    count = record.count,
                    measuredAt = dateTimeFormatter.format(record.startTime)
                )
            }

            Log.d(TAG, "getStepsRecords() - 변환된 레코드 수: ${records.size}")
            records
        } catch (e: Exception) {
            Log.e(TAG, "getStepsRecords() - 걸음수 조회 실패", e)
            emptyList()
        }
    }
}
