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

    // 화면별로 TMapView 캐싱 (홈탭용, 위치탭용)
    @Volatile
    private var homeTMapView: TMapView? = null

    @Volatile
    private var locationTMapView: TMapView? = null

    /**
     * 현재 기기 위치를 중심으로 지도를 초기화하여 TMapView를 생성합니다.
     * 화면별로 다른 TMapView를 사용합니다.
     *
     * @param context 애플리케이션 컨텍스트
     * @param zoomLevel 줌 레벨
     * @param isHomeScreen 홈 화면 여부
     * @param initialCenter 초기 중심 위치 (null이면 GPS 또는 기본 위치 사용)
     * @return 초기화된 TMapView 인스턴스
     */
    /**
     * TMapView를 생성하거나 캐시에서 가져옵니다.
     * @return Pair<TMapView, Boolean> - (TMapView 인스턴스, 캐싱된 뷰인지 여부)
     */
    suspend fun createTMapView(
        context: Context,
        zoomLevel: Int = 15,
        isHomeScreen: Boolean = false,
        initialCenter: Pair<Double, Double>? = null
    ): Pair<TMapView, Boolean> = withContext(Dispatchers.Main) {
        // 화면에 맞는 캐시 확인
        val cachedView = if (isHomeScreen) homeTMapView else locationTMapView

        cachedView?.let { existingView ->
            Log.d("TMapManager", "기존 TMapView 재사용 (${if (isHomeScreen) "홈" else "위치"}탭)")

            // 기존 부모에서 제거 (중요!)
            (existingView.parent as? android.view.ViewGroup)?.removeView(existingView)

            // 기존 마커 및 오버레이 모두 제거 (깨끗한 상태로 재사용)
            try {
                existingView.removeAllTMapMarkerItem()
                existingView.removeAllTMapPolyLine()
                existingView.removeAllTMapCircle()
                Log.d("TMapManager", "✅ 기존 마커/오버레이 모두 제거 완료")
            } catch (e: Exception) {
                Log.e("TMapManager", "❌ 마커/오버레이 제거 실패", e)
            }

            // 이전 OnMapReadyListener 제거 (중요!)
            try {
                existingView.setOnMapReadyListener(null)
                Log.d("TMapManager", "✅ 기존 OnMapReadyListener 제거")
            } catch (e: Exception) {
                Log.w("TMapManager", "OnMapReadyListener 제거 실패", e)
            }

            Log.d("TMapManager", "✅ 캐싱된 TMapView 준비 완료")

            return@withContext Pair(existingView, true)  // 캐싱된 뷰
        }

        Log.d("TMapManager", "새로운 TMapView 생성 (${if (isHomeScreen) "홈" else "위치"}탭)")

        // 초기 중심 위치 결정
        val (lat, lon) = if (initialCenter != null) {
            Log.d("TMapManager", "제공된 초기 위치 사용: (${initialCenter.first}, ${initialCenter.second})")
            initialCenter
        } else {
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

            // 위치를 가져오지 못하면 기본 위치(서울 시청) 사용
            val latitude = loc?.latitude ?: 37.5665
            val longitude = loc?.longitude ?: 126.9780

            if (loc == null) {
                Log.w("TMapManager", "현재 위치 없음 - 기본 위치 사용: ($latitude, $longitude)")
            }

            Pair(latitude, longitude)
        }

        val newTMapView = TMapView(context).apply {
            // API 키 설정
            setSKTMapApiKey(BuildConfig.TMAP_API_KEY)

            // OnMapReadyListener는 TMapComposable에서 설정
            // 여기서 설정하면 캐싱 후 재사용 시 타이밍 문제 발생

            resetNorthUp(this)
            setZoomLevel(zoomLevel)
            setCenterPoint(lat, lon) // TMap 3.0: (위도, 경도)
            Log.d("TMapManager", "TMapView 초기화 - 위치: ($lat, $lon), Zoom: $zoomLevel")
        }

        // 화면에 맞는 캐시에 저장
        if (isHomeScreen) {
            homeTMapView = newTMapView
        } else {
            locationTMapView = newTMapView
        }

        return@withContext Pair(newTMapView, false)  // 새로 생성된 뷰
    }

    /**
     * 캐시된 TMapView를 해제합니다.
     * 앱 종료 시 호출하여 메모리 누수를 방지합니다.
     */
    fun clearCache() {
        homeTMapView = null
        locationTMapView = null
        Log.d("TMapManager", "TMapView 캐시 해제")
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