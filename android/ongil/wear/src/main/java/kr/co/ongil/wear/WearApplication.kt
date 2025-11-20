package kr.co.ongil.wear

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
/**
 * Wear OS 앱의 Application 클래스
 *
 * 스프링부트의 @SpringBootApplication과 동일한 역할
 * - Hilt 의존성 주입 초기화
 * - 앱 전역 설정
 */
@HiltAndroidApp  // ← 스프링의 @SpringBootApplication 같은 것
class WearApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 앱 시작 시 실행되는 코드
        // 스프링의 @PostConstruct 같은 느낌

        // 필요하면 여기에 초기화 코드 추가
        // 예: 로그 설정, 크래시 리포팅 등
    }
}