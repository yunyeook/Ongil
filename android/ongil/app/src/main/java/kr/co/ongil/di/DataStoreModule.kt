package kr.co.ongil.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManagerImpl
import javax.inject.Singleton

/**
 * DataStore 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    companion object {
        @Provides
        @Singleton
        fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile("user_preferences")
            }
        }
    }

    /**
     * UserDataStoreManager 구현체 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindUserDataStoreManager(
        userDataStoreManagerImpl: UserDataStoreManagerImpl
    ): UserDataStoreManager
}
