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
import kr.co.ongil.data.model.map.ReportAbnormalRequest
import kr.co.ongil.data.model.map.UpdateLocationRequest
import kr.co.ongil.data.websocket.GpsWebSocketManager
import kr.co.ongil.presentation.MainActivity
import kr.co.ongil.BuildConfig
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
    @Inject lateinit var favoriteRepository: kr.co.ongil.domain.repository.FavoriteRepository
    @Inject lateinit var findRouteUseCase: kr.co.ongil.domain.usecase.map.FindRouteUseCase
    @Inject lateinit var safeZoneRepository: kr.co.ongil.domain.repository.SafeZoneRepository

    private lateinit var fusedClient: FusedLocationProviderClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationJob: Job? = null
    private var isTrackingActive = false

    // [테스트용] 1초마다 위치 전송하는 타이머
    private var periodicSendJob: Job? = null

    // 마지막으로 백엔드에 전송한 위치
    private var lastSentLocation: LocationPoint? = null

    // 마지막으로 LocationBus에 전송한 위치
    private var lastEmittedLocation: LocationPoint? = null

    // 안전 범위 모니터 (DataStore에서 설정을 로드하여 초기화)
    private var safetyZoneMonitor: SafetyZoneMonitor? = null

    // 경로 이탈 모니터 (길찾기 중에만 활성화)
    private var routeDeviationMonitor: RouteDeviationMonitor? = null

    // 기본 목적지 좌표 (SafetyZoneMonitor 기준점)
    private var defaultDestinationLatitude: Double? = null
    private var defaultDestinationLongitude: Double? = null

    // 현재 적용된 안전구역 설정 (handleAbnormalDetection에서 사용)
    private var currentSafeZoneSettings: kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings? = null

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        ensureNotificationChannel()

        // 기본 목적지 조회 (SafetyZoneMonitor 기준점)
        serviceScope.launch {
            loadDefaultDestination()
        }

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
    }
    /**
     * 기본 목적지 조회 (SafetyZoneMonitor 기준점)
     */
    private suspend fun loadDefaultDestination() {
        try {
            Log.d(TAG, "🔍 기본 목적지 조회 시작")

            val patientId = userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            Log.d(TAG, "환자 ID: $patientId")

            if (patientId == null) {
                Log.w(TAG, "환자 ID를 가져올 수 없어 기본 목적지를 조회할 수 없습니다")
                return
            }

            val userType = userDataStoreManager.getUserType().first()
            Log.d(TAG, "사용자 타입: $userType")

            if (userType != "PATIENT") {
                Log.d(TAG, "환자가 아니므로 기본 목적지 조회를 스킵합니다")
                return
            }

            // 기본 목적지 조회
            Log.d(TAG, "favoriteRepository.getFavoritePlaces 호출 중... (patientId=$patientId)")
            val favoritePlaces = favoriteRepository.getFavoritePlaces(patientId)

            favoritePlaces.fold(
                onSuccess = { places ->
                    Log.d(TAG, "즐겨찾기 장소 조회 성공: 총 ${places.items.size}개")
                    places.items.forEachIndexed { index, place ->
                        Log.d(TAG, "  [$index] ${place.displayName} - isDefault: ${place.isDefault}")
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "즐겨찾기 장소 조회 실패: ${error.message}", error)
                }
            )

            val defaultPlace = favoritePlaces.getOrNull()?.items?.firstOrNull { it.isDefault }

            if (defaultPlace == null) {
                Log.w(TAG, "⚠️ 기본 목적지가 설정되어 있지 않습니다. 안전범위 모니터링이 비활성화됩니다.")
                return
            }

            defaultDestinationLatitude = defaultPlace.latitude
            defaultDestinationLongitude = defaultPlace.longitude

            Log.d(TAG, "✅ 기본 목적지 로드 완료: ${defaultPlace.displayName} (${defaultPlace.latitude}, ${defaultPlace.longitude})")

            // 기본 목적지 로드 후 SafetyZoneMonitor 재초기화
            val settings = userDataStoreManager.getSafeZoneSettings(patientId)
//            Log.d(TAG, "📋 DataStore에서 로드한 안전구역 설정 (환자 ID: $patientId):")
//            Log.d(TAG, "  - 1단계: ${settings.level1Distance}m / ${settings.level1Dwell}분")
//            Log.d(TAG, "  - 2단계: ${settings.level2Distance}m / ${settings.level2Dwell}분")
//            Log.d(TAG, "  - 3단계: ${settings.level3Distance}m / ${settings.level3Dwell}분")
            updateSafetyZoneMonitor(settings)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 기본 목적지 조회 실패", e)
        }
    }

    private fun updateSafetyZoneMonitor(settings: kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings) {
        try {
            // 기본 목적지 좌표가 없으면 SafetyZoneMonitor 생성하지 않음
            val homeLat = defaultDestinationLatitude
            val homeLon = defaultDestinationLongitude

            if (homeLat == null || homeLon == null) {
                Log.w(TAG, "기본 목적지가 설정되지 않아 SafetyZoneMonitor를 생성하지 않습니다")
                safetyZoneMonitor = null
                currentSafeZoneSettings = null
                return
            }

            // 설정이 변경되었는지 확인
            val currentSettings = currentSafeZoneSettings
            val settingsChanged = currentSettings == null ||
                currentSettings.level1Distance != settings.level1Distance ||
                currentSettings.level1Dwell != settings.level1Dwell ||
                currentSettings.level2Distance != settings.level2Distance ||
                currentSettings.level2Dwell != settings.level2Dwell ||
                currentSettings.level3Distance != settings.level3Distance ||
                currentSettings.level3Dwell != settings.level3Dwell

            if (!settingsChanged && safetyZoneMonitor != null) {
                // 설정이 변경되지 않았고 모니터가 이미 존재하면 유지
                Log.d(TAG, "안전구역 설정이 변경되지 않아 SafetyZoneMonitor 유지 (체류 시간 타이머 보존)")
                return
            }

            // 설정 변경됨 - 새로 저장
            currentSafeZoneSettings = settings

            // SafetyZoneMonitor 재생성 (기본 목적지를 기준점으로 사용)
            safetyZoneMonitor = SafetyZoneMonitor(
                homeLatitude = homeLat,
                homeLongitude = homeLon,
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
            Log.d(TAG, "SafetyZoneMonitor 업데이트 완료: L1=${settings.level1Distance}m/${settings.level1Dwell}분, L2=${settings.level2Distance}m/${settings.level2Dwell}분, L3=${settings.level3Dwell}m/${settings.level3Dwell}분")
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

        val notification = buildNotification()
        startForeground(NOTI_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)

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
                10000L // 10초 간격
            ).setMinUpdateIntervalMillis(5000L) // 최소 5초 간격
             .setMinUpdateDistanceMeters(10f) // 10미터 이상 이동 시
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
                // 정확도 20m 초과하는 위치 무시
                if (loc.accuracy > 20.0f) {
                    Log.w(TAG, " 부정확한 위치 수신 (정확도: ${loc.accuracy}m) - 무시")
                    return
                }
                
                Log.d(TAG, "위치 수신됨: lat=${loc.latitude}, lon=${loc.longitude}, accuracy=${loc.accuracy}m")
                serviceScope.launch {
                    val point = LocationPoint(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracyMeters = loc.accuracy,
                        bearing = loc.bearing,
                        speedMps = loc.speed,
                        timeMillis = loc.time
                    )

                    locationBus.emit(point)
                    Log.d(TAG, "LocationBus에 위치 전송 완료")

                    val userType = userDataStoreManager.getUserType().first()
                    if (userType == "PATIENT") {
                        safetyZoneMonitor?.updateLocation(
                            point.latitude,
                            point.longitude,
                            point.timeMillis
                        )
                    }

                    sendLocationToBackend(point)

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

    private suspend fun sendLocationToBackend(currentLocation: LocationPoint) {
        try {
            if (gpsWebSocketManager.isConnected()) {
                return
            }

            val userType = userDataStoreManager.getUserType().first()
            if (userType != "PATIENT") {
                return
            }

            val lastLocation = lastSentLocation
            if (lastLocation != null) {
                val distance = calculateDistance(
                    lastLocation.latitude, lastLocation.longitude,
                    currentLocation.latitude, currentLocation.longitude
                )
                if (distance < 10.0) { // 10미터 이상 이동 시에만 전송
                    return
                }
            }

            val patientId = userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            if (patientId == null) {
                Log.w(TAG, "patientId를 가져올 수 없습니다")
                return
            }

            val request = UpdateLocationRequest(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )

            mapApi.updatePatientLocation(patientId, request)

            lastSentLocation = currentLocation

        } catch (e: Exception) {
            Log.e(TAG, "백엔드 위치 전송 실패", e)
        }
    }

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

    private suspend fun handleAbnormalDetection(stage: Int, durationMinutes: Long) {
        try {
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

            val homeLatitude = defaultDestinationLatitude ?: return
            val homeLongitude = defaultDestinationLongitude ?: return

            val distance = calculateDistance(
                homeLatitude, homeLongitude,
                currentLocation.latitude, currentLocation.longitude
            )

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

            mapApi.reportAbnormal(patientId, request)

            userDataStoreManager.saveAbnormalDetection(
                isDetected = true,
                stage = stage.toString(),
                detectedTime = Instant.now().toString()
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ 이상 상황 처리 실패", e)
        }
    }

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

            val request = ReportAbnormalRequest(
                abnormalType = "DEVIATE_FROM_THE_PATH", // 경로 이탈
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                safeZoneLevel = "FIRST",
                centerLatitude = 0.0,
                centerLongitude = 0.0,
                distanceFromCenter = distanceFromRoute,
                boundaryRadius = 50.0,
                elapsedTime = 0,
                thresholdTime = 0
            )

            mapApi.reportAbnormal(patientId, request)
            Log.d(TAG, "경로 이탈 알림 전송 성공: ${String.format("%.1f", distanceFromRoute)}m")
        } catch (e: Exception) {
            Log.e(TAG, "경로 이탈 알림 전송 실패", e)
        }
    }
}