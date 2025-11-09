package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.map.SearchPlaceApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 지도 관련 API
 */
interface MapApi {

    /**
     * 장소 검색
     * GET /api/v1/map/search
     */
    @GET("/api/v1/map/search")
    suspend fun searchPlaces(
        @Query("keyword") keyword: String,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radius") radius: Int? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): SearchPlaceApiResponse
}
