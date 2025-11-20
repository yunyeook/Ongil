package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.sosalert.AckSosAlertRequest
import kr.co.ongil.data.model.sosalert.AckSosAlertResponse
import kr.co.ongil.data.model.sosalert.SendSosAlertRequest
import kr.co.ongil.data.model.sosalert.SendSosAlertResponse
import kr.co.ongil.data.model.sosalert.StopSosAlertResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

// SOS 알림 API
interface SosAlertApi {

    // SOS 시작
    @POST("/api/v1/patients/{patientId}/sos")
    suspend fun sendSosAlert(
        @Path("patientId") patientId: Int,
        @Body request: SendSosAlertRequest
    ): Response<SendSosAlertResponse>

    // SOS 종료
    @DELETE("/api/v1/patients/{patientId}/sos")
    suspend fun stopSosAlert(
        @Path("patientId") patientId: Int
    ): Response<StopSosAlertResponse>

    // SOS 재생 완료 ACK
    @POST("/api/v1/patients/sos/{sosId}/ack")
    suspend fun ackSosAlert(
        @Path("sosId") sosId: Long,
        @Body request: AckSosAlertRequest
    ): Response<AckSosAlertResponse>
}
