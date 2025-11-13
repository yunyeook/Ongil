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
import androidx.compose.ui.graphics.toArgb
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapCircle
import com.skt.tmap.overlay.TMapMarkerItem
import com.skt.tmap.overlay.TMapPolyLine
import kr.co.ongil.common.location.LocationPoint
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.domain.model.Route
import kr.co.ongil.common.location.SafetyZoneMonitor
import kr.co.ongil.data.model.location.Coordinate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kr.co.ongil.presentation.ui.map.SafetyZoneConfig.CircleColors
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context as AndroidContext


/**
 * TMap을 표시하는 Composable 컴포넌트
 * - 현재 위치 기반으로 지도 초기화
 * - 위치 추적 및 마커 표시 (펄스 애니메이션 포함)
 * - 길안내 경로 표시
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
    route: Route? = null,  // 길안내 경로
    isNavigationMode: Boolean = false,  // 네비게이션 모드 (1인칭 시점)
    userType: String = "",
    selectedPatientId: String? = null,
    patientLocations: Map<Long, Coordinate> = emptyMap(),  // 환자 위치 (보호자용)
    showSafetyZones: Boolean = false,  // 안전 범위 표시 여부
    level1Distance: Int = SafetyZoneMonitor.DEFAULT_STAGE_1_RADIUS,
    level2Distance: Int = SafetyZoneMonitor.DEFAULT_STAGE_2_RADIUS,
    level3Distance: Int = SafetyZoneMonitor.DEFAULT_STAGE_3_RADIUS
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<TMapView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isMapInitialized by remember { mutableStateOf(false) }
    var lastPoint by remember { mutableStateOf<LocationPoint?>(null) }
    var locationMarker by remember { mutableStateOf<TMapMarkerItem?>(null) }
    var isFollowMode by remember { mutableStateOf(true) }
    var currentPulseFrame by remember { mutableStateOf(0) }
    var routePolyLine by remember { mutableStateOf<TMapPolyLine?>(null) }
    var currentBearing by remember { mutableStateOf(0f) }  // 현재 방위각
    var routeBearing by remember { mutableStateOf(0f) }  // 경로의 초기 방향

    // 환자 마커 (보호자는 선택된 환자 1명만)
    var patientMarker by remember { mutableStateOf<TMapMarkerItem?>(null) }

    // 안전 범위 동심원
    var safetyCircles by remember { mutableStateOf<List<String>>(emptyList()) }

    // 홈 위치 마커 (안전 범위 기준점)
    var homeMarker by remember { mutableStateOf<TMapMarkerItem?>(null) }

    // 펄스 애니메이션 프레임 생성 (녹색)
    val pulseFrames = remember {
        createPulseFrames(context, color = "#5C7165")
    }

    // 방위각 센서 (네비게이션 모드용)
    DisposableEffect(isNavigationMode) {
        val sensorManager = if (isNavigationMode) {
            context.getSystemService(AndroidContext.SENSOR_SERVICE) as? SensorManager
        } else null

        val sensorListener: SensorEventListener? = if (isNavigationMode && sensorManager != null) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return

                    // 회전 벡터를 회전 행렬로 변환
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    // 회전 행렬에서 방위각 추출
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    // 방위각 (라디안 -> 도)
                    val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    currentBearing = (azimuth + 360) % 360  // 0-360 범위로 정규화
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // 정확도 변경 무시
                }
            }
        } else null

        sensorListener?.let { listener ->
            val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            sensor?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
                Log.d("TMapComposable", "🧭 방위각 센서 시작")
            }
        }

        onDispose {
            sensorListener?.let { listener ->
                sensorManager?.unregisterListener(listener)
                Log.d("TMapComposable", "🧭 방위각 센서 정지")
            }
        }
    }

    // 네비게이션 모드: 지도 회전 (경로 방향 기준) - 2D 평면 회전만
    LaunchedEffect(isNavigationMode, isMapInitialized) {
        if (!isNavigationMode || !isMapInitialized) return@LaunchedEffect
        val tmap = mapView ?: return@LaunchedEffect

        while (isActive) {
            try {
                withContext(Dispatchers.Main) {
                    // 경로 방향 대비 상대 회전 계산
                    val relativeRotation = (currentBearing - routeBearing + 360) % 360

                    // 2D 평면 회전만 적용 (bearing 사용)
                    val currentCenter = tmap.centerPoint
                    if (currentCenter != null) {
                        // 3번째 파라미터에 bearing 각도 전달하여 2D 회전
                        trySetCenterWithBearing(tmap, currentCenter.latitude, currentCenter.longitude, -relativeRotation)
                    }
                }
                delay(100)  // 100ms마다 업데이트
            } catch (e: Exception) {
                Log.e("TMapComposable", "지도 회전 실패", e)
                break
            }
        }
    }

    // 네비게이션 모드 종료 시 지도 복구
    LaunchedEffect(isNavigationMode, isMapInitialized) {
        if (!isMapInitialized) return@LaunchedEffect
        if (isNavigationMode) return@LaunchedEffect  // 네비게이션 모드 활성화 시에는 아무것도 안함

        val tmap = mapView ?: return@LaunchedEffect

        // 네비게이션 모드가 종료되면 회전 비활성화
        withContext(Dispatchers.Main) {
            try {
                tmap.setCompassMode(false)
                Log.d("TMapComposable", "🔄 네비게이션 모드 종료 - 회전 비활성화")
            } catch (e: Exception) {
                Log.e("TMapComposable", "지도 복구 실패", e)
            }
        }
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
                        val currentCenter = tmap.centerPoint
                        if (currentCenter != null) {
                            tmap.setCenterPoint(currentCenter.latitude, currentCenter.longitude, true)
                        }
                        tmap.setCompassMode(false)
                    }
                    Log.d("TMapComposable", "🧭 북쪽 고정 완료")
                } catch (e: Exception) {
                    Log.e("TMapComposable", "❌ 북쪽 고정 실패", e)
                }
            }
        }
    }

    // 안전 범위 동심원 표시/숨김 (PolyLine 사용)
    LaunchedEffect(mapView, isMapInitialized, showSafetyZones, level1Distance, level2Distance, level3Distance) {
        if (!isMapInitialized) return@LaunchedEffect
        val tmap = mapView ?: return@LaunchedEffect

        try {
            withContext(Dispatchers.Main) {
                if (showSafetyZones) {
                    // 기존 폴리라인 제거
                    safetyCircles.forEach { id ->
                        try {
                            tmap.removeTMapPolyLine(id)
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "폴리라인 제거 실패: $id", e)
                        }
                    }

                    // 홈 위치로 지도 이동 (동심원이 보이도록)
                    tmap.setCenterPoint(
                        SafetyZoneConfig.HomeLocation.LATITUDE,
                        SafetyZoneConfig.HomeLocation.LONGITUDE
                    )
                    tmap.setZoomLevel(14)
                    Log.d("TMapComposable", "🏠 홈 위치로 이동: ${SafetyZoneConfig.HomeLocation.LATITUDE}, ${SafetyZoneConfig.HomeLocation.LONGITUDE}")

                    val circles = mutableListOf<String>()

                    // 3단계 - 빨간색
                    val points3 = createCirclePoints(
                        SafetyZoneConfig.HomeLocation.LATITUDE,
                        SafetyZoneConfig.HomeLocation.LONGITUDE,
                        level3Distance
                    )
                    val poly3 = TMapPolyLine("safety_zone_stage3", points3)
                    poly3.setLineColor(CircleColors.stage3StrokeColor.toArgb())
                    poly3.setLineWidth(0.5f)
                    poly3.setOutLineColor(0xFFD40806.toInt())  // 빨간색 외곽선
                    tmap.addTMapPolyLine(poly3)
                    circles.add("safety_zone_stage3")
                    Log.d("TMapComposable", "✅ 3단계 동심원 추가 (${level3Distance}m, ${points3.size}개 점)")

                    // 2단계 - 주황색
                    val points2 = createCirclePoints(
                        SafetyZoneConfig.HomeLocation.LATITUDE,
                        SafetyZoneConfig.HomeLocation.LONGITUDE,
                        level2Distance
                    )
                    val poly2 = TMapPolyLine("safety_zone_stage2", points2)
                    poly2.setLineColor(CircleColors.stage2StrokeColor.toArgb())
                    poly2.setLineWidth(0.5f)
                    poly2.setOutLineColor(0xFF007BFF.toInt())  // 파란색 외곽선
                    tmap.addTMapPolyLine(poly2)
                    circles.add("safety_zone_stage2")
                    Log.d("TMapComposable", "✅ 2단계 동심원 추가 (${level2Distance}m, ${points2.size}개 점)")

                    // 1단계 - 초록색
                    val points1 = createCirclePoints(
                        SafetyZoneConfig.HomeLocation.LATITUDE,
                        SafetyZoneConfig.HomeLocation.LONGITUDE,
                        level1Distance
                    )
                    val poly1 = TMapPolyLine("safety_zone_stage1", points1)
                    poly1.setLineColor(CircleColors.stage1StrokeColor.toArgb())
                    poly1.setLineWidth(0.5f)
                    poly1.setOutLineColor(0xFF10C00A.toInt())  // 초록색 외곽선
                    tmap.addTMapPolyLine(poly1)
                    circles.add("safety_zone_stage1")
                    Log.d("TMapComposable", "✅ 1단계 동심원 추가 (${level1Distance}m, ${points1.size}개 점)")



                    safetyCircles = circles
                    Log.d("TMapComposable", "✅ 안전 범위 동심원 표시 완료 (총 ${circles.size}개)")
                } else {
                    // 폴리라인 숨김
                    safetyCircles.forEach { id ->
                        try {
                            tmap.removeTMapPolyLine(id)
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "폴리라인 제거 실패: $id", e)
                        }
                    }
                    safetyCircles = emptyList()


                    Log.d("TMapComposable", "🗑️ 안전 범위 동심원 숨김 완료")
                }
            }
        } catch (e: Exception) {
            Log.e("TMapComposable", "❌ 안전 범위 동심원 처리 실패", e)
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

                // 팔로우 모드일 때만 지도 중심 이동
                if (isFollowMode) {
                    tmap.setCenterPoint(point.latitude, point.longitude)
                }
            } catch (e: Exception) {
                Log.e("TMapComposable", "❌ 위치 업데이트 실패", e)
            }
        }
    }

    // 길안내 경로 그리기
    LaunchedEffect(mapView, isMapInitialized, route) {
        Log.d("TMapComposable", "경로 그리기 LaunchedEffect 트리거 - route: ${route != null}")
        if (!isMapInitialized) {
            Log.d("TMapComposable", "지도가 초기화되지 않음")
            return@LaunchedEffect
        }
        val tmap = mapView ?: return@LaunchedEffect

        withContext(Dispatchers.Main) {
            try {
                // 기존 경로 제거
                routePolyLine?.let { oldPolyLine ->
                    try {
                        tmap.removeTMapPolyLine(oldPolyLine.id)
                    } catch (e: Exception) {
                        Log.w("TMapComposable", "기존 경로 제거 실패", e)
                    }
                }

                if (route != null) {
                    Log.d("TMapComposable", "🛣️ 경로 그리기 시작: ${route.path.size}개 포인트")

                    // 경로 좌표를 TMapPoint 리스트로 변환
                    val pointList = ArrayList<TMapPoint>()
                    route.path.forEach { pathPoint ->
                        pointList.add(TMapPoint(pathPoint.latitude, pathPoint.longitude))
                    }

                    // TMapPolyLine 생성 (ID와 pointList를 생성자에 전달)
                    val polyLine = TMapPolyLine("route_line", pointList)
                    polyLine.lineWidth = 10f
                    polyLine.lineColor = Color.parseColor("#5C7165")
                    polyLine.outLineWidth = 2f
                    polyLine.outLineColor = Color.WHITE

                    Log.d("TMapComposable", "PolyLine ID: ${polyLine.id}")
                    Log.d("TMapComposable", "PolyLine 좌표 개수: ${pointList.size}")
                    Log.d("TMapComposable", "첫 좌표: (${pointList.first().latitude}, ${pointList.first().longitude})")
                    Log.d("TMapComposable", "마지막 좌표: (${pointList.last().latitude}, ${pointList.last().longitude})")

                    // 지도에 추가
                    try {
                        tmap.addTMapPolyLine(polyLine)
                        Log.d("TMapComposable", "✅ PolyLine 추가 성공")
                    } catch (e: Exception) {
                        Log.e("TMapComposable", "❌ PolyLine 추가 실패", e)
                    }
                    routePolyLine = polyLine

                    // 출발지 마커 추가 (경로의 첫 좌표)
                    if (route.path.isNotEmpty()) {
                        val startPoint = route.path.first()
                        val startMarker = TMapMarkerItem().apply {
                            id = "start_marker"
                            tMapPoint = TMapPoint(startPoint.latitude, startPoint.longitude)
                            icon = createMarkerBitmap(context, "출발", Color.parseColor("#4CAF50"))
                        }
                        try {
                            tmap.addTMapMarkerItem(startMarker)
                            Log.d("TMapComposable", "✅ 출발지 마커 추가 성공")
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "❌ 출발지 마커 추가 실패", e)
                        }
                    }

                    // 도착지 마커 추가 (경로의 마지막 좌표)
                    if (route.path.isNotEmpty()) {
                        val endPoint = route.path.last()
                        val endMarker = TMapMarkerItem().apply {
                            id = "end_marker"
                            tMapPoint = TMapPoint(endPoint.latitude, endPoint.longitude)
                            icon = createMarkerBitmap(context, "도착", Color.parseColor("#F44336"))
                        }
                        try {
                            tmap.addTMapMarkerItem(endMarker)
                            Log.d("TMapComposable", "✅ 도착지 마커 추가 성공")
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "❌ 도착지 마커 추가 실패", e)
                        }
                    }

                    // 경로의 초기 방향 계산 (첫 2개 점)
                    if (route.path.size >= 2) {
                        val start = route.path[0]
                        val next = route.path[1]
                        routeBearing = calculateBearing(start.latitude, start.longitude, next.latitude, next.longitude)
                        Log.d("TMapComposable", "📐 경로 초기 방향: ${routeBearing}°")
                    } else {
                        routeBearing = 0f
                    }

                    Log.d("TMapComposable", "✅ 경로 그리기 완료")
                } else {
                    // 경로가 null이면 마커도 제거
                    routePolyLine = null
                    routeBearing = 0f  // 경로 방향 리셋
                    tmap.removeTMapMarkerItem("start_marker")
                    tmap.removeTMapMarkerItem("end_marker")
                    Log.d("TMapComposable", "🗑️ 경로 제거")
                }
            } catch (e: Exception) {
                Log.e("TMapComposable", "❌ 경로 그리기 실패", e)
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

/**
 * 두 지점 간 방위각 계산 (베어링, 북쪽 기준 시계방향 각도)
 */
private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val dLon = Math.toRadians(lon2 - lon1)
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)

    val y = sin(dLon) * cos(lat2Rad)
    val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

    val bearing = Math.toDegrees(atan2(y, x))
    return ((bearing + 360) % 360).toFloat()  // 0-360 범위로 정규화
}

/**
 * 원형 폴리라인 생성 (반경을 미터 단위로)
 */
private fun createCirclePoints(centerLat: Double, centerLon: Double, radiusMeters: Int, points: Int = 72): ArrayList<TMapPoint> {
    val circlePoints = ArrayList<TMapPoint>()
    val earthRadius = 6371000.0 // 지구 반경 (미터)

    for (i in 0..points) {
        val angle = Math.toRadians((i * 360.0 / points))

        // 위도 변화량
        val deltaLat = radiusMeters / earthRadius * cos(angle)
        // 경도 변화량 (위도에 따라 조정)
        val deltaLon = radiusMeters / earthRadius * sin(angle) / cos(Math.toRadians(centerLat))

        val lat = centerLat + Math.toDegrees(deltaLat)
        val lon = centerLon + Math.toDegrees(deltaLon)

        circlePoints.add(TMapPoint(lat, lon))
    }

    return circlePoints
}

/**
 * 출발/도착 마커 비트맵 생성
 */
private fun createMarkerBitmap(
    context: android.content.Context,
    text: String,
    backgroundColor: Int
): Bitmap {
    val sizeDp = 40
    val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centerX = sizePx / 2f
    val centerY = sizePx / 2f

    // 배경 원
    val bgPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, centerY, sizePx / 2f - 4f, bgPaint)

    // 흰색 테두리
    val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, centerY, sizePx / 2f - 4f, borderPaint)

    // 텍스트
    val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 14 * context.resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(text, centerX, textY, textPaint)

    return bitmap
}

/**
 * TMap SDK 버전 독립적으로 지도 회전 설정 (2D 평면만)
 */
private fun setMapRotation(tmap: TMapView, angle: Float) {
    try {
        // 나침반 모드를 사용하여 2D 회전
        tmap.setCompassMode(true)

        // 2D 평면 회전 메서드 시도
        val methods = listOf("setMapRotationAngle", "setMapBearing", "setBearing")
        for (methodName in methods) {
            if (tryInvokeRotation(tmap, methodName, angle)) {
                return
            }
        }
    } catch (_: Exception) {
        // 조용히 실패
    }
}

/**
 * 리플렉션으로 회전 메서드 호출 시도
 */
private fun tryInvokeRotation(tmap: TMapView, methodName: String, angle: Float): Boolean {
    return try {
        val method = TMapView::class.java.getMethod(methodName, Float::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(tmap, angle)
        true
    } catch (_: Throwable) {
        false
    }
}

/**
 * 2D 평면 회전: bearing을 포함한 setCenterPoint 호출
 */
private fun trySetCenterWithBearing(tmap: TMapView, lat: Double, lon: Double, bearing: Float) {
    try {
        // setCenterPoint(double lat, double lon, float bearing)
        val method = TMapView::class.java.getMethod(
            "setCenterPoint",
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Float::class.javaPrimitiveType
        )
        method.isAccessible = true
        method.invoke(tmap, lat, lon, bearing)
    } catch (e: NoSuchMethodException) {
        // bearing 파라미터가 없는 SDK 버전 - 기본 회전 메서드 사용
        setMapRotation(tmap, bearing)
    } catch (_: Throwable) {
        // 실패 시 조용히 무시
    }
}
