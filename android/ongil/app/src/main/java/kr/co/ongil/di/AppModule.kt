package kr.co.ongil.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.data.datasource.wear.WearDataClient
import javax.inject.Singleton

/**
 * 앱 전역 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Application Context 제공
     */
    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context {
        return context
    }

    /**
     * WearDataClient 제공
     */
    @Provides
    @Singleton
    fun provideWearDataClient(
        context: Context
    ): WearDataClient {
        return WearDataClient(context)
    }
}
