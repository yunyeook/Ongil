package kr.co.ongil.presentation.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.viewinterop.AndroidView
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem
import kr.co.ongil.common.location.LocationPoint
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.data.model.location.Coordinate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * TMap을 표시하는 Composable 컴포넌트
 * - 현재 위치 기반으로 지도 초기화
 * - 위치 추적 및 마커 표시 (펄스 애니메이션 포함)
 */
@Composable
fun TMapComposable(
    modifier: Modifier = Modifier,
    zoomLevel: Int = 15,
    locationBus: LocationStreamBus? = null,
    enableTracking: Boolean = false,
    onMapReady: ((TMapView) -> Unit)? = null,
    myLocationTrigger: Int = 0,
    northUpTrigger: Int = 0,  // 북쪽 고정 트리거
    userType: String = "",
    selectedPatientId: String? = null,
    patientLocations: Map<Long, Coordinate> = emptyMap()  // 환자 위치 (보호자용)
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<TMapView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isMapInitialized by remember { mutableStateOf(false) }
    var lastPoint by remember { mutableStateOf<LocationPoint?>(null) }
    var locationMarker by remember { mutableStateOf<TMapMarkerItem?>(null) }
    var isFollowMode by remember { mutableStateOf(true) }
    var currentPulseFrame by remember { mutableStateOf(0) }

    // 환자 마커 (보호자는 선택된 환자 1명만)
    var patientMarker by remember { mutableStateOf<TMapMarkerItem?>(null) }

    // 펄스 애니메이션 프레임 생성 (녹색)
    val pulseFrames = remember {
        createPulseFrames(context, color = "#5C7165")
    }

    // 펄스 애니메이션 루프 (환자용 - 자신의 위치)
    LaunchedEffect(locationMarker) {
        if (userType != "PATIENT") return@LaunchedEffect  // 환자만 자신의 위치 표시
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

    // 펄스 애니메이션 루프 (보호자용 - 선택된 환자의 위치)
    LaunchedEffect(patientMarker) {
        if (userType != "GUARDIAN") return@LaunchedEffect  // 보호자만 환자 위치 표시
        val marker = patientMarker ?: return@LaunchedEffect
        val tmap = mapView ?: return@LaunchedEffect

        while (true) {
            delay(100) // 100ms마다 프레임 변경 (초당 10프레임)
            currentPulseFrame = (currentPulseFrame + 1) % pulseFrames.size

            try {
                marker.icon = pulseFrames[currentPulseFrame]
                tmap.updateTMapMarkerItem(marker)
            } catch (e: Exception) {
                Log.e("TMapComposable", "환자 마커 애니메이션 업데이트 실패", e)
            }
        }
    }

    // 지도 초기화
    LaunchedEffect(Unit) {
        try {
            Log.d("TMapComposable", "🔄 지도 초기화 시작")

            val tmapView = TMapViewFactory.createTMapView(
                context = context,
                zoomLevel = zoomLevel
            )

            Log.d("TMapComposable", "🗺️ 지도 생성 완료")

            // 지도 엔진 초기화 대기
            delay(300)

            tmapView.setCompassMode(false)

            // 사용자 터치 시 팔로우 모드 OFF
            tmapView.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        isFollowMode = false
                        Log.d("TMapComposable", "📍 사용자 터치 → 팔로우 모드 OFF")
                    }
                }
                false
            }

            mapView = tmapView
            isMapInitialized = true
            isLoading = false

            onMapReady?.invoke(tmapView)
            Log.d("TMapComposable", "✅ 지도 초기화 완료")
        } catch (e: Exception) {
            Log.e("TMapComposable", "❌ 지도 초기화 실패", e)
            isLoading = false
        }
    }

    // 내 위치 버튼 클릭 시 위치로 이동
    LaunchedEffect(myLocationTrigger) {
        if (myLocationTrigger > 0) {
            val tmap = mapView

            if (userType == "GUARDIAN") {
                // 보호자: 선택된 환자의 위치로 이동
                val selectedId = selectedPatientId?.toLongOrNull()
                val selectedPatientLocation = if (selectedId != null) {
                    patientLocations[selectedId]
                } else {
                    null
                }

                if (tmap != null && selectedPatientLocation != null) {
                    try {
                        isFollowMode = true
                        tmap.setCenterPoint(selectedPatientLocation.latitude, selectedPatientLocation.longitude)
                        Log.d("TMapComposable", "🎯 선택된 환자 위치로 이동")
                    } catch (e: Exception) {
                        Log.e("TMapComposable", "❌ 환자 위치 이동 실패", e)
                    }
                }
            } else {
                // 환자: 자신의 위치로 이동
                val p = lastPoint
                if (tmap != null && p != null) {
                    try {
                        isFollowMode = true
                        tmap.setCenterPoint(p.latitude, p.longitude)
                        Log.d("TMapComposable", "🎯 내 위치로 이동")
                    } catch (e: Exception) {
                        Log.e("TMapComposable", "❌ 위치 이동 실패", e)
                    }
                }
            }
        }
    }

    // 북쪽 고정 버튼 클릭 시 북쪽으로 회전
    LaunchedEffect(northUpTrigger) {
        if (northUpTrigger > 0) {
            mapView?.let { tmap ->
                try {
                    withContext(Dispatchers.Main) {
                        tmap.setCompassMode(false)
                    }
                    Log.d("TMapComposable", "🧭 북쪽 고정")
                } catch (e: Exception) {
                    Log.e("TMapComposable", "❌ 북쪽 고정 실패", e)
                }
            }
        }
    }

    // 환자 위치 업데이트 (보호자용 - 선택된 환자만)
    LaunchedEffect(mapView, isMapInitialized, patientLocations, selectedPatientId, userType) {
        Log.d("TMapComposable", "환자 위치 업데이트 LaunchedEffect 실행: isMapInitialized=$isMapInitialized, userType=$userType, selectedPatientId=$selectedPatientId, patientLocations=${patientLocations.keys}")

        if (!isMapInitialized) {
            Log.d("TMapComposable", "지도 초기화 안됨")
            return@LaunchedEffect
        }
        if (userType != "GUARDIAN") {
            Log.d("TMapComposable", "보호자 아님: $userType")
            return@LaunchedEffect
        }
        val tmap = mapView ?: run {
            Log.d("TMapComposable", "mapView가 null")
            return@LaunchedEffect
        }
        val selectedId = selectedPatientId?.toLongOrNull()
        Log.d("TMapComposable", "선택된 환자 ID: $selectedId")

        try {
            // 선택된 환자의 위치만 가져오기
            val selectedPatientLocation = if (selectedId != null) {
                patientLocations[selectedId]
            } else {
                null
            }

            Log.d("TMapComposable", "선택된 환자 위치: $selectedPatientLocation")

            if (selectedPatientLocation != null) {
                // 선택된 환자의 마커가 없으면 생성
                if (patientMarker == null) {
                    val markerBitmap = pulseFrames[currentPulseFrame]
                    val marker = TMapMarkerItem().apply {
                        id = "selected_patient_marker"
                        tMapPoint = TMapPoint(selectedPatientLocation.latitude, selectedPatientLocation.longitude)
                        icon = markerBitmap
                    }

                    try {
                        tmap.addTMapMarkerItem(marker)
                        patientMarker = marker
                        // 첫 환자 위치로 지도 이동
                        tmap.setCenterPoint(selectedPatientLocation.latitude, selectedPatientLocation.longitude)
                        tmap.setZoomLevel(17)
                        Log.d("TMapComposable", "✅ 선택된 환자 마커 생성: patientId=$selectedId, lat=${selectedPatientLocation.latitude}, lon=${selectedPatientLocation.longitude}")
                    } catch (e: Exception) {
                        Log.e("TMapComposable", "선택된 환자 마커 추가 실패", e)
                    }
                } else {
                    // 기존 마커 위치 업데이트
                    try {
                        patientMarker?.tMapPoint = TMapPoint(selectedPatientLocation.latitude, selectedPatientLocation.longitude)
                        patientMarker?.let { tmap.updateTMapMarkerItem(it) }
                        Log.d("TMapComposable", "📍 선택된 환자 마커 업데이트: patientId=$selectedId, lat=${selectedPatientLocation.latitude}, lon=${selectedPatientLocation.longitude}")
                    } catch (e: Exception) {
                        Log.e("TMapComposable", "선택된 환자 마커 업데이트 실패", e)
                    }
                }
            } else {
                Log.d("TMapComposable", "선택된 환자 위치 정보 없음")
                // 선택된 환자가 없거나 위치 정보가 없으면 마커 제거
                patientMarker?.let { marker ->
                    try {
                        tmap.removeTMapMarkerItem(marker.id)
                        patientMarker = null
                        Log.d("TMapComposable", "🗑️ 선택된 환자 마커 삭제")
                    } catch (e: Exception) {
                        Log.e("TMapComposable", "선택된 환자 마커 삭제 실패", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TMapComposable", "❌ 선택된 환자 위치 업데이트 실패", e)
        }
    }

    // 위치 업데이트 수신 (환자만 자신의 위치 표시)
    LaunchedEffect(mapView, isMapInitialized, enableTracking, userType) {
        if (!isMapInitialized || !enableTracking) return@LaunchedEffect
        if (userType != "PATIENT") return@LaunchedEffect  // 환자만 자신의 위치 표시
        val tmap = mapView ?: return@LaunchedEffect
        val bus = locationBus ?: return@LaunchedEffect

        Log.d("TMapComposable", "📡 위치 업데이트 수신 시작 (환자)")
        bus.updates.collectLatest { point ->
            try {
                // 정확도 필터링
                if ((point.accuracyMeters ?: 999f) > 50f) {
                    Log.w("TMapComposable", "⚠️ 정확도 낮음 (${point.accuracyMeters}m)")
                    return@collectLatest
                }

                // 거리 필터링
                val prevPoint = lastPoint
                if (prevPoint != null) {
                    val distance = calculateDistance(
                        prevPoint.latitude, prevPoint.longitude,
                        point.latitude, point.longitude
                    )
                    if (distance < 2.0) {
                        return@collectLatest
                    }
                    Log.d("TMapComposable", "📍 이동 거리: ${String.format("%.1f", distance)}m")
                }

                lastPoint = point

                // 마커 생성 (최초 1회)
                if (locationMarker == null) {
                    val markerBitmap = pulseFrames[currentPulseFrame]
                    val marker = TMapMarkerItem().apply {
                        id = "location_marker"
                        tMapPoint = TMapPoint(point.latitude, point.longitude)
                        icon = markerBitmap
                    }

                    try {
                        tmap.addTMapMarkerItem(marker)
                        Log.d("TMapComposable", "✅ 마커 생성 완료")
                    } catch (e: Exception) {
                        Log.e("TMapComposable", "마커 추가 실패", e)
                    }

                    locationMarker = marker

                    // 첫 위치로 지도 이동
                    tmap.setCenterPoint(point.latitude, point.longitude)
                    tmap.setZoomLevel(17)
                } else {
                    // 마커 위치 업데이트
                    try {
                        locationMarker?.tMapPoint = TMapPoint(point.latitude, point.longitude)
                        locationMarker?.let { tmap.updateTMapMarkerItem(it) }
                    } catch (e: Exception) {
                        Log.e("TMapComposable", "마커 위치 업데이트 실패", e)
                    }
                }

                // 팔로우 모드일 때 지도 중심 이동
                if (isFollowMode) {
                    tmap.setCenterPoint(point.latitude, point.longitude)
                }
            } catch (e: Exception) {
                Log.e("TMapComposable", "❌ 위치 업데이트 실패", e)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            mapView?.let { tmapView ->
                AndroidView(
                    factory = { tmapView },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mapView?.onDestroy()
                Log.d("TMapComposable", "🗑️ TMapView destroyed")
            } catch (e: Exception) {
                Log.e("TMapComposable", "❌ TMapView destroy 실패", e)
            } finally {
                mapView = null
            }
        }
    }
}

/**
 * 펄스 애니메이션 프레임들을 미리 생성 (재사용)
 */
private fun createPulseFrames(
    context: android.content.Context,
    frameCount: Int = 8,
    color: String = "#5C7165"
): List<Bitmap> {
    val frames = mutableListOf<Bitmap>()
    val markerSizeDp = 24
    val pulseSizeDp = 48 // 펄스 효과를 포함한 전체 크기
    val sizePx = (pulseSizeDp * context.resources.displayMetrics.density).toInt()
    val markerRadius = (markerSizeDp * context.resources.displayMetrics.density / 2f)

    for (i in 0 until frameCount) {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centerX = sizePx / 2f
        val centerY = sizePx / 2f

        // 펄스 진행도 (0.0 ~ 1.0)
        val progress = i.toFloat() / frameCount

        // 펄스 원 그리기 (점점 커지면서 투명해짐)
        val pulseRadius = markerRadius + (markerRadius * 0.8f * progress)
        val fadeProgress = if (progress < 0.7f) {
            progress / 0.7f * 0.5f
        } else {
            0.5f + ((progress - 0.7f) / 0.3f * 0.5f)
        }
        val pulseAlpha = (180 * (1f - fadeProgress)).toInt()
        val pulsePaint = Paint().apply {
            this.color = Color.parseColor(color)
            alpha = pulseAlpha
            style = Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, pulseRadius, pulsePaint)

        // 메인 마커 (그림자 포함)
        val shadowPaint = Paint().apply {
            this.color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(6f, 0f, 0f, Color.argb(150, 0, 0, 0))
        }
        canvas.drawCircle(centerX, centerY, markerRadius, shadowPaint)

        // 마커 내부
        val fillPaint = Paint().apply {
            this.color = Color.parseColor(color)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, markerRadius - 3f, fillPaint)

        frames.add(bitmap)
    }

    return frames
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
