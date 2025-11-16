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
import com.skt.tmap.overlay.TMapPolygon
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
    level3Distance: Int = SafetyZoneMonitor.DEFAULT_STAGE_3_RADIUS,
    defaultDestinationCoordinate: Pair<Double, Double>? = null,  // 기본 목적지 좌표 (안전범위 기준점)
    placeLocationTrigger: Pair<Int, Pair<Double, Double>> = 0 to Pair(0.0, 0.0),  // 장소 위치로 이동 트리거
    disableFollowMode: Boolean = false,  // 팔로우 모드 강제 비활성화 (장소 상세 정보 표시 중)
    isHomeScreen: Boolean = false  // 홈 화면 여부
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

    // 장소 마커 (장소 상세 정보 표시 중)
    var placeMarker by remember { mutableStateOf<TMapMarkerItem?>(null) }

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
    LaunchedEffect(locationMarker, isMapInitialized) {
        if (userType != "PATIENT") return@LaunchedEffect  // 환자만 자신의 위치 표시
        if (!isMapInitialized) return@LaunchedEffect  // 지도 초기화 완료 후 시작
        val marker = locationMarker ?: return@LaunchedEffect
        val tmap = mapView ?: return@LaunchedEffect

        Log.d("TMapComposable", "🎬 펄스 애니메이션 시작 (환자) - marker: ${marker.id}")

        while (isActive) {  // isActive 체크 추가
            delay(100) // 100ms마다 프레임 변경 (초당 10프레임)
            currentPulseFrame = (currentPulseFrame + 1) % pulseFrames.size

            // 마커와 맵이 유효한지 확인
            if (locationMarker == null || mapView == null) {
                Log.d("TMapComposable", "마커 또는 맵 제거됨 - 펄스 종료")
                break
            }

            // 마커 ID 유효성 체크 - null이면 즉시 종료 (재시도 안 함)
            if (marker.id == null) {
                Log.d("TMapComposable", "마커 ID가 null - 펄스 종료")
                break
            }

            try {
                // 아이콘만 변경하고 업데이트
                marker.icon = pulseFrames[currentPulseFrame]
                tmap.updateTMapMarkerItem(marker)
            } catch (e: NullPointerException) {
                // NPE 발생 시 즉시 종료 (마커 무효화)
                Log.d("TMapComposable", "마커 무효화됨 (NPE) - 펄스 종료")
                break
            } catch (e: Exception) {
                // 기타 예외도 즉시 종료
                Log.d("TMapComposable", "펄스 업데이트 실패 - 펄스 종료: ${e.message}")
                break
            }
        }
    }

    // 펄스 애니메이션 루프 (보호자용 - 선택된 환자의 위치)
    LaunchedEffect(patientMarker, mapView, isMapInitialized) {
        if (userType != "GUARDIAN") return@LaunchedEffect  // 보호자만 환자 위치 표시
        if (!isMapInitialized) return@LaunchedEffect  // 지도 초기화 완료 후 시작
        val marker = patientMarker ?: return@LaunchedEffect
        val tmap = mapView ?: return@LaunchedEffect

        Log.d("TMapComposable", "🎬 펄스 애니메이션 시작 (보호자)")

        while (isActive) {  // isActive 체크 추가
            delay(100) // 100ms마다 프레임 변경 (초당 10프레임)
            currentPulseFrame = (currentPulseFrame + 1) % pulseFrames.size

            // 마커와 맵이 유효한지 확인
            if (patientMarker == null || mapView == null) {
                Log.d("TMapComposable", "환자 마커 또는 맵 제거됨 - 펄스 종료")
                break
            }

            // 마커 ID 유효성 체크 - null이면 즉시 종료 (재시도 안 함)
            if (marker.id == null) {
                Log.d("TMapComposable", "환자 마커 ID가 null - 펄스 종료")
                break
            }

            try {
                // 아이콘만 변경하고 업데이트
                marker.icon = pulseFrames[currentPulseFrame]
                tmap.updateTMapMarkerItem(marker)
            } catch (e: NullPointerException) {
                // NPE 발생 시 즉시 종료 (마커 무효화)
                Log.d("TMapComposable", "환자 마커 무효화됨 (NPE) - 펄스 종료")
                break
            } catch (e: Exception) {
                // 기타 예외도 즉시 종료
                Log.d("TMapComposable", "환자 마커 펄스 업데이트 실패 - 펄스 종료: ${e.message}")
                break
            }
        }
    }

    // 컴포넌트 시작 시 로그 및 상태 초기화
    LaunchedEffect(Unit) {
        Log.d("TMapComposable", "📍 TMapComposable 시작 - enableTracking=$enableTracking, userType=$userType, selectedPatientId=$selectedPatientId, patientLocations=${patientLocations.keys}")
        // 기존 마커 상태 초기화 (TMapViewFactory에서 마커 제거했으므로)
        locationMarker = null
        patientMarker = null
        Log.d("TMapComposable", "🔄 마커 상태 초기화")
    }

    // 환자 위치 변경 로그
    LaunchedEffect(patientLocations) {
        Log.d("TMapComposable", "📍 환자 위치 변경됨: ${patientLocations.keys}, userType=$userType")
    }

    // 지도 초기화
    LaunchedEffect(Unit) {
        try {
            Log.d("TMapComposable", "🔄 지도 초기화 시작 (isHomeScreen=$isHomeScreen)")

            val (tmapView, isCached) = TMapViewFactory.createTMapView(
                context = context,
                zoomLevel = zoomLevel,
                isHomeScreen = isHomeScreen
            )

            Log.d("TMapComposable", "🗺️ 지도 생성 완료 (캐싱됨: $isCached)")

            if (isCached) {
                // 캐싱된 TMapView는 이미 초기화 완료 상태 - 즉시 사용 가능
                Log.d("TMapComposable", "✅ TMapView 엔진 준비 완료 (캐싱됨: 즉시 설정)")
                isMapInitialized = true
                onMapReady?.invoke(tmapView)
            } else {
                // 새로 생성된 TMapView - OnMapReadyListener 대기
                tmapView.setOnMapReadyListener {
                    Log.d("TMapComposable", "✅ TMapView 엔진 준비 완료 (새로 생성됨)")
                    isMapInitialized = true
                    onMapReady?.invoke(tmapView)
                }
            }

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
            isLoading = false

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

    // 장소 위치로 지도 이동 및 마커 표시
    LaunchedEffect(placeLocationTrigger.first) {
        if (placeLocationTrigger.first > 0) {
            val tmap = mapView
            val (lat, lon) = placeLocationTrigger.second

            if (tmap != null && lat != 0.0 && lon != 0.0) {
                try {
                    withContext(Dispatchers.Main) {
                        // 기존 장소 마커 제거
                        placeMarker?.let { marker ->
                            try {
                                tmap.removeTMapMarkerItem(marker.id)
                            } catch (e: Exception) {
                                Log.e("TMapComposable", "기존 장소 마커 제거 실패", e)
                            }
                        }

                        // 장소 마커 생성
                        val marker = TMapMarkerItem().apply {
                            id = "place_marker"
                            tMapPoint = TMapPoint(lat, lon)
                            icon = createPlaceMarkerBitmap(context, Color.parseColor("#FF9800"))
                        }

                        try {
                            tmap.addTMapMarkerItem(marker)
                            placeMarker = marker
                            Log.d("TMapComposable", "✅ 장소 마커 추가 성공: lat=$lat, lon=$lon")
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "❌ 장소 마커 추가 실패", e)
                        }

                        // 지도 이동
                        tmap.setCenterPoint(lat, lon)
                        tmap.setZoomLevel(17)
                        Log.d("TMapComposable", "📍 장소 위치로 이동: lat=$lat, lon=$lon")
                    }
                } catch (e: Exception) {
                    Log.e("TMapComposable", "❌ 장소 위치 이동 실패", e)
                }
            } else if (tmap != null && lat == 0.0 && lon == 0.0) {
                // 장소 상세 정보가 닫혔을 때 마커 제거
                try {
                    withContext(Dispatchers.Main) {
                        placeMarker?.let { marker ->
                            try {
                                tmap.removeTMapMarkerItem(marker.id)
                                placeMarker = null
                                Log.d("TMapComposable", "🗑️ 장소 마커 제거")
                            } catch (e: Exception) {
                                Log.e("TMapComposable", "장소 마커 제거 실패", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TMapComposable", "❌ 장소 마커 제거 실패", e)
                }
            }
        }
    }

    // disableFollowMode 변경 감지
    LaunchedEffect(disableFollowMode) {
        if (disableFollowMode) {
            isFollowMode = false
            Log.d("TMapComposable", "🚫 팔로우 모드 비활성화 (장소 상세 정보 표시 중)")
        } else {
            // disableFollowMode가 false로 변경되면 팔로우 모드 활성화
            isFollowMode = true
            Log.d("TMapComposable", "✅ 팔로우 모드 활성화 (장소 상세 정보 닫힘)")
        }
    }

    // 안전 범위 동심원 표시/숨김 (Polygon 사용 - 채워진 영역)
    LaunchedEffect(mapView, isMapInitialized, showSafetyZones, level1Distance, level2Distance, level3Distance, defaultDestinationCoordinate) {
        Log.d("TMapComposable", "🔄 안전범위 LaunchedEffect 실행 - showSafetyZones=$showSafetyZones, isHomeScreen=$isHomeScreen, defaultDestination=$defaultDestinationCoordinate")
        if (!isMapInitialized) return@LaunchedEffect
        val tmap = mapView ?: return@LaunchedEffect

        try {
            withContext(Dispatchers.Main) {
                if (showSafetyZones) {
                    // 기본 목적지가 설정되지 않은 경우 안전범위 표시 안함
                    if (defaultDestinationCoordinate == null) {
                        Log.w("TMapComposable", "⚠️ 기본 목적지가 설정되지 않아 안전범위를 표시할 수 없습니다")
                        return@withContext
                    }

                    val (centerLat, centerLon) = defaultDestinationCoordinate
                    Log.d("TMapComposable", "✅ 기본 목적지 기준 안전범위 표시: ($centerLat, $centerLon)")

                    // 기존 폴리곤 제거
                    safetyCircles.forEach { id ->
                        try {
                            tmap.removeTMapPolygon(id)
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "폴리곤 제거 실패: $id", e)
                        }
                    }

                    val polygons = mutableListOf<String>()

                    // 각 영역을 겹치지 않는 링(donut) 형태로 생성
                    // 1단계 - 초록색 (level1Distance ~ level2Distance 사이의 링)
                    val outerPoints1 = createCirclePoints(
                        centerLat,
                        centerLon,
                        level2Distance,
                        72
                    )
                    val innerPoints1 = createCirclePoints(
                        centerLat,
                        centerLon,
                        level1Distance,
                        72
                    )
                    val pointList1 = ArrayList<TMapPoint>().apply {
                        addAll(outerPoints1)
                        addAll(innerPoints1.reversed())
                    }
                    val polygon1 = TMapPolygon("safety_zone_stage1", pointList1)
                    polygon1.setAreaColor(Color.rgb(16, 192, 10))  // 초록색
                    polygon1.setAreaAlpha(32)
                    polygon1.setLineColor(Color.TRANSPARENT)
                    polygon1.setPolygonWidth(0f)
                    tmap.addTMapPolygon(polygon1)
                    polygons.add("safety_zone_stage1")
                    Log.d("TMapComposable", "✅ 1단계 안전 범위 추가 (${level1Distance}m ~ ${level2Distance}m)")

                    // 2단계 - 파란색 (level2Distance ~ level3Distance 사이의 링)
                    val outerPoints2 = createCirclePoints(
                        centerLat,
                        centerLon,
                        level3Distance,
                        72
                    )
                    val innerPoints2 = createCirclePoints(
                        centerLat,
                        centerLon,
                        level2Distance,
                        72
                    )
                    val pointList2 = ArrayList<TMapPoint>().apply {
                        addAll(outerPoints2)
                        addAll(innerPoints2.reversed())
                    }
                    val polygon2 = TMapPolygon("safety_zone_stage2", pointList2)
                    polygon2.setAreaColor(Color.rgb(0, 123, 255))  // 파란색
                    polygon2.setAreaAlpha(32)
                    polygon2.setLineColor(Color.TRANSPARENT)
                    polygon2.setPolygonWidth(0f)
                    tmap.addTMapPolygon(polygon2)
                    polygons.add("safety_zone_stage2")
                    Log.d("TMapComposable", "✅ 2단계 안전 범위 추가 (${level2Distance}m ~ ${level3Distance}m)")

                    // 3단계 - 빨간색 (level3Distance ~ 50km 사이의 링)
                    val maxRadius = 50000 // 50km
                    val outerPoints3 = createCirclePoints(
                        centerLat,
                        centerLon,
                        maxRadius,
                        721
                    )
                    val innerPoints3 = createCirclePoints(
                        centerLat,
                        centerLon,
                        level3Distance,
                        72
                    )
                    val pointList3 = ArrayList<TMapPoint>().apply {
                        addAll(outerPoints3)
                        addAll(innerPoints3.reversed())
                    }
                    val polygon3 = TMapPolygon("safety_zone_stage3", pointList3)
                    polygon3.setAreaColor(Color.rgb(212, 8, 6))  // 빨간색
                    polygon3.setAreaAlpha(32)
                    polygon3.setLineColor(Color.TRANSPARENT)
                    polygon3.setPolygonWidth(0f)
                    tmap.addTMapPolygon(polygon3)
                    polygons.add("safety_zone_stage3")
                    Log.d("TMapComposable", "✅ 3단계 안전 범위 추가 (${level3Distance}m ~ ${maxRadius}m)")

                    safetyCircles = polygons

                    Log.d("TMapComposable", "✅ 안전 범위 동심원 표시 완료 (총 ${polygons.size}개)")

                    // 안전 범위 추가 후 마커를 폴리곤 위로 재추가
                    if (userType == "PATIENT") {
                        locationMarker?.let { marker ->
                            try {
                                tmap.removeTMapMarkerItem(marker.id)
                                tmap.addTMapMarkerItem(marker)
                                Log.d("TMapComposable", "✅ 현재위치 마커 재추가 (폴리곤 위)")
                            } catch (e: Exception) {
                                Log.e("TMapComposable", "현재위치 마커 재추가 실패", e)
                            }
                        }
                    } else if (userType == "GUARDIAN") {
                        patientMarker?.let { marker ->
                            try {
                                tmap.removeTMapMarkerItem(marker.id)
                                tmap.addTMapMarkerItem(marker)
                                Log.d("TMapComposable", "✅ 환자 마커 재추가 (폴리곤 위)")
                            } catch (e: Exception) {
                                Log.e("TMapComposable", "환자 마커 재추가 실패", e)
                            }
                        }
                    }
                } else {
                    // 폴리곤 숨김
                    safetyCircles.forEach { id ->
                        try {
                            tmap.removeTMapPolygon(id)
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "폴리곤 제거 실패: $id", e)
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
//        Log.d("TMapComposable", "환자 위치 업데이트 LaunchedEffect 실행: isMapInitialized=$isMapInitialized, userType=$userType, selectedPatientId=$selectedPatientId, patientLocations=${patientLocations.keys}")

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
                val accuracy = point.accuracyMeters ?: 999f
                val speed = point.speedMps ?: 0f

                // 정확도 필터링 (40m 초과는 무시)
                if (accuracy > 40f) {
                    Log.w("TMapComposable", "⚠️ 정확도 낮음 (${accuracy}m) - 무시")
                    return@collectLatest
                }

                // 거리 및 속도 기반 필터링
                val prevPoint = lastPoint
                if (prevPoint != null) {
                    val distance = calculateDistance(
                        prevPoint.latitude, prevPoint.longitude,
                        point.latitude, point.longitude
                    )

                    // 시간 차이 계산 (초 단위)
                    val timeDiffSeconds = (point.timeMillis - prevPoint.timeMillis) / 1000.0

                    // 속도와 거리 일관성 체크
                    if (speed > 1.0f && timeDiffSeconds > 0) {
                        // 예상 이동 거리 (속도 * 시간)
                        val expectedDistance = speed * timeDiffSeconds
                        // 실제 이동 거리와 예상 이동 거리의 차이
                        val distanceDiff = Math.abs(expectedDistance - distance)

                        // 차이가 너무 크면 GPS 오류로 판단 (예상의 80% 이상 차이) - 기준 완화
                        if (distanceDiff > expectedDistance * 0.8) {
                            Log.w("TMapComposable", "⚠️ 속도-거리 불일치 감지 (속도: ${String.format("%.1f", speed)}m/s, 실제: ${String.format("%.1f", distance)}m, 예상: ${String.format("%.1f", expectedDistance)}m) - 무시")
                            return@collectLatest
                        }
                    }

                    // 속도 기반 필터링 (정지/저속 상태에서 GPS 드리프트 방지) - 기준 완화
                    when {
                        speed < 0.5f -> {
                            // 거의 정지 상태 (0.5m/s = 1.8km/h 미만) - 5m로 완화
                            if (distance > 5.0) {
                                Log.w("TMapComposable", "⚠️ 정지 상태 GPS 점프 감지 (${String.format("%.1f", distance)}m, 속도: ${String.format("%.1f", speed)}m/s) - 무시")
                                return@collectLatest
                            }
                        }
                        speed < 1.5f -> {
                            // 느린 보행 (0.5-1.5m/s, 1.8-5.4km/h) - 기준 완화
                            val maxJump = when {
                                accuracy < 20f -> 30.0   // 8.0 → 30.0
                                accuracy < 30f -> 50.0   // 10.0 → 50.0
                                else -> 60.0             // 12.0 → 60.0
                            }
                            if (distance > maxJump) {
                                Log.w("TMapComposable", "⚠️ 저속 이동 중 GPS 점프 감지 (${String.format("%.1f", distance)}m, 속도: ${String.format("%.1f", speed)}m/s) - 무시")
                                return@collectLatest
                            }
                        }
                        // speed >= 1.5m/s (5.4km/h+): 정상 이동으로 간주, 점프 체크 안 함
                    }

                    // 정확도에 따른 최소 이동 거리 (동적 임계값)
                    val minDistance = when {
                        accuracy < 15f -> 2.0   // 정확도 좋음 (15m 미만): 2m
                        accuracy < 25f -> 5.0   // 정확도 보통 (15-25m): 5m
                        else -> 10.0            // 정확도 나쁨 (25-40m): 10m
                    }

                    // 최소 이동 거리 체크
                    if (distance < minDistance) {
                        return@collectLatest
                    }

                    Log.d("TMapComposable", "📍 위치 업데이트: ${String.format("%.1f", distance)}m 이동, 속도: ${String.format("%.1f", speed)}m/s, 정확도: ${String.format("%.0f", accuracy)}m")
                }

                lastPoint = point

                // 마커 생성 또는 업데이트
                val currentMarker = locationMarker

                // 마커가 없거나 무효화된 경우 재생성
                if (currentMarker == null || currentMarker.id == null) {
                    // 기존 마커가 있으면 제거 시도
                    if (currentMarker != null) {
                        try {
                            tmap.removeTMapMarkerItem("location_marker")
                            Log.d("TMapComposable", "무효화된 마커 제거")
                        } catch (e: Exception) {
                            // 이미 제거되었을 수 있음
                        }
                    }

                    // 새 마커 생성
                    val markerBitmap = pulseFrames[currentPulseFrame]
                    val marker = TMapMarkerItem().apply {
                        id = "location_marker"
                        tMapPoint = TMapPoint(point.latitude, point.longitude)
                        icon = markerBitmap
                    }

                    try {
                        tmap.addTMapMarkerItem(marker)
                        locationMarker = marker
                        Log.d("TMapComposable", "✅ 마커 생성 완료")

                        // 첫 위치로 지도 이동
                        tmap.setCenterPoint(point.latitude, point.longitude)
                        // 홈 화면이 아닐 때만 줌 레벨 변경
                        if (!isHomeScreen) {
                            tmap.setZoomLevel(17)
                        }
                    } catch (e: Exception) {
                        Log.e("TMapComposable", "마커 추가 실패", e)
                        locationMarker = null
                    }
                } else {
                    // 마커 위치 업데이트
                    try {
                        currentMarker.tMapPoint = TMapPoint(point.latitude, point.longitude)
                        tmap.updateTMapMarkerItem(currentMarker)
                    } catch (e: NullPointerException) {
                        // 마커가 무효화됨 - 다음 위치 업데이트 시 재생성
                        Log.w("TMapComposable", "마커 무효화됨 - 재생성 필요")
                        locationMarker = null
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
                            icon = createStartMarkerBitmap(context, Color.parseColor("#4CAF50"))
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
                            icon = createEndMarkerBitmap(context, Color.parseColor("#F44336"))
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

                    // 홈 화면에서 경로 전체가 보이도록 자동 줌 조정
                    if (isHomeScreen && route.path.isNotEmpty()) {
                        try {
                            // 경로의 최소/최대 위경도 계산
                            val minLat = route.path.minOf { it.latitude }
                            val maxLat = route.path.maxOf { it.latitude }
                            val minLon = route.path.minOf { it.longitude }
                            val maxLon = route.path.maxOf { it.longitude }

                            // 경로 중심으로 이동
                            tmap.setCenterPoint(
                                (minLat + maxLat) / 2,
                                (minLon + maxLon) / 2
                            )

                            // 경로 범위에 맞는 줌 레벨 계산 (여유를 위해 1.8배 확장)
                            val latDiff = (maxLat - minLat) * 1.8
                            val lonDiff = (maxLon - minLon) * 1.8
                            val maxDiff = maxOf(latDiff, lonDiff)

                            // 줌 레벨 계산
                            val autoZoomLevel = when {
//                                maxDiff > 0.4 -> 10      // 매우 매우 넓은 범위 (40km+)
                                maxDiff > 0.2 -> 10      // 매우 넓은 범위 (20-40km)
                                maxDiff > 0.1 -> 11      // 넓은 범위 (10-20km)
                                maxDiff > 0.05 -> 12     // 중간 범위 (5-10km)
                                maxDiff > 0.025 -> 13    // 좁은 범위 (2.5-5km)
                                maxDiff > 0.012 -> 14    // 매우 좁은 범위 (1.2-2.5km)
                                maxDiff > 0.006 -> 15    // 아주 좁은 범위 (600m-1.2km)
                                maxDiff > 0.003 -> 16    // 초근접 (300-600m)
                                maxDiff > 0.0015 -> 17   // 극근접 (150-300m)
                                else -> 18               // 최대 확대 (150m 미만)
                            }

                            tmap.zoomLevel = autoZoomLevel
                            Log.d("TMapComposable", "🔍 홈 화면 자동 줌 조정: $autoZoomLevel (범위: ${String.format("%.4f", maxDiff)}, 약 ${String.format("%.1f", maxDiff * 111)}km)")
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "자동 줌 조정 실패", e)
                        }
                    } else if (!isHomeScreen && route.path.isNotEmpty()) {
                        // 위치 탭에서 길찾기 시작 시: 출발지(환자 위치)로 이동 + 줌 레벨 18
                        try {
                            val startPoint = route.path.first()
                            tmap.setCenterPoint(startPoint.latitude, startPoint.longitude)
                            tmap.setZoomLevel(19)
                            Log.d("TMapComposable", "🔍 길찾기 시작 - 출발지로 이동 (줌 레벨 18)")
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "출발지 이동 실패", e)
                        }
                    }

                    Log.d("TMapComposable", "✅ 경로 그리기 완료")
                } else {
                    // 경로가 null이면 PolyLine과 마커 모두 제거
                    routePolyLine?.let { oldPolyLine ->
                        try {
                            tmap.removeTMapPolyLine(oldPolyLine.id)
                            Log.d("TMapComposable", "✅ 기존 PolyLine 제거 성공")
                        } catch (e: Exception) {
                            Log.w("TMapComposable", "기존 PolyLine 제거 실패", e)
                        }
                    }
                    routePolyLine = null
                    routeBearing = 0f  // 경로 방향 리셋

                    try {
                        tmap.removeTMapMarkerItem("start_marker")
                        tmap.removeTMapMarkerItem("end_marker")
                    } catch (e: Exception) {
                        Log.w("TMapComposable", "마커 제거 실패", e)
                    }

                    // 홈 화면에서 경로 제거 시 기본 줌 레벨로 복원
                    if (isHomeScreen) {
                        try {
                            tmap.zoomLevel = zoomLevel
                            Log.d("TMapComposable", "🔍 홈 화면 기본 줌 복원: $zoomLevel")
                        } catch (e: Exception) {
                            Log.e("TMapComposable", "기본 줌 복원 실패", e)
                        }
                    }

                    Log.d("TMapComposable", "🗑️ 경로 제거 완료")
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
            // TMapView를 캐싱하여 재사용하므로 destroy하지 않음
            // 하지만 마커 상태는 초기화하여 펄스 애니메이션 종료
            locationMarker = null
            patientMarker = null
            placeMarker = null
            Log.d("TMapComposable", "🔄 TMapComposable dispose - 마커 상태 초기화 (TMapView는 캐시에 유지)")
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
 * 출발/도착/장소 마커 비트맵 생성 (핀 모양)
 */
private fun createMarkerBitmap(
    context: android.content.Context,
    text: String,
    backgroundColor: Int
): Bitmap {
    val widthDp = 48
    val heightDp = 64  // 핀 모양이므로 높이가 더 김
    val widthPx = (widthDp * context.resources.displayMetrics.density).toInt()
    val heightPx = (heightDp * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centerX = widthPx / 2f

    // 핀 헤드 (상단 원)
    val pinHeadRadius = widthPx / 2f - 8f
    val pinHeadCenterY = pinHeadRadius + 8f

    // 그림자 효과
    val shadowPaint = Paint().apply {
        color = Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
        isAntiAlias = true
        maskFilter = android.graphics.BlurMaskFilter(8f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius + 2f, shadowPaint)

    // 핀 꼬리 (하단 삼각형)
    val pinTailPath = android.graphics.Path().apply {
        moveTo(centerX - pinHeadRadius / 2f, pinHeadCenterY + pinHeadRadius - 4f)
        lineTo(centerX + pinHeadRadius / 2f, pinHeadCenterY + pinHeadRadius - 4f)
        lineTo(centerX, heightPx - 8f)
        close()
    }

    val tailPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawPath(pinTailPath, tailPaint)

    // 핀 헤드 배경 (외곽 원)
    val outerCirclePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius + 3f, outerCirclePaint)

    // 핀 헤드 메인 색상
    val bgPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius, bgPaint)

    // 내부 흰색 원 (텍스트 배경)
    val innerCirclePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius - 6f, innerCirclePaint)

    // 텍스트
    val textPaint = Paint().apply {
        color = backgroundColor
        textSize = 16 * context.resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    val textY = pinHeadCenterY - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(text, centerX, textY, textPaint)

    return bitmap
}

/**
 * 출발 마커 비트맵 생성 (핀 모양 + 원형 아이콘)
 */
private fun createStartMarkerBitmap(
    context: android.content.Context,
    backgroundColor: Int
): Bitmap {
    val widthDp = 36
    val heightDp = 48
    val widthPx = (widthDp * context.resources.displayMetrics.density).toInt()
    val heightPx = (heightDp * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centerX = widthPx / 2f

    // 핀 헤드 (상단 원)
    val pinHeadRadius = widthPx / 2f - 8f
    val pinHeadCenterY = pinHeadRadius + 8f

    // 그림자
    val shadowPaint = Paint().apply {
        color = Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
        isAntiAlias = true
        maskFilter = android.graphics.BlurMaskFilter(6f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius + 2f, shadowPaint)

    // 핀 꼬리
    val pinTailPath = android.graphics.Path().apply {
        moveTo(centerX - pinHeadRadius / 2.5f, pinHeadCenterY + pinHeadRadius - 2f)
        lineTo(centerX + pinHeadRadius / 2.5f, pinHeadCenterY + pinHeadRadius - 2f)
        lineTo(centerX, heightPx - 6f)
        close()
    }
    val tailPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawPath(pinTailPath, tailPaint)

    // 핀 헤드 외곽
    val outerCirclePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius + 2f, outerCirclePaint)

    // 핀 헤드 메인 색상
    val bgPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius, bgPaint)

    // 내부 흰색 원형 아이콘
    val iconRadius = pinHeadRadius * 0.5f
    val iconPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, iconRadius, iconPaint)

    return bitmap
}

/**
 * 도착 마커 비트맵 생성 (핀 모양 + 깃발 아이콘)
 */
private fun createEndMarkerBitmap(
    context: android.content.Context,
    backgroundColor: Int
): Bitmap {
    val widthDp = 36
    val heightDp = 48
    val widthPx = (widthDp * context.resources.displayMetrics.density).toInt()
    val heightPx = (heightDp * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centerX = widthPx / 2f

    // 핀 헤드 (상단 원)
    val pinHeadRadius = widthPx / 2f - 8f
    val pinHeadCenterY = pinHeadRadius + 8f

    // 그림자
    val shadowPaint = Paint().apply {
        color = Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
        isAntiAlias = true
        maskFilter = android.graphics.BlurMaskFilter(6f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius + 2f, shadowPaint)

    // 핀 꼬리
    val pinTailPath = android.graphics.Path().apply {
        moveTo(centerX - pinHeadRadius / 2.5f, pinHeadCenterY + pinHeadRadius - 2f)
        lineTo(centerX + pinHeadRadius / 2.5f, pinHeadCenterY + pinHeadRadius - 2f)
        lineTo(centerX, heightPx - 6f)
        close()
    }
    val tailPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawPath(pinTailPath, tailPaint)

    // 핀 헤드 외곽
    val outerCirclePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius + 2f, outerCirclePaint)

    // 핀 헤드 메인 색상
    val bgPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius, bgPaint)

    // 내부 흰색 깃발 아이콘
    val iconSize = pinHeadRadius * 0.8f
    val flagLeft = centerX - iconSize * 0.35f
    val flagTop = pinHeadCenterY - iconSize * 0.4f

    // 깃대
    val polePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    canvas.drawLine(
        flagLeft,
        flagTop,
        flagLeft,
        flagTop + iconSize * 0.8f,
        polePaint
    )

    // 깃발
    val flagPath = android.graphics.Path().apply {
        moveTo(flagLeft, flagTop)
        lineTo(flagLeft + iconSize * 0.5f, flagTop + iconSize * 0.15f)
        lineTo(flagLeft + iconSize * 0.5f, flagTop + iconSize * 0.45f)
        lineTo(flagLeft, flagTop + iconSize * 0.3f)
        close()
    }
    val flagPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawPath(flagPath, flagPaint)

    return bitmap
}

/**
 * 장소 마커 비트맵 생성 (핀 모양)
 */
private fun createPlaceMarkerBitmap(
    context: android.content.Context,
    backgroundColor: Int
): Bitmap {
    val widthDp = 36
    val heightDp = 48
    val widthPx = (widthDp * context.resources.displayMetrics.density).toInt()
    val heightPx = (heightDp * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centerX = widthPx / 2f

    // 핀 헤드 (상단 원)
    val pinHeadRadius = widthPx / 2f - 8f
    val pinHeadCenterY = pinHeadRadius + 8f

    // 그림자
    val shadowPaint = Paint().apply {
        color = Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
        isAntiAlias = true
        maskFilter = android.graphics.BlurMaskFilter(6f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius + 2f, shadowPaint)

    // 핀 꼬리
    val pinTailPath = android.graphics.Path().apply {
        moveTo(centerX - pinHeadRadius / 2.5f, pinHeadCenterY + pinHeadRadius - 2f)
        lineTo(centerX + pinHeadRadius / 2.5f, pinHeadCenterY + pinHeadRadius - 2f)
        lineTo(centerX, heightPx - 6f)
        close()
    }
    val tailPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawPath(pinTailPath, tailPaint)

    // 핀 헤드 외곽
    val outerCirclePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius + 2f, outerCirclePaint)

    // 핀 헤드 메인 색상
    val bgPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, pinHeadCenterY, pinHeadRadius, bgPaint)

    // 내부 아이콘 (위치 핀 아이콘)
    val iconSize = pinHeadRadius * 0.8f
    val iconPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val iconStrokePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    // 작은 핀 모양 아이콘
    val iconPinRadius = iconSize * 0.25f
    val iconPinCenterY = pinHeadCenterY - iconSize * 0.15f

    // 아이콘 핀 헤드 (원)
    canvas.drawCircle(centerX, iconPinCenterY, iconPinRadius, iconPaint)

    // 아이콘 핀 꼬리 (삼각형)
    val iconPinTailPath = android.graphics.Path().apply {
        moveTo(centerX - iconPinRadius * 0.4f, iconPinCenterY + iconPinRadius - 1f)
        lineTo(centerX + iconPinRadius * 0.4f, iconPinCenterY + iconPinRadius - 1f)
        lineTo(centerX, pinHeadCenterY + iconSize * 0.25f)
        close()
    }
    canvas.drawPath(iconPinTailPath, iconPaint)

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
