package kr.co.ongil.wear.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.wear.data.repository.CallRepositoryImpl
import kr.co.ongil.wear.data.repository.LocationRepositoryImpl
import kr.co.ongil.wear.data.repository.SosRepositoryImpl
import kr.co.ongil.wear.domain.repository.CallRepository
import kr.co.ongil.wear.domain.repository.LocationRepository
import kr.co.ongil.wear.domain.repository.SosRepository
import javax.inject.Singleton

/**
 * Wear OS Repository DI 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WearRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindCallRepository(
        impl: CallRepositoryImpl
    ): CallRepository

    @Binds
    @Singleton
    abstract fun bindSosRepository(
        impl: SosRepositoryImpl
    ): SosRepository
}
