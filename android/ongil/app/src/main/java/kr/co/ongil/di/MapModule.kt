package kr.co.ongil.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.data.repository.TMapRepositoryImpl
import kr.co.ongil.data.repository.MapRepositoryImpl
import kr.co.ongil.domain.repository.TMapRepository
import kr.co.ongil.domain.repository.MapRepository
import javax.inject.Singleton

/**
 * 지도 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {

    @Binds
    @Singleton
    abstract fun bindTMapRepository(
        impl: TMapRepositoryImpl
    ): TMapRepository

    @Binds
    @Singleton
    abstract fun bindMapRepository(
        impl: MapRepositoryImpl
    ): MapRepository
}
