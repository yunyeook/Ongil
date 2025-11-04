package kr.co.ongil.map

import android.content.Context
import com.skt.tmap.TMapView
import kr.co.ongil.common.BuildConfig

/**
 * TMAP SDK 인증을 처리하는 객체입니다.
 */
object TMapInitializer {

    /**
     * TMAP SDK를 초기화하고 API 키를 설정합니다.
     * 이 메서드는 Application 클래스나 메인 액티비티의 onCreate에서 한 번만 호출하면 됩니다.
     *
     * @param context 애플리케이션 컨텍스트
     */
    fun initialize(context: Context) {
        // TMapView 인스턴스를 생성하여 BuildConfig에서 API 키를 가져와 설정합니다.
        // 한 번 설정하면 앱 세션 동안 유효합니다.
        val tmapView = TMapView(context)
        tmapView.setSKTMapApiKey(BuildConfig.TMAP_API_KEY)
    }
}
