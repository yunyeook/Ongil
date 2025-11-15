package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.insight.PatientInsightResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PatientInsightApi {
    // 최신 주간 인사이트 조회
    @GET("/api/v1/patients/{patientId}/insights/weekly/latest")
    suspend fun getLatestWeeklyInsight(
        @Path("patientId") patientId: Int
    ): PatientInsightResponse
}
