package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.map.SearchPlaceApiResponse
import kr.co.ongil.data.model.map.UpdateLocationRequest
import kr.co.ongil.data.model.map.UpdateLocationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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

    /**
     * 환자 위치 정보 전송
     * POST /api/v1/patients/{patientId}/location
     */
    @POST("/api/v1/patients/{patientId}/location")
    suspend fun updatePatientLocation(
        @Path("patientId") patientId: Long,
        @Body request: UpdateLocationRequest
    ): UpdateLocationResponse
}
