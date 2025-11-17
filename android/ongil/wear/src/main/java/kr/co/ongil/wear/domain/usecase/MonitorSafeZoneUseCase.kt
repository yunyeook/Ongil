package kr.co.ongil.wear.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.common.location.SafetyZoneMonitor
import kr.co.ongil.wear.domain.model.SafeZoneConfig
import kr.co.ongil.wear.domain.model.SafeZoneStatus
import javax.inject.Inject
import javax.inject.Singleton
import kr.co.ongil.common.location.SafetyZoneStatus as CommonSafetyZoneStatus

/**
 * 안전 범위 모니터링 UseCase
 *
 * 주요 기능:
 * 1. LocationStreamBus에서 위치 업데이트 구독
 * 2. SafetyZoneMonitor를 사용하여 안전 범위 체크
 * 3. 안전 범위 이탈 시 상태 업데이트
 */
@Singleton
class MonitorSafeZoneUseCase @Inject constructor(
    private val locationStreamBus: LocationStreamBus
) {

    companion object {
        private const val TAG = "MonitorSafeZoneUseCase"
    }

    // 현재 SafetyZoneMonitor 인스턴스 (설정 변경 시 재생성)
    private var safetyZoneMonitor: SafetyZoneMonitor? = null

    // 현재 안전 범위 설정
    private var currentConfig: SafeZoneConfig? = null

    /**
     * 안전 범위 설정 업데이트
     */
    fun updateSafeZone(
        homeLatitude: Double,
        homeLongitude: Double,
        stage1Radius: Int = 100,
        stage2Radius: Int = 350,
        stage3Radius: Int = 700,
        stage1ThresholdMinutes: Int = 60,
        stage2ThresholdMinutes: Int = 30,
        stage3ThresholdMinutes: Int = 15
    ) {
        currentConfig = SafeZoneConfig(
            homeLatitude = homeLatitude,
            homeLongitude = homeLongitude,
            stage1Radius = stage1Radius,
            stage2Radius = stage2Radius,
            stage3Radius = stage3Radius,
            stage1ThresholdMinutes = stage1ThresholdMinutes,
            stage2ThresholdMinutes = stage2ThresholdMinutes,
            stage3ThresholdMinutes = stage3ThresholdMinutes
        )

        // 새로운 SafetyZoneMonitor 인스턴스 생성
        safetyZoneMonitor = SafetyZoneMonitor(
            homeLatitude = homeLatitude,
            homeLongitude = homeLongitude,
            level1Distance = stage1Radius,
            level1Dwell = stage1ThresholdMinutes,
            level2Distance = stage2Radius,
            level2Dwell = stage2ThresholdMinutes,
            level3Distance = stage3Radius,
            level3Dwell = stage3ThresholdMinutes,
            onAbnormalDetected = { stage, durationMinutes ->
                // 이상 감지 시 콜백 (로그 출력)
                android.util.Log.w(TAG, "안전 범위 Stage $stage 이탈: ${durationMinutes}분")
                // TODO: 알림 표시 로직 추가 필요 (WearNotificationManager)
            }
        )
    }

    /**
     * 안전 범위 모니터링 시작
     *
     * @param homeLatitude 홈 위치 위도
     * @param homeLongitude 홈 위치 경도
     * @return SafeZoneStatus Flow
     */
    operator fun invoke(
        homeLatitude: Double,
        homeLongitude: Double
    ): Flow<SafeZoneStatus> = flow {
        // SafetyZoneMonitor 초기화 (기본 설정)
        if (safetyZoneMonitor == null) {
            updateSafeZone(homeLatitude, homeLongitude)
        }

        val monitor = safetyZoneMonitor ?: return@flow

        // LocationStreamBus에서 위치 업데이트 구독
        locationStreamBus.updates.collect { locationPoint ->
            // SafetyZoneMonitor 업데이트
            monitor.updateLocation(
                latitude = locationPoint.latitude,
                longitude = locationPoint.longitude,
                currentTimeMillis = locationPoint.timeMillis
            )

            // 현재 상태 가져오기
            val commonStatus = monitor.getCurrentStatus()

            // Common 모듈의 SafetyZoneStatus → Wear 모듈의 SafeZoneStatus 변환
            val wearStatus = convertToWearSafeZoneStatus(commonStatus)

            // 상태 emit
            emit(wearStatus)
        }
    }

    /**
     * Common 모듈의 SafetyZoneStatus를 Wear 모듈의 SafeZoneStatus로 변환
     */
    private fun convertToWearSafeZoneStatus(
        commonStatus: CommonSafetyZoneStatus
    ): SafeZoneStatus {
        return SafeZoneStatus(
            isInsideStage1 = commonStatus.stage1OutsideDurationMinutes == null,
            isInsideStage2 = commonStatus.stage2OutsideDurationMinutes == null,
            isInsideStage3 = commonStatus.stage3OutsideDurationMinutes == null,
            stage1OutsideDuration = commonStatus.stage1OutsideDurationMinutes,
            stage2OutsideDuration = commonStatus.stage2OutsideDurationMinutes,
            stage3OutsideDuration = commonStatus.stage3OutsideDurationMinutes,
            hasAlerted = commonStatus.stage1OutsideDurationMinutes != null ||
                    commonStatus.stage2OutsideDurationMinutes != null ||
                    commonStatus.stage3OutsideDurationMinutes != null
        )
    }
}
