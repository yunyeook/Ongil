package kr.co.ongil.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import javax.inject.Singleton
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.domain.handler.SosActionHandler
import kr.co.ongil.presentation.handler.SosActionHandlerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class HandlerModule {

    @Binds
    @Singleton
    abstract fun bindSosActionHandler(impl: SosActionHandlerImpl): SosActionHandler

}