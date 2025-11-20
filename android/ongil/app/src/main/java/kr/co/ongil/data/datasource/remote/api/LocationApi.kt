package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.call.ApiResponse
import kr.co.ongil.data.model.map.CoordinateDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LocationApi {

    // ✅ 이 메서드만 추가
    @GET("api/v1/patients/{patientId}/location")
    suspend fun getPatientLocation(
        @Path("patientId") patientId: Long
    ): ApiResponse<CoordinateDto>
}