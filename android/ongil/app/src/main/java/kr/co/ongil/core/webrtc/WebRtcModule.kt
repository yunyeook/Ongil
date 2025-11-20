package kr.co.ongil.core.webrtc

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebRtcModule {

    @Provides
    @Singleton
    fun provideWebRtcCallClient(
        @ApplicationContext context: Context
    ): WebRtcCallClient = WebRtcCallClient(context)
}