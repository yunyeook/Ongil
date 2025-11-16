package kr.co.ongil.wear.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.ongil.wear.data.datasource.local.WearDataStoreManager
import kr.co.ongil.wear.data.datasource.remote.WearAuthInterceptor
import kr.co.ongil.wear.data.datasource.remote.WearRetrofitClient
import kr.co.ongil.wear.data.datasource.remote.api.WearAuthApi
import kr.co.ongil.wear.data.datasource.remote.api.WearCallApi
import kr.co.ongil.wear.data.datasource.remote.api.WearLocationApi
import kr.co.ongil.wear.data.datasource.remote.api.WearSosApi
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Singleton

/**
 * Wear OS 네트워크 DI 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object WearNetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrl(): String {
        // TODO: BuildConfig에서 가져오도록 수정 필요
        // local.properties에 BASE_URL 추가 필요
        return "https://staging.on-gil.co.kr/api"
    }

    @Provides
    @Singleton
    fun provideWearAuthApi(): WearAuthApi {
        // AuthApi는 AuthInterceptor 없이 생성 (순환 참조 방지)
        // 별도의 Retrofit 인스턴스 사용
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(
                okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl(provideBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()

        return retrofit.create(WearAuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWearAuthInterceptor(
        dataStoreManager: WearDataStoreManager,
        authApi: WearAuthApi
    ): WearAuthInterceptor {
        return WearAuthInterceptor(dataStoreManager, authApi)
    }

    @Provides
    @Singleton
    fun provideWearRetrofitClient(
        authInterceptor: WearAuthInterceptor,
        baseUrl: String
    ): WearRetrofitClient {
        return WearRetrofitClient(authInterceptor, baseUrl)
    }

    @Provides
    @Singleton
    fun provideWearLocationApi(
        retrofitClient: WearRetrofitClient
    ): WearLocationApi {
        return retrofitClient.createApi()
    }

    @Provides
    @Singleton
    fun provideWearCallApi(
        retrofitClient: WearRetrofitClient
    ): WearCallApi {
        return retrofitClient.createApi()
    }

    @Provides
    @Singleton
    fun provideWearSosApi(
        retrofitClient: WearRetrofitClient
    ): WearSosApi {
        return retrofitClient.createApi()
    }
}
