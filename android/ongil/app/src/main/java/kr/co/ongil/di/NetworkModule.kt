package kr.co.ongil.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaType
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.datasource.remote.api.CallApi
import kr.co.ongil.data.datasource.remote.api.FavoriteApi
import kr.co.ongil.data.datasource.remote.api.MapApi
import kr.co.ongil.data.datasource.remote.api.NotificationApi
import kr.co.ongil.data.datasource.remote.api.UserApi
import kr.co.ongil.data.datasource.remote.interceptor.AuthInterceptor
import kr.co.ongil.BuildConfig

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ForInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        explicitNulls = false
    }

    /**
     * AuthApi 전용 OkHttpClient (AuthInterceptor 없음)
     * 순환 참조 방지
     */
    @Provides
    @Singleton
    @AuthOkHttpClient
    fun provideAuthOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    /**
     * 일반 API용 OkHttpClient (AuthInterceptor 포함)
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenManager: UserDataStoreManager,
        @ForInterceptor authApi: AuthApi
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // BODY로 변경하여 실제 JSON 확인
        }
        val authInterceptor = AuthInterceptor(tokenManager, authApi)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor) // 401 에러 시 자동 토큰 갱신
            .build()
    }

    /**
     * AuthApi 전용 Retrofit
     */
    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(
        json: Json,
        @AuthOkHttpClient client: OkHttpClient
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /**
     * 일반 API용 Retrofit
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        json: Json,
        client: OkHttpClient
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /**
     * AuthInterceptor 전용 AuthApi (비인증 - 순환 참조 방지)
     * refresh 호출용
     */
    @Provides
    @Singleton
    @ForInterceptor
    fun provideAuthApiForInterceptor(@AuthRetrofit retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    /**
     * Repository에서 사용하는 AuthApi (인증 - logout에 Authorization 헤더 필요)
     */
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideFavoriteApi(retrofit: Retrofit): FavoriteApi =
        retrofit.create(FavoriteApi::class.java)

    @Provides
    @Singleton
    fun provideCallApi(retrofit: Retrofit): CallApi =
        retrofit.create(CallApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideMapApi(retrofit: Retrofit): MapApi =
        retrofit.create(MapApi::class.java)
}