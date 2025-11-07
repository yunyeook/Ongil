package kr.co.ongil.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.data.repository.AuthRepositoryImpl
import kr.co.ongil.data.repository.CallRepositoryImpl
import kr.co.ongil.data.repository.FavoriteRepositoryImpl
import kr.co.ongil.data.repository.FindPasswordAuthRepositoryImpl
import kr.co.ongil.data.repository.NotificationRepositoryImpl
import kr.co.ongil.data.repository.UserRepositoryImpl
import kr.co.ongil.domain.repository.AuthRepository
import kr.co.ongil.domain.repository.CallRepository
import kr.co.ongil.domain.repository.FavoriteRepository
import kr.co.ongil.domain.repository.FindPasswordAuthRepository
import kr.co.ongil.domain.repository.NotificationRepository
import kr.co.ongil.domain.repository.UserRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindFavoriteRepository(
        impl: FavoriteRepositoryImpl
    ): FavoriteRepository

    @Binds
    abstract fun bindFindPasswordAuthRepository(
        impl: FindPasswordAuthRepositoryImpl
    ): FindPasswordAuthRepository

    @Binds
    abstract fun bindCallRepository(
        impl: CallRepositoryImpl
    ): CallRepository

    @Binds
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository
}