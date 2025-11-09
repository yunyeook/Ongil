package kr.co.ongil.presentation.ui.map

import android.content.Context
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.skt.tmap.TMapView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.ongil.common.BuildConfig

/**
 * TMap 초기화 로직을 담당하는 매니저
 */
object TMapViewFactory {

    /**
     * 현재 기기 위치를 중심으로 지도를 초기화하여 TMapView를 생성합니다.
     *
     * @param context 애플리케이션 컨텍스트
     * @param zoomLevel 줌 레벨
     * @return 초기화된 TMapView 인스턴스
     */
    suspend fun createTMapView(
        context: Context,
        zoomLevel: Int = 15
    ): TMapView = withContext(Dispatchers.Main) {
        // 현재 위치 우선 확보
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val loc = try {
            @Suppress("MissingPermission")
            fusedClient.lastLocation.await()
        } catch (e: SecurityException) {
            Log.e("TMapManager", "위치 권한 없음", e)
            null
        } catch (e: Exception) {
            Log.e("TMapManager", "현재 위치 조회 실패", e)
            null
        }

        val lat = loc?.latitude
        val lon = loc?.longitude
        if (lat == null || lon == null) {
            throw IllegalStateException("현재 위치를 가져오지 못했습니다. 권한/설정 확인 후 다시 시도하세요.")
        }

        TMapView(context).apply {
            // API 키 설정
            setSKTMapApiKey(BuildConfig.TMAP_API_KEY)
            resetNorthUp(this)
            setZoomLevel(zoomLevel)
            setCenterPoint(lat, lon) // TMap 3.0: (위도, 경도)
            Log.d("TMapManager", "TMapView 초기화 - 내 위치: ($lat, $lon), Zoom: $zoomLevel")
        }
    }
}

private fun resetNorthUp(tmap: TMapView) {
    // Try a few possible API names across SDK variants
    if (tryInvokeRotation(tmap, "setMapRotation")) return
    if (tryInvokeRotation(tmap, "setRotation")) return
    if (tryInvokeRotationDouble(tmap, "setRotationAngle")) return

    // Fallback: disable compass mode so north stays up
    try {
        tmap.setCompassMode(false)
        Log.d("TMapManager", "북쪽 고정: setCompassMode(false) 대체 적용")
    } catch (_: Exception) {
        // ignore
    }
}

private fun tryInvokeRotation(tmap: TMapView, methodName: String): Boolean {
    return try {
        val m = TMapView::class.java.getMethod(methodName, Float::class.javaPrimitiveType)
        m.isAccessible = true
        m.invoke(tmap, 0f)
        Log.d("TMapManager", "북쪽 고정: $methodName(0f) 호출")
        true
    } catch (_: Throwable) {
        false
    }
}

private fun tryInvokeRotationDouble(tmap: TMapView, methodName: String): Boolean {
    return try {
        val m = TMapView::class.java.getMethod(methodName, Double::class.javaPrimitiveType)
        m.isAccessible = true
        m.invoke(tmap, 0.0)
        Log.d("TMapManager Double", "북쪽 고정: $methodName(0.0) 호출")
        true
    } catch (_: Throwable) {
        false
    }
}