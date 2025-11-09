package kr.co.ongil.presentation.ui.map

import android.content.Context
import android.util.Log
import com.skt.tmap.TMapView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.ongil.common.BuildConfig

/**
 * TMap 초기화 로직을 담당하는 매니저
 */
object TMapViewFactory {

    /**
     * TMapView를 생성하고 초기화합니다.
     *
     * @param context 애플리케이션 컨텍스트
     * @param latitude 지도 중심 위도
     * @param longitude 지도 중심 경도
     * @param zoomLevel 줌 레벨
     * @return 초기화된 TMapView 인스턴스
     */
    suspend fun createTMapView(
        context: Context,
        latitude: Double = 37.5665,
        longitude: Double = 126.9780,
        zoomLevel: Int = 15
    ): TMapView = withContext(Dispatchers.Main) {

        TMapView(context).apply {
            // API 키 설정
            setSKTMapApiKey(BuildConfig.TMAP_API_KEY)

            // 기본 설정
            setZoomLevel(zoomLevel)
            setCenterPoint(latitude, longitude)  // TMap은 (위도, 경도) 순서

            Log.d("TMapManager", "TMapView configured - Center: ($latitude, $longitude), Zoom: $zoomLevel")
        }
    }
}