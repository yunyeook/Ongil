package kr.co.ongil.wear.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.common.location.LocationStreamBus
import javax.inject.Singleton

/**
 * 앱 전역 의존성 주입 모듈
 *
 * 스프링부트의 @Configuration과 동일한 역할
 * - Context 제공
 * - 싱글톤 객체 생성
 * - Common 모듈 의존성 제공
 */
@Module  // Hilt 모듈 표시
@InstallIn(SingletonComponent::class)  // 앱 전체 생명주기 (스프링의 ApplicationContext)
object WearAppModule {

    /**
     * Application Context 제공
     *
     * 스프링의 @Bean과 동일
     */
    @Provides  // 스프링의 @Bean과 동일
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context  // Hilt가 자동으로 주입
    ): Context {
        return context
    }

    /**
     * LocationStreamBus 제공 (Common 모듈)
     *
     * 앱 내 위치 업데이트 브로드캐스트 버스
     */
    @Provides
    @Singleton
    fun provideLocationStreamBus(): LocationStreamBus {
        return LocationStreamBus()
    }

    /**
     * FusedLocationProviderClient 제공
     *
     * Google Play Services 위치 클라이언트
     */
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}
