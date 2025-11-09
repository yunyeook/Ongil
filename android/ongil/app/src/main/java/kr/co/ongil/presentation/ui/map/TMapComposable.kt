package kr.co.ongil.presentation.ui.map

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem
import kr.co.ongil.common.location.LocationPoint
import kr.co.ongil.common.location.LocationStreamBus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
/**
 * TMap을 표시하는 Composable 컴포넌트 (앱 전용)
 *
 * @param modifier 레이아웃 수정자
 * @param latitude 지도 중심 위도 (기본값: 서울 시청)
 * @param longitude 지도 중심 경도 (기본값: 서울 시청)
 * @param zoomLevel 줌 레벨 (기본값: 15)
 * @param locationBus 위치 스트림 버스 (DI로 주입받아야 함)
 * @param enableTracking 위치 추적 활성화 여부 (기본값: false)
 * @param showMyLocationButton 내 위치 버튼 표시 여부 (기본값: true)
 * @param onMapReady TMapView가 준비되었을 때 호출되는 콜백
 */
@Composable
fun TMapComposable(
    modifier: Modifier = Modifier,
    latitude: Double = 37.5665,
    longitude: Double = 126.9780,
    zoomLevel: Int = 15,
    locationBus: LocationStreamBus? = null,
    enableTracking: Boolean = false,
    onMapReady: ((TMapView) -> Unit)? = null,
    myLocationTrigger: Int = 0  // 이 값이 변경되면 현재 위치로 이동
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<TMapView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isMapInitialized by remember { mutableStateOf(false) }
    var lastPoint by remember { mutableStateOf<LocationPoint?>(null) }
    var locationMarker by remember { mutableStateOf<TMapMarkerItem?>(null) }
    var isFollowMode by remember { mutableStateOf(true) } // 팔로우 모드
    var lastUpdatePoint by remember { mutableStateOf<LocationPoint?>(null) } // 마지막 업데이트 위치

    // 펄스 애니메이션용 프레임들 (미리 생성)
    val pulseFrames = remember { createPulseFrames(context) }
    var currentPulseFrame by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            val tmapView = TMapViewFactory.createTMapView(
                context = context,
                latitude = latitude,
                longitude = longitude,
                zoomLevel = zoomLevel
            )

            // TMapView 생성 직후 약간의 딜레이를 주어 내부 초기화 완료 대기
            delay(300)

            // 컴퍼스 모드는 안전하게 설정 가능
            tmapView.setCompassMode(false)

            // 사용자가 지도를 터치하면 팔로우 모드 OFF
            tmapView.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        // 터치 시작 시 팔로우 모드 OFF
                        isFollowMode = false
                        Log.d("TMapComposable", "사용자 터치 감지, 팔로우 모드 OFF")
                    }
                }
                false // 이벤트를 TMapView가 계속 처리하도록
            }

            mapView = tmapView
            isMapInitialized = true
            isLoading = false

            onMapReady?.invoke(tmapView)
            Log.d("TMapComposable", "TMapView initialization complete")
        } catch (e: Exception) {
            Log.e("TMapComposable", "TMapView initialization failed", e)
            isLoading = false
        }
    }

    // 내 위치 버튼 클릭 시 현재 위치로 이동
    LaunchedEffect(myLocationTrigger) {
        if (myLocationTrigger > 0) {  // 0은 초기값이므로 무시
            val tmap = mapView
            val p = lastPoint
            if (tmap != null && p != null) {
                try {
                    isFollowMode = true
                    tmap.setCenterPoint(p.latitude, p.longitude)
                    Log.d("TMapComposable", "팔로우 모드 활성화 및 위치 이동")
                } catch (e: Exception) {
                    Log.e("TMapComposable", "Failed to recenter to my location", e)
                }
            }
        }
    }

    // 펄스 애니메이션 루프
    LaunchedEffect(locationMarker) {
        val marker = locationMarker ?: return@LaunchedEffect
        val tmap = mapView ?: return@LaunchedEffect

        while (true) {
            delay(100) // 100ms마다 프레임 변경 (초당 10프레임)
            currentPulseFrame = (currentPulseFrame + 1) % pulseFrames.size

            try {
                marker.icon = pulseFrames[currentPulseFrame]
                tmap.updateTMapMarkerItem(marker)
            } catch (e: Exception) {
                Log.e("TMapComposable", "펄스 애니메이션 업데이트 실패", e)
            }
        }
    }

    // 위치 업데이트 수신 (locationBus가 제공된 경우에만)
    LaunchedEffect(mapView, isMapInitialized) {
        if (!isMapInitialized) {
            Log.d("TMapComposable", "맵이 아직 초기화되지 않음")
            return@LaunchedEffect
        }
        val tmap = mapView ?: run {
            Log.d("TMapComposable", "맵뷰가 null")
            return@LaunchedEffect
        }
        val bus = locationBus ?: run {
            Log.d("TMapComposable", "locationBus가 null")
            return@LaunchedEffect
        }

        Log.d("TMapComposable", "위치 업데이트 수신 시작")
        try {
            bus.updates.collectLatest { point ->
                try {
                    lastPoint = point

                    // 필터링: 정확도 50m 이상인 경우 무시
                    val accuracy = point.accuracyMeters
                    if (accuracy != null && accuracy > 50f) {
                        Log.w("TMapComposable", "정확도 낮음 (${accuracy}m), 무시")
                        return@collectLatest
                    }

                    // 업데이트 조건: 이전 위치와 2m 이상 차이날 때만
                    val shouldUpdate = lastUpdatePoint == null ||
                        calculateDistance(lastUpdatePoint!!, point) > 2.0

                    if (!shouldUpdate) {
                        return@collectLatest
                    }

                    Log.d("TMapComposable", "위치 수신: lat=${point.latitude}, lon=${point.longitude}, accuracy=${point.accuracyMeters}m")

                    // 마커 생성 또는 업데이트
                    if (locationMarker == null) {
                        // 첫 마커 생성 - 펄스 애니메이션 프레임으로 시작
                        val markerBitmap = pulseFrames[currentPulseFrame]
                        val marker = TMapMarkerItem().apply {
                            id = "location_marker"
                            tMapPoint = TMapPoint(point.latitude, point.longitude)
                            icon = markerBitmap
                        }

                        try {
                            tmap.addTMapMarkerItem(marker)
                            Log.d("TMapComposable", "TMapMarkerItem 추가 완료")
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "TMapMarkerItem 추가 실패", e)
                        }

                        locationMarker = marker

                        // 첫 위치로 지도 이동
                        tmap.setCenterPoint(point.latitude, point.longitude)
                        tmap.setZoomLevel(17) // 줌 레벨을 높여서 마커가 잘 보이게
                        Log.d("TMapComposable", "위치 마커 생성 완료: lat=${point.latitude}, lon=${point.longitude}")
                    } else {
                        // 마커 위치 업데이트
                        try {
                            locationMarker?.tMapPoint = TMapPoint(point.latitude, point.longitude)
                            tmap.updateTMapMarkerItem(locationMarker)
                            Log.d("TMapComposable", "마커 위치 업데이트: ${point.latitude},${point.longitude}")
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "마커 위치 업데이트 실패", e)
                        }

                        // 팔로우 모드일 때만 지도 중심 이동
                        if (isFollowMode) {
                            tmap.setCenterPoint(point.latitude, point.longitude)
                        }
                    }

                    lastUpdatePoint = point
                    Log.d("TMapComposable", "지도 위치 갱신 완료, 팔로우 모드: $isFollowMode")
                } catch (e: Exception) {
                    Log.e("TMapComposable", "Failed to update location on map", e)
                }
            }
        } catch (e: Exception) {
            Log.e("TMapComposable", "Failed to collect location updates", e)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 지도가 준비되면 표시
        mapView?.let { tmapView ->
            AndroidView(
                factory = { tmapView },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 로딩 인디케이터
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    // 화면이 제거될 때 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            try {
                Log.d("TMapComposable", "TMapView disposed")
            } catch (e: Exception) {
                Log.e("TMapComposable", "Error during dispose", e)
            } finally {
                mapView = null
            }
        }
    }
}

/**
 * 펄스 애니메이션 프레임들을 미리 생성 (재사용)
 * @param context 안드로이드 컨텍스트
 * @param frameCount 프레임 수 (기본 8프레임)
 * @return 펄스 애니메이션 비트맵 리스트
 */
private fun createPulseFrames(
    context: android.content.Context,
    frameCount: Int = 8
): List<android.graphics.Bitmap> {
    val frames = mutableListOf<android.graphics.Bitmap>()
    val markerSizeDp = 24
    val pulseSizeDp = 48 // 펄스 효과를 포함한 전체 크기
    val sizePx = (pulseSizeDp * context.resources.displayMetrics.density).toInt()
    val markerRadius = (markerSizeDp * context.resources.displayMetrics.density / 2f)

    for (i in 0 until frameCount) {
        val bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val centerX = sizePx / 2f
        val centerY = sizePx / 2f

        // 펄스 진행도 (0.0 ~ 1.0)
        val progress = i.toFloat() / frameCount

        // 펄스 원 그리기 (점점 커지면서 투명해짐)
        // 프레임 경계(반지름 24dp)보다 작게 유지하기 위해 0.8배로 제한
        val pulseRadius = markerRadius + (markerRadius * 0.8f * progress)
        // 더 빨리 투명해지도록 조정 (progress 0.7~1.0에서 급격히 투명)
        val fadeProgress = if (progress < 0.7f) {
            progress / 0.7f * 0.5f  // 0.0 ~ 0.5
        } else {
            0.5f + ((progress - 0.7f) / 0.3f * 0.5f)  // 0.5 ~ 1.0
        }
        val pulseAlpha = (180 * (1f - fadeProgress)).toInt()
        val pulsePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#5C7165")
            alpha = pulseAlpha
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, pulseRadius, pulsePaint)

        // 메인 마커 (그림자 포함)
        val shadowPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(6f, 0f, 0f, android.graphics.Color.argb(150, 0, 0, 0))
        }
        canvas.drawCircle(centerX, centerY, markerRadius, shadowPaint)

        // 마커 내부
        val fillPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#5c7165")
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, markerRadius - 3f, fillPaint)

        frames.add(bitmap)
    }

    return frames
}

/**
 * 두 위치 간의 거리 계산 (미터 단위)
 * Haversine formula 사용
 */
private fun calculateDistance(p1: LocationPoint, p2: LocationPoint): Double {
    val lat1 = Math.toRadians(p1.latitude)
    val lat2 = Math.toRadians(p2.latitude)
    val lon1 = Math.toRadians(p1.longitude)
    val lon2 = Math.toRadians(p2.longitude)

    val dLat = lat2 - lat1
    val dLon = lon2 - lon1

    val a = sin(dLat / 2).pow(2) +
            cos(lat1) * cos(lat2) *
            sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val earthRadius = 6371000.0 // 지구 반경 (미터)

    return earthRadius * c
}
