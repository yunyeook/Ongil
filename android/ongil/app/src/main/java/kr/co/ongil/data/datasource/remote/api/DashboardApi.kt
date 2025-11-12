package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.dashboard.DashboardResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DashboardApi {
    // 홈 화면 요약 정보 조회
    @GET("/api/v1/aggregation/mainboard/{patientId}")
    suspend fun getDashboard(
        @Path("patientId") patientId: Int
    ): DashboardResponse
}
