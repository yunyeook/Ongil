package kr.co.ongil.data.datasource.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kr.co.ongil.data.datasource.remote.api.CallApi
import kr.co.ongil.data.datasource.remote.api.NotificationApi
import kr.co.ongil.data.datasource.remote.api.UserApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import kr.co.ongil.BuildConfig

/**
 * Retrofit 클라이언트 싱글톤
 * Kotlinx Serialization을 사용하여 JSON 직렬화/역직렬화 처리
 */
object RetrofitClient {

    private const val BASE_URL = BuildConfig.BASE_URL

    // Kotlinx Serialization Json 설정
    private val json = Json {
        ignoreUnknownKeys = true // API 응답에 정의되지 않은 필드 무시
        coerceInputValues = true // null 값을 기본값으로 변환
        isLenient = true // 유연한 JSON 파싱
    }

    // OkHttp 클라이언트
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    // Retrofit 인스턴스
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // API 인스턴스들
    val userApi: UserApi by lazy {
        instance.create(UserApi::class.java)
    }

    val callApi: CallApi by lazy {
        instance.create(CallApi::class.java)
    }

    val notificationApi: NotificationApi by lazy {
        instance.create(NotificationApi::class.java)
    }
}
