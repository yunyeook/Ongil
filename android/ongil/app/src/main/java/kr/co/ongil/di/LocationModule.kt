package kr.co.ongil.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.common.location.LocationStreamBus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationStreamBus(): LocationStreamBus {
        return LocationStreamBus()
    }
}
