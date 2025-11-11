package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.patientinfo.PatientInfoResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PatientInfoApi {
    // 환자 정보 조회
    @GET("/api/v1/aggregation/patientinfo/{patientId}")
    suspend fun getPatientInfo(
        @Path("patientId") patientId: Int
    ): PatientInfoResponse
}
