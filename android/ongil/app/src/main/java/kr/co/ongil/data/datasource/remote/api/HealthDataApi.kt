package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.health.HealthDataDeleteResponse
import kr.co.ongil.data.model.health.HealthDataResponse
import kr.co.ongil.data.model.health.HealthDataUploadRequest
import kr.co.ongil.data.model.health.HealthDataUploadResponse
import kr.co.ongil.data.model.health.HealthSummaryResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HealthDataApi {

    /**
     * 생체 데이터 업로드
     */
    @POST("api/v1/patients/{patientId}/health-data")
    suspend fun uploadHealthData(
        @Path("patientId") patientId: Long,
        @Body request: HealthDataUploadRequest
    ): HealthDataUploadResponse

    /**
     * 생체 데이터 조회 (일별, 시간별 등 상세)
     * GET /api/v1/patients/{patientId}/health-data
     * @param type 조회할 데이터 종류 (nullable - null이면 전체 조회)
     * @param from 조회 시작 날짜 (yyyyMMdd)
     * @param to 조회 종료 날짜 (yyyyMMdd)
     * @param sort 정렬 기준 (기본: measuredAt,desc)
     */
    @GET("api/v1/patients/{patientId}/health-data")
    suspend fun getHealthData(
        @Path("patientId") patientId: Long,
        @Query("type") type: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("sort") sort: String = "measuredAt,desc"
    ): HealthDataResponse

    /**
     * 생체 데이터 요약 통계 조회 (일별 단위)
     * GET /api/v1/patients/{patientId}/health-data/summary
     * @param type 통계할 데이터 종류 (null이면 전체)
     * @param from 조회 시작 날짜 (yyyyMMdd)
     * @param to 조회 종료 날짜 (yyyyMMdd)
     */
    @GET("api/v1/patients/{patientId}/health-data/summary")
    suspend fun getHealthDataSummary(
        @Path("patientId") patientId: Long,
        @Query("type") type: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): HealthSummaryResponse

    /**
     * 생체 데이터 삭제
     * DELETE /api/v1/patients/{patientId}/health-data/{healthDataId}
     * @param patientId 환자 ID
     * @param healthDataId 삭제할 건강 데이터 ID
     */
    @DELETE("api/v1/patients/{patientId}/health-data/{healthDataId}")
    suspend fun deleteHealthData(
        @Path("patientId") patientId: Long,
        @Path("healthDataId") healthDataId: Long
    ): HealthDataDeleteResponse
}
