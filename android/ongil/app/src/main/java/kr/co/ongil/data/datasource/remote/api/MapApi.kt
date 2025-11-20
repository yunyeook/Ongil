package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.map.ReportAbnormalRequest
import kr.co.ongil.data.model.map.ReportAbnormalResponse
import kr.co.ongil.data.model.map.NavigationEndRequest
import kr.co.ongil.data.model.map.NavigationEndResponse
import kr.co.ongil.data.model.map.NavigationStartRequest
import kr.co.ongil.data.model.map.NavigationStartResponse
import kr.co.ongil.data.model.map.PlaceDetailApiResponse
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

    /**
     * 이상 탐지 이벤트 등록
     * POST /api/v1/patients/{patientId}/abnormals
     */
    @POST("/api/v1/patients/{patientId}/abnormals")
    suspend fun reportAbnormal(
        @Path("patientId") patientId: Long,
        @Body request: ReportAbnormalRequest
    ): ReportAbnormalResponse

    /**
     * 장소 상세 정보 조회
     * GET /api/v1/map/places/{poiId}
     */
    @GET("/api/v1/map/places/{poiId}")
    suspend fun getPlaceDetail(
        @Path("poiId") poiId: String
    ): PlaceDetailApiResponse

    /**
     * 길안내 시작
     * POST /api/v1/navigation/start
     */
    @POST("/api/v1/navigation/start")
    suspend fun startNavigation(
        @Body request: NavigationStartRequest
    ): NavigationStartResponse

    /**
     * 길안내 종료
     * POST /api/v1/navigation/end
     */
    @POST("/api/v1/navigation/end")
    suspend fun endNavigation(
        @Body request: NavigationEndRequest
    ): NavigationEndResponse
}
