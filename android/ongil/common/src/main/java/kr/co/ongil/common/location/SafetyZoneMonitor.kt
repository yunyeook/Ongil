package kr.co.ongil.common.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class SafetyZoneMonitor(
    private val homeLatitude: Double,
    private val homeLongitude: Double,
    private val level1Distance: Int = DEFAULT_STAGE_1_RADIUS,
    private val level1Dwell: Int = DEFAULT_STAGE_1_THRESHOLD_MINUTES,
    private val level2Distance: Int = DEFAULT_STAGE_2_RADIUS,
    private val level2Dwell: Int = DEFAULT_STAGE_2_THRESHOLD_MINUTES,
    private val level3Distance: Int = DEFAULT_STAGE_3_RADIUS,
    private val level3Dwell: Int = DEFAULT_STAGE_3_THRESHOLD_MINUTES,
    private val onAbnormalDetected: (stage: Int, durationMinutes: Long) -> Unit,
    private val onSafeZoneExit: (stage: Int, distance: Double) -> Unit  // ⭐ 새 파라미터
) {
    companion object {
        // 기본 안전 범위 반경 (미터)
        const val DEFAULT_STAGE_1_RADIUS = 100
        const val DEFAULT_STAGE_2_RADIUS = 350
        const val DEFAULT_STAGE_3_RADIUS = 700

        // 기본 이상 판정 기준 시간 (분)
        const val DEFAULT_STAGE_1_THRESHOLD_MINUTES = 60
        const val DEFAULT_STAGE_2_THRESHOLD_MINUTES = 30
        const val DEFAULT_STAGE_3_THRESHOLD_MINUTES = 15

        /**
         * 두 지점 간 거리 계산 (Haversine formula, 미터 단위)
         */
        fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadius = 6371000.0 // 지구 반경 (미터)
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)

            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2)

            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadius * c
        }
    }

    // 각 단계별 범위 밖으로 나간 시작 시간 (null이면 범위 안에 있음)
    private var stage1OutsideStartTime: Long? = null
    private var stage2OutsideStartTime: Long? = null
    private var stage3OutsideStartTime: Long? = null

    // 각 단계별 이상 판정 여부 (한 번만 알림)
    private var stage1Alerted = false
    private var stage2Alerted = false
    private var stage3Alerted = false

    // 위치 업데이트 처리
    fun updateLocation(latitude: Double, longitude: Double, currentTimeMillis: Long) {
        // 홈 위치와의 거리 계산
        val distance = calculateDistance(homeLatitude, homeLongitude, latitude, longitude)

        // 각 단계별 처리
        checkStage1(distance, currentTimeMillis)
        checkStage2(distance, currentTimeMillis)
        checkStage3(distance, currentTimeMillis)
    }

    // 1 단계
    private fun checkStage1(distance: Double, currentTimeMillis: Long) {
        if (distance > level1Distance) {
            // 범위 밖
            if (stage1OutsideStartTime == null) {
                // 처음 범위 밖으로 나감
                stage1OutsideStartTime = currentTimeMillis
                stage1Alerted = false
                onSafeZoneExit(1, distance)
            } else {
                // 이미 범위 밖 - 연속 체류 시간 계산
                val durationMillis = currentTimeMillis - stage1OutsideStartTime!!
                val durationMinutes = durationMillis / 1000 / 60

                if (durationMinutes >= level1Dwell && !stage1Alerted) {
                    // 이상 판정
                    stage1Alerted = true
                    onAbnormalDetected(1, durationMinutes)
                }
            }
        } else {
            // 범위 안으로 들어옴 - 타이머 리셋
            stage1OutsideStartTime = null
            stage1Alerted = false
        }
    }

   // 2 단계
    private fun checkStage2(distance: Double, currentTimeMillis: Long) {
        if (distance > level2Distance) {
            // 범위 밖
            if (stage2OutsideStartTime == null) {
                // 처음 범위 밖으로 나감
                stage2OutsideStartTime = currentTimeMillis
                stage2Alerted = false
                onSafeZoneExit(2, distance)
            } else {
                // 이미 범위 밖 - 연속 체류 시간 계산
                val durationMillis = currentTimeMillis - stage2OutsideStartTime!!
                val durationMinutes = durationMillis / 1000 / 60

                if (durationMinutes >= level2Dwell && !stage2Alerted) {
                    // 이상 판정
                    stage2Alerted = true
                    onAbnormalDetected(2, durationMinutes)
                }
            }
        } else {
            // 범위 안으로 들어옴 - 타이머 리셋
            stage2OutsideStartTime = null
            stage2Alerted = false
        }
    }

    // 3 단계
    private fun checkStage3(distance: Double, currentTimeMillis: Long) {
        if (distance > level3Distance) {
            // 범위 밖
            if (stage3OutsideStartTime == null) {
                // 처음 범위 밖으로 나감
                stage3OutsideStartTime = currentTimeMillis
                stage3Alerted = false
                onSafeZoneExit(3, distance)
            } else {
                // 이미 범위 밖 - 연속 체류 시간 계산
                val durationMillis = currentTimeMillis - stage3OutsideStartTime!!
                val durationMinutes = durationMillis / 1000 / 60

                if (durationMinutes >= level3Dwell && !stage3Alerted) {
                    // 이상 판정
                    stage3Alerted = true
                    onAbnormalDetected(3, durationMinutes)
                }
            }
        } else {
            // 범위 안으로 들어옴 - 타이머 리셋
            stage3OutsideStartTime = null
            stage3Alerted = false
        }
    }

    // 현재 상태 조회
    fun getCurrentStatus(): SafetyZoneStatus {
        val now = System.currentTimeMillis()
        return SafetyZoneStatus(
            stage1OutsideDurationMinutes = stage1OutsideStartTime?.let { (now - it) / 1000 / 60 },
            stage2OutsideDurationMinutes = stage2OutsideStartTime?.let { (now - it) / 1000 / 60 },
            stage3OutsideDurationMinutes = stage3OutsideStartTime?.let { (now - it) / 1000 / 60 }
        )
    }

    // 모니터링 리셋
    // TODO: 이거는 확인하고 필요없으면 삭제
    fun reset() {
        stage1OutsideStartTime = null
        stage2OutsideStartTime = null
        stage3OutsideStartTime = null
        stage1Alerted = false
        stage2Alerted = false
        stage3Alerted = false
    }
}

// 현재 안전범위 상태
data class SafetyZoneStatus(
    val stage1OutsideDurationMinutes: Long?,
    val stage2OutsideDurationMinutes: Long?,
    val stage3OutsideDurationMinutes: Long?
)
