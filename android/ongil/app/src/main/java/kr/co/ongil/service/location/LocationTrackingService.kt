package kr.co.ongil.service.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.common.location.LocationPoint
import kr.co.ongil.common.location.SafetyZoneMonitor
import kr.co.ongil.common.location.NavigationRouteManager
import kr.co.ongil.common.location.RouteDeviationMonitor
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.datasource.remote.api.MapApi
import kr.co.ongil.data.datasource.wear.WearDataClient
import kr.co.ongil.data.model.map.ReportAbnormalRequest
import kr.co.ongil.data.model.map.UpdateLocationRequest
import kr.co.ongil.data.websocket.GpsWebSocketManager
import kr.co.ongil.presentation.MainActivity
import java.time.Instant
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Fused Location 기반 포그라운드 서비스
 * - 앱이 백그라운드에서도 위치 추적 유지
 * - 위치 권한 체크 및 에러 핸들링 포함
 */
@AndroidEntryPoint
class LocationTrackingService : Service() {

    companion object {
        private const val TAG = "LocationTrackingService"
        const val ACTION_START = "ongil.action.START_TRACKING"
        const val ACTION_STOP = "ongil.action.STOP_TRACKING"
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val NOTI_ID = 1001
    }

    @Inject lateinit var locationBus: LocationStreamBus
    @Inject lateinit var mapApi: MapApi
    @Inject lateinit var navigationRouteManager: NavigationRouteManager
    @Inject lateinit var userDataStoreManager: UserDataStoreManager
    @Inject lateinit var gpsWebSocketManager: GpsWebSocketManager

    @Inject lateinit var wearDataClient: WearDataClient

    private lateinit var fusedClient: FusedLocationProviderClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationJob: Job? = null
    private var isTrackingActive = false

    // 마지막으로 백엔드에 전송한 위치
    private var lastSentLocation: LocationPoint? = null

    // 마지막으로 LocationBus에 전송한 위치
    private var lastEmittedLocation: LocationPoint? = null

    // 안전 범위 모니터 (DataStore에서 설정을 로드하여 초기화)
    private var safetyZoneMonitor: SafetyZoneMonitor? = null

    // 경로 이탈 모니터 (길찾기 중에만 활성화)
    private var routeDeviationMonitor: RouteDeviationMonitor? = null


    // 현재 적용된 안전구역 설정 (handleAbnormalDetection에서 사용)
    private var currentSafeZoneSettings: kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings? = null

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        ensureNotificationChannel()

        // 경로 변경 구독 (길찾기 시작/종료 감지)
        serviceScope.launch {
            navigationRouteManager.currentRoute.collect() { route ->
                if (route != null) {
                    // 길찾기 시작 - 경로 이탈 모니터 생성
                    Log.d(TAG, "길찾기 시작 감지 - 경로 이탈 모니터 활성화")
                    routeDeviationMonitor = RouteDeviationMonitor(
                        routePath = route.path,
                        deviationThresholdMeters = 50.0,
                        onRouteDeviation = { distance ->
                            Log.w(TAG, "⚠️ 경로 이탈 감지: ${String.format("%.1f", distance)}m 벗어남")
                            serviceScope.launch {
                                handleRouteDeviation(distance)
                            }
                        }
                    )
                } else {
                    // 길찾기 종료 - 경로 이탈 모니터 해제
                    Log.d(TAG, "길찾기 종료 감지 - 경로 이탈 모니터 비활성화")
                    routeDeviationMonitor = null
                }
            }
        }

        // 안전구역 설정 변경 구독
        serviceScope.launch {
            userDataStoreManager.observeSafeZoneSettings().collect { settings ->
                Log.d(TAG, "안전구역 설정 변경 감지")
                updateSafetyZoneMonitor(settings)
            }
        }
    }
    private fun updateSafetyZoneMonitor(settings: kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings) {
        try {
            currentSafeZoneSettings = settings

            // SafetyZoneMonitor 재생성 (홈 위치는 하드코딩, 추후 사용자 설정 추가 가능)
            safetyZoneMonitor = SafetyZoneMonitor(
                homeLatitude = 37.50175822768635,
                homeLongitude = 127.03958229478599,
                level1Distance = settings.level1Distance,
                level1Dwell = settings.level1Dwell,
                level2Distance = settings.level2Distance,
                level2Dwell = settings.level2Dwell,
                level3Distance = settings.level3Distance,
                level3Dwell = settings.level3Dwell,
                onAbnormalDetected = { stage, durationMinutes ->
                    // 이상 판정 콜백
                    Log.w(TAG, "⚠️ 이상 상황 감지: ${stage}단계, ${durationMinutes}분 경과")
                    serviceScope.launch {
                        handleAbnormalDetection(stage, durationMinutes)
                    }
                }
            )
            Log.d(TAG, "SafetyZoneMonitor 업데이트 완료: L1=${settings.level1Distance}m/${settings.level1Dwell}분, L2=${settings.level2Distance}m/${settings.level2Dwell}분, L3=${settings.level3Distance}m/${settings.level3Dwell}분")
        } catch (e: Exception) {
            Log.e(TAG, "SafetyZoneMonitor 업데이트 실패", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        if (isTrackingActive) {
            Log.d(TAG, "이미 추적 중입니다.")
            return
        }

        // Foreground Service는 시작 후 5초 내에 startForeground()를 호출해야 함
        // 권한 체크보다 먼저 호출해야 크래시를 방지할 수 있음
        val notification = buildNotification()
        startForeground(NOTI_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)

        // 위치 권한 체크
        if (!hasLocationPermission()) {
            Log.w(TAG, "위치 권한이 없습니다. 알림만 표시하고 위치 추적은 하지 않습니다.")
            return
        }

        isTrackingActive = true
        locationJob = serviceScope.launch {
            requestLocationUpdates()
        }
        Log.d(TAG, "위치 추적 시작")
    }

    private fun stopTracking() {
        isTrackingActive = false
        locationJob?.cancel()
        locationJob = null

        try {
            fusedClient.removeLocationUpdates(callback)
        } catch (e: Exception) {
            Log.e(TAG, "위치 업데이트 제거 실패", e)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
        Log.d(TAG, "위치 추적 중지")
    }

    private suspend fun requestLocationUpdates() = withContext(Dispatchers.Main) {
        if (!hasLocationPermission()) {
            Log.w(TAG, "위치 권한이 없어 위치 업데이트를 요청할 수 없습니다.")
            return@withContext
        }

        try {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                2000L // 2초 간격
            ).setMinUpdateDistanceMeters(3f)
                .setWaitForAccurateLocation(true)
                .build()

            fusedClient.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "위치 업데이트 요청 성공")
        } catch (e: SecurityException) {
            Log.e(TAG, "위치 권한 부족으로 업데이트 요청 실패", e)
            stopTracking()
        } catch (e: Exception) {
            Log.e(TAG, "위치 업데이트 요청 중 오류 발생", e)
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                serviceScope.launch {
                    val point = LocationPoint(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracyMeters = loc.accuracy,
                        bearing = loc.bearing,
                        speedMps = loc.speed,
                        timeMillis = loc.time
                    )

                    val accuracy = loc.accuracy
                    val speed = loc.speed

                    // 1. 정확도 필터링 (40m 초과는 무시)
                    if (accuracy > 40f) {
                        Log.w(TAG, "⚠️ 정확도 낮음 (${accuracy}m) - 위치 무시")
                        return@launch
                    }

                    // 2. 거리 및 속도 기반 필터링
                    val lastEmitted = lastEmittedLocation
                    if (lastEmitted != null) {
                        val distance = calculateDistance(
                            lastEmitted.latitude, lastEmitted.longitude,
                            point.latitude, point.longitude
                        )

                        // 시간 차이 계산 (초 단위)
                        val timeDiffSeconds = (point.timeMillis - lastEmitted.timeMillis) / 1000.0

                        // 속도와 거리 일관성 체크
                        if (speed > 1.0f && timeDiffSeconds > 0) {
                            // 예상 이동 거리 (속도 * 시간)
                            val expectedDistance = speed * timeDiffSeconds
                            // 실제 이동 거리와 예상 이동 거리의 차이
                            val distanceDiff = Math.abs(expectedDistance - distance)

                            // 차이가 너무 크면 GPS 오류로 판단 (예상의 80% 이상 차이) - 기준 완화
                            if (distanceDiff > expectedDistance * 0.8) {
                                Log.w(TAG, "⚠️ 속도-거리 불일치 감지 (속도: ${String.format("%.1f", speed)}m/s, 실제: ${String.format("%.1f", distance)}m, 예상: ${String.format("%.1f", expectedDistance)}m) - 무시")
                                return@launch
                            }
                        }

                        // 속도 기반 필터링 (정지/저속 상태에서 GPS 드리프트 방지) - 기준 완화
                        when {
                            speed < 0.5f -> {
                                // 거의 정지 상태 (0.5m/s = 1.8km/h 미만) - 5m로 완화
                                if (distance > 5.0) {
                                    Log.w(TAG, "⚠️ 정지 상태 GPS 점프 감지 (${String.format("%.1f", distance)}m, 속도: ${String.format("%.1f", speed)}m/s) - 무시")
                                    return@launch
                                }
                            }
                            speed < 1.5f -> {
                                // 느린 보행 (0.5-1.5m/s, 1.8-5.4km/h) - 기준 완화
                                val maxJump = when {
                                    accuracy < 20f -> 30.0   // 8.0 → 30.0
                                    accuracy < 30f -> 50.0   // 10.0 → 50.0
                                    else -> 60.0             // 15.0 → 60.0
                                }
                                if (distance > maxJump) {
                                    Log.w(TAG, "⚠️ 저속 이동 중 GPS 점프 감지 (${String.format("%.1f", distance)}m, 속도: ${String.format("%.1f", speed)}m/s) - 무시")
                                    return@launch
                                }
                            }

                        }

                        // 정확도에 따른 최소 이동 거리 (동적 임계값)
                        val minDistance = when {
                            accuracy < 15f -> 2.0   // 정확도 좋음 (15m 미만): 2m
                            accuracy < 25f -> 5.0   // 정확도 보통 (15-25m): 5m
                            else -> 10.0            // 정확도 나쁨 (25-40m): 10m
                        }

                        // 최소 이동 거리 체크
                        if (distance < minDistance) {
                            Log.d(TAG, "이동 거리 ${String.format("%.1f", distance)}m < ${minDistance}m - 무시")
                            return@launch
                        }

                        Log.d(TAG, "✅ 위치 수신: ${String.format("%.1f", distance)}m 이동, 속도: ${String.format("%.1f", speed)}m/s, 정확도: ${String.format("%.0f", accuracy)}m")
                    } else {
                        Log.d(TAG, "✅ 첫 위치 수신: lat=${loc.latitude}, lon=${loc.longitude}, accuracy=${accuracy}m")
                    }

                    // 필터링 통과 - LocationBus에 위치 전송
                    lastEmittedLocation = point
                    locationBus.emit(point)
                    Log.d(TAG, "LocationBus에 위치 전송 완료")

                    // 워치로 위치 전송 (백그라운드로 실행, 실패해도 무시)
                    serviceScope.launch {
                        try {
                            wearDataClient.syncLocation(point.latitude, point.longitude)
                        } catch (e: Exception) {
                            Log.e(TAG, "워치 위치 동기화 실패 (무시)", e)
                        }
                    }

                    // 안전 범위 모니터링 (환자일 때만)
                    val userType = userDataStoreManager.getUserType().first()
                    if (userType == "PATIENT") {
                        safetyZoneMonitor?.updateLocation(
                            point.latitude,
                            point.longitude,
                            point.timeMillis
                        )
                    }

                    // 백엔드로 위치 전송 (환자일 때만, 5m 이상 이동 시)
                    sendLocationToBackend(point)

                    // 경로 이탈 모니터링 (길찾기 중일 때만)
                    if (gpsWebSocketManager.isConnected()) {
                        routeDeviationMonitor?.updateLocation(
                            point.latitude,
                            point.longitude
                        )
                    }
                }
            } ?: run {
                Log.w(TAG, "LocationResult에 lastLocation이 null입니다")
            }
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("온길 길찾기")
            .setContentText("길찾기 진행 중 · 위치 추적 활성화")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun ensureNotificationChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "길찾기 위치 추적",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "길찾기 진행 중 위치 정보를 수집합니다"
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    /**
     * 백엔드로 위치 전송 (환자일 때만, 1m 이상 이동 시)
     * 길찾기 중(WebSocket 연결 중)에는 WebSocket으로 전송하므로 API 호출 안 함
     */
    private suspend fun sendLocationToBackend(currentLocation: LocationPoint) {
        try {
            // 0. 길찾기 중이면 WebSocket으로 전송하므로 API 호출 스킵
            if (gpsWebSocketManager.isConnected()) {
                Log.d(TAG, "WebSocket 연결 중 - API 전송 건너뜀")
                return
            }

            // 1. 사용자 타입 확인 (환자만 전송)
            val userType = userDataStoreManager.getUserType().first()
            if (userType != "PATIENT") {
                return
            }

            // 2. 1m 이상 이동했는지 확인
            val lastLocation = lastSentLocation
            if (lastLocation != null) {
                val distance = calculateDistance(
                    lastLocation.latitude, lastLocation.longitude,
                    currentLocation.latitude, currentLocation.longitude
                )
                if (distance < 1.0) {
                    Log.d(TAG, "이동 거리 ${String.format("%.1f", distance)}m - 전송 건너뜀")
                    return
                }
                Log.d(TAG, "이동 거리 ${String.format("%.1f", distance)}m - 백엔드로 전송")
            } else {
                Log.d(TAG, "첫 위치 - 백엔드로 전송")
            }

            // 3. 환자 ID 가져오기
            val patientId = userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            if (patientId == null) {
                Log.w(TAG, "patientId를 가져올 수 없습니다")
                return
            }

            // 4. 백엔드로 전송
            val request = UpdateLocationRequest(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )

            val response = mapApi.updatePatientLocation(patientId, request)
            Log.d(TAG, "위치 전송 성공: ${response.message}")

            // 5. 마지막 전송 위치 업데이트
            lastSentLocation = currentLocation

        } catch (e: Exception) {
            Log.e(TAG, "백엔드 위치 전송 실패", e)
        }
    }

    /**
     * 두 지점 간 거리 계산 (Haversine formula, 미터 단위)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    /**
     * 이상 상황 감지 시 처리
     * - 백엔드 API로 알림 전송
     * - DataStore에 상태 저장
     */
    private suspend fun handleAbnormalDetection(stage: Int, durationMinutes: Long) {
        try {
            val patientId = userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            if (patientId == null) {
                Log.w(TAG, "patientId를 가져올 수 없습니다")
                return
            }

            // 현재 위치 가져오기
            val currentLocation = locationBus.lastValue
            if (currentLocation == null) {
                Log.w(TAG, "현재 위치를 가져올 수 없습니다")
                return
            }

            // 홈 위치 (SafetyZoneMonitor와 동일한 값 사용)
            val homeLatitude = 37.50175822768635
            val homeLongitude = 127.03958229478599

            // 거리 계산
            val distance = calculateDistance(
                homeLatitude, homeLongitude,
                currentLocation.latitude, currentLocation.longitude
            )

            // 단계별 정보 (currentSafeZoneSettings 사용, 없으면 기본값 사용)
            val settings = currentSafeZoneSettings
            val (safeZoneLevel, boundaryRadius, thresholdMinutes) = when (stage) {
                1 -> Triple("FIRST", (settings?.level1Distance ?: SafetyZoneMonitor.DEFAULT_STAGE_1_RADIUS).toDouble(), settings?.level1Dwell ?: SafetyZoneMonitor.DEFAULT_STAGE_1_THRESHOLD_MINUTES)
                2 -> Triple("SECOND", (settings?.level2Distance ?: SafetyZoneMonitor.DEFAULT_STAGE_2_RADIUS).toDouble(), settings?.level2Dwell ?: SafetyZoneMonitor.DEFAULT_STAGE_2_THRESHOLD_MINUTES)
                3 -> Triple("THIRD", (settings?.level3Distance ?: SafetyZoneMonitor.DEFAULT_STAGE_3_RADIUS).toDouble(), settings?.level3Dwell ?: SafetyZoneMonitor.DEFAULT_STAGE_3_THRESHOLD_MINUTES)
                else -> {
                    Log.e(TAG, "알 수 없는 단계: $stage")
                    return
                }
            }

            // API Request 생성
            val request = ReportAbnormalRequest(
                abnormalType = "WANDER", // 배회 감지
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                safeZoneLevel = safeZoneLevel,
                centerLatitude = homeLatitude,
                centerLongitude = homeLongitude,
                distanceFromCenter = distance,
                boundaryRadius = boundaryRadius,
                elapsedTime = (durationMinutes * 60).toInt(), // 분 → 초
                thresholdTime = (thresholdMinutes * 60).toInt() // 분 → 초
            )

            Log.w(TAG, """
                ⚠️ 이상 상황 감지 - 백엔드로 전송
                - 환자 ID: $patientId
                - 단계: $safeZoneLevel ($boundaryRadius m)
                - 연속 체류 시간: ${durationMinutes}분
                - 현재 위치: ${currentLocation.latitude}, ${currentLocation.longitude}
                - 중심으로부터 거리: ${String.format("%.1f", distance)}m
            """.trimIndent())

            // 백엔드 API 호출
            val response = mapApi.reportAbnormal(patientId, request)
            Log.i(TAG, "✅ 이상 상황 알림 전송 성공: ${response.message}")

            // DataStore에 상태 저장
            userDataStoreManager.saveAbnormalDetection(
                isDetected = true,
                stage = stage.toString(),
                detectedTime = Instant.now().toString()
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ 이상 상황 처리 실패", e)
        }
    }

    /**
     * 경로 이탈 이상상황 처리
     */
    private suspend fun handleRouteDeviation(distanceFromRoute: Double) {
        try {
            val userType = userDataStoreManager.getUserType().first()
            if (userType != "PATIENT") return

            val patientId = userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            if (patientId == null) {
                Log.w(TAG, "patientId를 가져올 수 없습니다")
                return
            }

            val currentLocation = locationBus.lastValue
            if (currentLocation == null) {
                Log.w(TAG, "현재 위치를 가져올 수 없습니다")
                return
            }

            // 백엔드로 이상상황 알림
            val request = ReportAbnormalRequest(
                abnormalType = "DEVIATE_FROM_THE_PATH", // 경로 이탈
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                safeZoneLevel = "FIRST",    // 임시 테스트용 TODO: 백엔드 수정되면 삭제
                centerLatitude = 0.0,       // 임시 테스트용 TODO: 백엔드 수정되면 삭제
                centerLongitude = 0.0,      // 임시 테스트용 TODO: 백엔드 수정되면 삭제
                distanceFromCenter = distanceFromRoute,
                boundaryRadius = 50.0,
                elapsedTime = 0,            // 임시 테스트용 TODO: 백엔드 수정되면 삭제
                thresholdTime = 0           // 임시 테스트용 TODO: 백엔드 수정되면 삭제
            )

            mapApi.reportAbnormal(patientId, request)
            Log.d(TAG, "경로 이탈 알림 전송 성공: ${String.format("%.1f", distanceFromRoute)}m")
        } catch (e: Exception) {
            Log.e(TAG, "경로 이탈 알림 전송 실패", e)
        }
    }
}