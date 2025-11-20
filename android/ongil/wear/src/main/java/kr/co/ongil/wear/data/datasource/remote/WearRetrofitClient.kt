package kr.co.ongil.wear.data.datasource.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wear OS용 Retrofit 클라이언트
 *
 * 앱 모듈의 RetrofitClient를 참고하되, Wear OS에 최적화:
 * - 배터리 절약을 위해 타임아웃 단축
 * - 필요한 API만 선택적으로 제공
 */
@Singleton
class WearRetrofitClient @Inject constructor(
    private val authInterceptor: WearAuthInterceptor,
    private val baseUrl: String
) {

    // Kotlinx Serialization Json 설정
    private val json = Json {
        ignoreUnknownKeys = true // API 응답에 정의되지 않은 필드 무시
        coerceInputValues = true // null 값을 기본값으로 변환
        isLenient = true // 유연한 JSON 파싱
    }

    // OkHttp 클라이언트
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) // 앱보다 짧게 (배터리 절약)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor) // Auth Interceptor 추가
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    // Retrofit 인스턴스
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // API 인스턴스 생성 헬퍼
    inline fun <reified T> createApi(): T {
        return retrofit.create(T::class.java)
    }
}
