package kr.co.ongil.presentation.ui.map

import androidx.compose.ui.graphics.Color

/**
 * 안전 범위 UI 설정
 *
 * Note: 안전 범위 반경과 이상 판정 기준 시간은 SafetyZoneMonitor에서 관리됩니다.
 * 이 파일은 UI 관련 설정만 포함합니다.
 */
object SafetyZoneConfig {
    /**
     * 기준점 (홈 위치)
     * 나중에 사용자 설정으로 변경 가능
     */
    object HomeLocation {
        const val LATITUDE = 37.50175822768635
        const val LONGITUDE = 127.03958229478599
    }

    /**
     * 안전 범위 동심원 색상 (Android Color Int)
     * 나중에 사용자가 수정할 수 있도록 var로 선언
     */
    object CircleColors {
        // 1단계 - 초록색 (더 진하게)
        var stage1FillColor = Color(0x6400FF00)   // 초록색 반투명 (투명도 약 39%)
        var stage1StrokeColor = Color(0xFF00C800) // 초록색 외곽선

        // 2단계 - 주황색 (더 진하게)
        var stage2FillColor = Color(0x64FFA500)   // 주황색 반투명 (투명도 약 39%)
        var stage2StrokeColor = Color(0xFF007BFF) // 주황색 외곽선

        // 3단계 - 빨간색 (더 진하게)
        var stage3FillColor = Color(0x64FF0000)   // 빨간색 반투명 (투명도 약 39%)
        var stage3StrokeColor = Color(0xFFDC0000) // 빨간색 외곽선
    }
}
