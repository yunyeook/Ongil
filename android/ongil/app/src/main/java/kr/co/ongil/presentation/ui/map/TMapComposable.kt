package kr.co.ongil.presentation.ui.map

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
import com.skt.tmap.TMapView

/**
 * TMap을 표시하는 Composable 컴포넌트 (앱 전용)
 *
 * @param modifier 레이아웃 수정자
 * @param latitude 지도 중심 위도 (기본값: 서울 시청)
 * @param longitude 지도 중심 경도 (기본값: 서울 시청)
 * @param zoomLevel 줌 레벨 (기본값: 15)
 * @param onMapReady TMapView가 준비되었을 때 호출되는 콜백
 */
@Composable
fun TMapComposable(
    modifier: Modifier = Modifier,
    latitude: Double = 37.5665,
    longitude: Double = 126.9780,
    zoomLevel: Int = 15,
    onMapReady: ((TMapView) -> Unit)? = null
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<TMapView?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 백그라운드 스레드에서 TMap 초기화 (common 모듈의 TMapManager 사용)
    LaunchedEffect(Unit) {
        val tmapView = TMapViewFactory.createTMapView(
            context = context,
            latitude = latitude,
            longitude = longitude,
            zoomLevel = zoomLevel
        )

        mapView = tmapView
        isLoading = false
        onMapReady?.invoke(tmapView)
        Log.d("TMapComposable", "TMapView initialization complete")

    }

    Box(modifier = modifier.fillMaxSize()) {
        // 지도가 준비되면 표시
        mapView?.let { view ->
            AndroidView(
                factory = { view },
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
            Log.d("TMapComposable", "TMapView disposed")
            mapView = null
        }
    }
}
