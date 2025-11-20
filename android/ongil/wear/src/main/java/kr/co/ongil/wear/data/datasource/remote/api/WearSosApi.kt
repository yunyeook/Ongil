package kr.co.ongil.wear.data.datasource.remote.api

import kr.co.ongil.wear.data.model.sos.SendSosAlertRequest
import kr.co.ongil.wear.data.model.sos.SendSosAlertResponse
import kr.co.ongil.wear.data.model.sos.StopSosAlertResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Wear OS용 SOS 알림 API
 */
interface WearSosApi {

    /**
     * SOS 알림 시작
     * POST /api/v1/patients/{patientId}/sos
     */
    @POST("/api/v1/patients/{patientId}/sos")
    suspend fun sendSosAlert(
        @Path("patientId") patientId: Int,
        @Body request: SendSosAlertRequest
    ): Response<SendSosAlertResponse>

    /**
     * SOS 알림 종료
     * DELETE /api/v1/patients/{patientId}/sos
     */
    @DELETE("/api/v1/patients/{patientId}/sos")
    suspend fun stopSosAlert(
        @Path("patientId") patientId: Int
    ): Response<StopSosAlertResponse>
}
