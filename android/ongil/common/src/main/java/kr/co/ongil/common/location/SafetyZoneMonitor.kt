package kr.co.ongil.common.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 안전 범위 모니터링 클래스
 * - 현재 위치와 홈 위치 간 거리 계산
 * - 3단계 안전 범위 판정
 * - 연속 체류 시간 추적
 * - 이상 판정 콜백
 *
 * Android와 Wear 모두에서 사용 가능한 순수 Kotlin 구현
 */
class SafetyZoneMonitor(
    private val homeLatitude: Double,
    private val homeLongitude: Double,
    private val onAbnormalDetected: (stage: Int, durationMinutes: Long) -> Unit
) {
    companion object {
        // 안전 범위 반경 (미터)
        const val STAGE_1_RADIUS = 100
        const val STAGE_2_RADIUS = 350
        const val STAGE_3_RADIUS = 700

        // 이상 판정 기준 시간 (분)
        // TODO: 테스트 완료 후 원래 값으로 변경 (60L, 30L, 15L)
        const val STAGE_1_THRESHOLD_MINUTES = 60L  // 원래: 60L
        const val STAGE_2_THRESHOLD_MINUTES = 30L  // 원래: 30L
        const val STAGE_3_THRESHOLD_MINUTES = 15L  // 원래: 15L

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

    /**
     * 위치 업데이트 처리
     * @param latitude 현재 위도
     * @param longitude 현재 경도
     * @param currentTimeMillis 현재 시간 (밀리초)
     */
    fun updateLocation(latitude: Double, longitude: Double, currentTimeMillis: Long) {
        // 홈 위치와의 거리 계산
        val distance = calculateDistance(homeLatitude, homeLongitude, latitude, longitude)

        // 각 단계별 처리
        checkStage1(distance, currentTimeMillis)
        checkStage2(distance, currentTimeMillis)
        checkStage3(distance, currentTimeMillis)
    }

    /**
     * 1단계 (100m) 체크
     */
    private fun checkStage1(distance: Double, currentTimeMillis: Long) {
        if (distance > STAGE_1_RADIUS) {
            // 범위 밖
            if (stage1OutsideStartTime == null) {
                // 처음 범위 밖으로 나감
                stage1OutsideStartTime = currentTimeMillis
                stage1Alerted = false
            } else {
                // 이미 범위 밖 - 연속 체류 시간 계산
                val durationMillis = currentTimeMillis - stage1OutsideStartTime!!
                val durationMinutes = durationMillis / 1000 / 60

                if (durationMinutes >= STAGE_1_THRESHOLD_MINUTES && !stage1Alerted) {
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

    /**
     * 2단계 (350m) 체크
     */
    private fun checkStage2(distance: Double, currentTimeMillis: Long) {
        if (distance > STAGE_2_RADIUS) {
            // 범위 밖
            if (stage2OutsideStartTime == null) {
                // 처음 범위 밖으로 나감
                stage2OutsideStartTime = currentTimeMillis
                stage2Alerted = false
            } else {
                // 이미 범위 밖 - 연속 체류 시간 계산
                val durationMillis = currentTimeMillis - stage2OutsideStartTime!!
                val durationMinutes = durationMillis / 1000 / 60

                if (durationMinutes >= STAGE_2_THRESHOLD_MINUTES && !stage2Alerted) {
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

    /**
     * 3단계 (700m) 체크
     */
    private fun checkStage3(distance: Double, currentTimeMillis: Long) {
        if (distance > STAGE_3_RADIUS) {
            // 범위 밖
            if (stage3OutsideStartTime == null) {
                // 처음 범위 밖으로 나감
                stage3OutsideStartTime = currentTimeMillis
                stage3Alerted = false
            } else {
                // 이미 범위 밖 - 연속 체류 시간 계산
                val durationMillis = currentTimeMillis - stage3OutsideStartTime!!
                val durationMinutes = durationMillis / 1000 / 60

                if (durationMinutes >= STAGE_3_THRESHOLD_MINUTES && !stage3Alerted) {
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

    /**
     * 현재 상태 조회
     */
    fun getCurrentStatus(): SafetyZoneStatus {
        val now = System.currentTimeMillis()
        return SafetyZoneStatus(
            stage1OutsideDurationMinutes = stage1OutsideStartTime?.let { (now - it) / 1000 / 60 },
            stage2OutsideDurationMinutes = stage2OutsideStartTime?.let { (now - it) / 1000 / 60 },
            stage3OutsideDurationMinutes = stage3OutsideStartTime?.let { (now - it) / 1000 / 60 }
        )
    }

    /**
     * 모니터링 리셋
     */
    fun reset() {
        stage1OutsideStartTime = null
        stage2OutsideStartTime = null
        stage3OutsideStartTime = null
        stage1Alerted = false
        stage2Alerted = false
        stage3Alerted = false
    }
}

/**
 * 안전 범위 상태
 */
data class SafetyZoneStatus(
    val stage1OutsideDurationMinutes: Long?,
    val stage2OutsideDurationMinutes: Long?,
    val stage3OutsideDurationMinutes: Long?
)
