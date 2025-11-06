package kr.co.ongil.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.data.datasource.local.preferences.TokenManager
import kr.co.ongil.data.datasource.local.preferences.TokenManagerImpl
import javax.inject.Singleton

/**
 * DataStore 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    /**
     * TokenManager 구현체 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindTokenManager(
        tokenManagerImpl: TokenManagerImpl
    ): TokenManager
}
