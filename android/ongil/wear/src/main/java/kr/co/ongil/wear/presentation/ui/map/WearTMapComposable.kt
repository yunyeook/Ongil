package kr.co.ongil.wear.presentation.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.wear.compose.material.CircularProgressIndicator
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem
import kotlinx.coroutines.delay
import kr.co.ongil.common.BuildConfig

/**
 * 워치용 간단한 TMap Composable
 *
 * - 지도만 표시 (읽기 전용)
 * - 현재 위치 마커
 * - 워치 화면에 최적화
 */
@Composable
fun WearTMapComposable(
    modifier: Modifier = Modifier,
    latitude: Double = 37.5665,  // 서울시청 기본값
    longitude: Double = 126.9780,
    zoomLevel: Int = 15
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<TMapView?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 로딩 중이면 스피너 표시
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // TMapView 생성
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            android.util.Log.d("WearTMapComposable", "TMapView 생성 시작")
            createTMapView(ctx, latitude, longitude, zoomLevel).also { tMap ->
                mapView = tMap
            }
        }
    )

    // 지도 초기화 (비동기)
    LaunchedEffect(mapView) {
        mapView?.let { tmap ->
            try {
                android.util.Log.d("WearTMapComposable", "지도 초기화 시작")

                // 지도 엔진 초기화 대기
                delay(500)

                // 마커 추가
                addLocationMarker(tmap, latitude, longitude, context)

                android.util.Log.d("WearTMapComposable", "지도 초기화 완료")
                isLoading = false

            } catch (e: Exception) {
                android.util.Log.e("WearTMapComposable", "지도 초기화 실패", e)
                isLoading = false  // 에러나도 로딩은 끝내기
            }
        }
    }

    // 컴포넌트 파괴 시 정리
    DisposableEffect(Unit) {
        onDispose {
            mapView = null
        }
    }
}

/**
 * TMapView 생성
 */
private fun createTMapView(
    context: Context,
    latitude: Double,
    longitude: Double,
    zoomLevel: Int
): TMapView {
    return TMapView(context).apply {
        try {
            android.util.Log.d("WearTMapComposable", "TMAP API 키 설정")

            // TMAP API 키 설정
            setSKTMapApiKey(BuildConfig.TMAP_API_KEY)

            // 지도 기본 설정
            setZoomLevel(zoomLevel)
            setCenterPoint(longitude, latitude)  // TMap 3.0: (경도, 위도)

            // 컨트롤 숨기기
            setCompassMode(false)

            android.util.Log.d("WearTMapComposable", "TMapView 생성 완료: lat=$latitude, lon=$longitude, zoom=$zoomLevel")

        } catch (e: Exception) {
            android.util.Log.e("WearTMapComposable", "TMapView 설정 실패", e)
        }
    }
}

/**
 * 현재 위치 마커 추가
 */
private fun addLocationMarker(
    mapView: TMapView,
    latitude: Double,
    longitude: Double,
    context: Context
) {
    try {
        android.util.Log.d("WearTMapComposable", "마커 추가 시작")

        // 마커 아이템 생성
        val marker = TMapMarkerItem().apply {
            id = "current_location"
            tMapPoint = TMapPoint(latitude, longitude)
            icon = createCircleMarkerBitmap(context)
        }

        // 마커 추가
        mapView.addTMapMarkerItem(marker)

        android.util.Log.d("WearTMapComposable", "마커 추가 완료")

    } catch (e: Exception) {
        android.util.Log.e("WearTMapComposable", "마커 추가 실패", e)
    }
}

/**
 * 원형 마커 비트맵 생성 (녹색)
 */
private fun createCircleMarkerBitmap(context: Context): Bitmap {
    val size = 40  // 마커 크기 (픽셀)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 외곽 원 (흰색)
    val paintOuter = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintOuter)

    // 내부 원 (녹색)
    val paintInner = Paint().apply {
        color = android.graphics.Color.parseColor("#5C7165")  // 온길 그린
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 4, paintInner)

    return bitmap
}