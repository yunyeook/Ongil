package kr.co.ongil.wear.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 앱 전역 의존성 주입 모듈
 *
 * 스프링부트의 @Configuration과 동일한 역할
 * - Context 제공
 * - 싱글톤 객체 생성
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
}
