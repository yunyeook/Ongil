package kr.co.ongil.wear.service.location

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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kr.co.ongil.common.location.LocationPoint
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.wear.data.datasource.local.WearDataStoreManager
import kr.co.ongil.wear.domain.repository.LocationRepository
import kr.co.ongil.wear.presentation.WearMainActivity
import javax.inject.Inject

/**
 * Wear OS용 위치 추적 Foreground Service
 *
 * 주요 기능:
 * 1. FusedLocationProviderClient를 사용한 위치 추적
 * 2. LocationStreamBus로 앱 내 브로드캐스트
 * 3. 서버로 주기적 위치 전송
 * 4. 안전 범위 모니터링
 * 5. 배터리 최적화 (동적 업데이트 간격)
 */
@AndroidEntryPoint
class WearLocationTrackingService : Service() {

    companion object {
        private const val TAG = "WearLocationService"
        const val ACTION_START = "kr.co.ongil.wear.START_TRACKING"
        const val ACTION_STOP = "kr.co.ongil.wear.STOP_TRACKING"
        private const val CHANNEL_ID = "wear_location_tracking"
        private const val NOTIFICATION_ID = 2001

        // 위치 업데이트 간격
        private const val LOCATION_UPDATE_INTERVAL_MOVING = 5000L // 5초 (이동 중)
        private const val LOCATION_UPDATE_INTERVAL_STATIONARY = 30000L // 30초 (정지 중)
        private const val MIN_DISTANCE_METERS = 3f // 최소 이동 거리

        // 배터리 최적화 임계값
        private const val SPEED_THRESHOLD_MPS = 0.5f // 0.5 m/s 이하면 정지로 판단
    }

    @Inject
    lateinit var locationStreamBus: LocationStreamBus

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var dataStoreManager: WearDataStoreManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationJob: Job? = null
    private var isTrackingActive = false

    // 마지막 전송 위치 (중복 전송 방지)
    private var lastSentLocation: LocationPoint? = null

    // 현재 업데이트 간격 (동적 조정)
    private var currentUpdateInterval = LOCATION_UPDATE_INTERVAL_MOVING

    // 환자 ID (서버 전송 시 필요)
    private var patientId: Long? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START -> {
                if (!isTrackingActive) {
                    startLocationTracking()
                }
            }
            ACTION_STOP -> {
                stopLocationTracking()
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        stopLocationTracking()
        serviceScope.cancel()
    }

    /**
     * 위치 추적 시작
     */
    private fun startLocationTracking() {
        Log.d(TAG, "Starting location tracking")

        // Foreground Service 시작
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        isTrackingActive = true

        // 환자 ID 로드 및 안전 범위 설정 로드
        locationJob = serviceScope.launch {
            // 환자 ID 가져오기
            val userId = dataStoreManager.getUserId().first()
            patientId = userId?.toLongOrNull()

            // 위치 업데이트 시작
            startLocationUpdates()
        }
    }

    /**
     * 위치 추적 중지
     */
    private fun stopLocationTracking() {
        Log.d(TAG, "Stopping location tracking")
        isTrackingActive = false
        locationJob?.cancel()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * FusedLocationProvider로 위치 업데이트 시작
     */
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted")
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            currentUpdateInterval
        ).apply {
            setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates requested")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to request location updates", e)
            stopSelf()
        }
    }

    /**
     * 위치 콜백
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                handleLocationUpdate(location)
            }
        }
    }

    /**
     * 위치 업데이트 처리
     */
    private fun handleLocationUpdate(location: android.location.Location) {
        val locationPoint = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            bearing = location.bearing,
            speedMps = location.speed,
            timeMillis = System.currentTimeMillis()
        )

        Log.d(TAG, "Location update: ${locationPoint.latitude}, ${locationPoint.longitude}")

        // 1. LocationStreamBus로 앱 내 브로드캐스트
        locationStreamBus.tryEmit(locationPoint)

        // 2. 서버로 위치 전송 (블루투스 모델: Phone으로 relay)
        // TODO: 필요 시 활성화 (현재는 주석 처리)
        // if (shouldSendToServer(locationPoint)) {
        //     sendLocationToServer(locationPoint)
        // }

        // 3. 배터리 최적화 - 동적 업데이트 간격 조정
        adjustLocationUpdateInterval(location.speed)
    }

    /**
     * 서버로 전송할지 여부 판단 (블루투스 모델: Phone으로 relay)
     *
     * TODO: 필요 시 활성화 (현재는 주석 처리)
     */
    private fun shouldSendToServer(newLocation: LocationPoint): Boolean {
        val lastSent = lastSentLocation ?: return true

        // 최소 5초 간격
        val timeDiff = newLocation.timeMillis - lastSent.timeMillis
        if (timeDiff < 5000) return false

        // 최소 10m 이동
        val distance = calculateDistance(
            lastSent.latitude, lastSent.longitude,
            newLocation.latitude, newLocation.longitude
        )
        return distance >= 10f
    }

    /**
     * 서버로 위치 전송 (블루투스 모델: Watch → Phone → Server)
     *
     * LocationRepository가 WearDataClient를 사용하여 Phone으로 relay
     * Phone의 WearMessageListenerService가 수신 → 서버로 전송
     *
     * TODO: 필요 시 활성화 (현재는 주석 처리)
     */
    private fun sendLocationToServer(locationPoint: LocationPoint) {
        val currentPatientId = patientId ?: run {
            Log.w(TAG, "Patient ID not available, skipping server upload")
            return
        }

        serviceScope.launch {
            try {
                // LocationRepository → WearDataClient → Phone → Server
                val result = locationRepository.updatePatientLocation(
                    patientId = currentPatientId,
                    latitude = locationPoint.latitude,
                    longitude = locationPoint.longitude
                )

                if (result.isSuccess) {
                    lastSentLocation = locationPoint
                    Log.d(TAG, "Location sent to Phone successfully")
                } else {
                    Log.e(TAG, "Failed to send location: ${result.exceptionOrNull()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending location to Phone", e)
            }
        }
    }

    /**
     * 배터리 최적화를 위한 동적 업데이트 간격 조정
     */
    private fun adjustLocationUpdateInterval(speedMps: Float) {
        val newInterval = if (speedMps > SPEED_THRESHOLD_MPS) {
            LOCATION_UPDATE_INTERVAL_MOVING // 이동 중: 5초
        } else {
            LOCATION_UPDATE_INTERVAL_STATIONARY // 정지 중: 30초
        }

        if (newInterval != currentUpdateInterval) {
            currentUpdateInterval = newInterval
            Log.d(TAG, "Adjusting update interval to ${currentUpdateInterval}ms")

            // 위치 업데이트 재시작
            serviceScope.launch {
                if (ContextCompat.checkSelfPermission(
                        this@WearLocationTrackingService,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    startLocationUpdates()
                }
            }
        }
    }

    /**
     * Haversine 공식을 사용한 거리 계산 (meters)
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val R = 6371000f // 지구 반지름 (meters)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c.toFloat()
    }

    /**
     * Notification Channel 생성
     */
    private fun ensureNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "워치 위치 추적",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "온길 워치 앱 위치 추적 서비스"
            setShowBadge(false)
        }

        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Foreground Notification 생성
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, WearMainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("온길 위치 추적")
            .setContentText("현재 위치를 추적하고 있습니다")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
