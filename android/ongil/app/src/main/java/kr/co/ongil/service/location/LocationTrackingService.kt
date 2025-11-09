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
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.common.location.LocationPoint
import kr.co.ongil.presentation.MainActivity

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
    private lateinit var fusedClient: FusedLocationProviderClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationJob: Job? = null
    private var isTrackingActive = false

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        ensureNotificationChannel()
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
}