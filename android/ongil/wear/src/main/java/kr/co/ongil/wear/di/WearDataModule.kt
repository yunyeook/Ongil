
package kr.co.ongil.wear.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.wear.data.datasource.local.WearDataStoreManager
import kr.co.ongil.wear.data.datasource.local.WearDataStoreManagerImpl
import javax.inject.Singleton
import kr.co.ongil.wear.data.repository.WearAuthRepositoryImpl
import kr.co.ongil.wear.domain.repository.WearAuthRepository

/**
 * 데이터 관련 의존성 주입 모듈
 *
 * 스프링부트의 @Configuration과 동일
 * - Repository 바인딩
 * - DataStore Manager 바인딩
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WearDataModule {

    /**
     * WearDataStoreManager 인터페이스를 구현체에 바인딩
     *
     * 스프링의 @Bean과 동일하지만
     * 인터페이스 <-> 구현체 연결 전용
     */
    @Binds
    @Singleton
    abstract fun bindWearDataStoreManager(
        impl: WearDataStoreManagerImpl  // 구현체
    ): WearDataStoreManager  // 인터페이스

    /**
     * WearAuthRepository 인터페이스를 구현체에 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindWearAuthRepository(
        impl: WearAuthRepositoryImpl
    ): WearAuthRepository
}
