package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.ongil.domain.model.SearchPlace
import kr.co.ongil.common.BuildConfig
import kr.co.ongil.domain.repository.TMapRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TMapRepositoryImpl @Inject constructor() : TMapRepository {

    private val client = OkHttpClient()

    override suspend fun searchPlaces(
        query: String,
        latitude: Double?,
        longitude: Double?
    ): Result<List<SearchPlace>> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) {
                return@withContext Result.success(emptyList())
            }

            // TMap 통합검색 API 호출
            val urlBuilder = StringBuilder("https://apis.openapi.sk.com/tmap/pois?version=1")
            urlBuilder.append("&searchKeyword=$query")
            urlBuilder.append("&count=20")

            // 중심 좌표가 있으면 추가
            if (latitude != null && longitude != null) {
                urlBuilder.append("&centerLat=$latitude")
                urlBuilder.append("&centerLon=$longitude")
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .addHeader("appKey", BuildConfig.TMAP_API_KEY)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonString = response.body?.string() ?: ""
                val places = parseTMapResponse(jsonString)
                Log.d("TMapRepository", "검색 성공: ${places.size}개 결과")
                Result.success(places)
            } else {
                Log.e("TMapRepository", "검색 실패: ${response.code}")
                Result.failure(Exception("TMap API 오류: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("TMapRepository", "검색 오류: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * TMap API 응답 파싱
     */
    private fun parseTMapResponse(jsonString: String): List<SearchPlace> {
        try {
            val json = JSONObject(jsonString)
            val searchPoiInfo = json.optJSONObject("searchPoiInfo") ?: return emptyList()
            val pois = searchPoiInfo.optJSONObject("pois") ?: return emptyList()
            val poiArray = pois.optJSONArray("poi") ?: return emptyList()

            val places = mutableListOf<SearchPlace>()
            for (i in 0 until poiArray.length()) {
                val poi = poiArray.getJSONObject(i)

                val poiId = poi.optString("id", "")
                val name = poi.optString("name", "")
                val address = poi.optString("upperAddrName", "") + " " +
                             poi.optString("middleAddrName", "") + " " +
                             poi.optString("lowerAddrName", "")
                val lat = poi.optString("noorLat", "0").toDoubleOrNull() ?: 0.0
                val lon = poi.optString("noorLon", "0").toDoubleOrNull() ?: 0.0

                if (name.isNotBlank()) {
                    places.add(
                        SearchPlace(
                            id = poiId,
                            name = name,
                            address = address.trim(),
                            latitude = lat,
                            longitude = lon
                        )
                    )
                }
            }

            return places
        } catch (e: Exception) {
            Log.e("TMapRepository", "응답 파싱 오류: ${e.message}", e)
            return emptyList()
        }
    }
}
