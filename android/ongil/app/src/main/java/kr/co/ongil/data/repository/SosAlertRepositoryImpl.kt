package kr.co.ongil.data.repository

import android.util.Log
import kr.co.ongil.data.datasource.remote.api.SosAlertApi
import kr.co.ongil.data.model.sosalert.AckSosAlertRequest
import kr.co.ongil.data.model.sosalert.SendSosAlertRequest
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.SosAlertRepository
import javax.inject.Inject

class SosAlertRepositoryImpl @Inject constructor(
    private val sosAlertApi: SosAlertApi
) : SosAlertRepository {

    override suspend fun sendSosAlert(patientId: Int, message: String?): Result<Unit> {
        return try {
            Log.d("SosAlertRepository", "SOS 알림 시작 - patientId: $patientId")

            val response = sosAlertApi.sendSosAlert(
                patientId = patientId,
                request = SendSosAlertRequest(message = message)
            )

            Log.d("SosAlertRepository", "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d("SosAlertRepository", "SOS 알림 시작 성공")
                Result.success(Unit)
            } else {
                val exception = ErrorHandler.handleException(
                    retrofit2.HttpException(response)
                )
                Log.e("SosAlertRepository", "SOS 알림 시작 실패: ${exception.message}")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Log.e("SosAlertRepository", "SOS 알림 시작 예외 발생", e)
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun stopSosAlert(patientId: Int): Result<Unit> {
        return try {
            Log.d("SosAlertRepository", "SOS 알림 종료 - patientId: $patientId")

            val response = sosAlertApi.stopSosAlert(patientId = patientId)

            Log.d("SosAlertRepository", "응답 코드: ${response.code()}")

            if (response.isSuccessful) {
                Log.d("SosAlertRepository", "SOS 알림 종료 성공")
                Result.success(Unit)
            } else {
                val exception = ErrorHandler.handleException(
                    retrofit2.HttpException(response)
                )
                Log.e("SosAlertRepository", "SOS 알림 종료 실패: ${exception.message}")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Log.e("SosAlertRepository", "SOS 알림 종료 예외 발생", e)
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun ackSosAlert(sosId: Long): Result<Unit> {
        return try {
            Log.d("SosAlertRepository", "SOS 재생 완료 ACK - sosId: $sosId")

            val response = sosAlertApi.ackSosAlert(
                sosId = sosId,
                request = AckSosAlertRequest()
            )

            Log.d("SosAlertRepository", "응답 코드: ${response.code()}")

            if (response.isSuccessful) {
                Log.d("SosAlertRepository", "SOS 재생 완료 ACK 성공")
                Result.success(Unit)
            } else {
                val exception = ErrorHandler.handleException(
                    retrofit2.HttpException(response)
                )
                Log.e("SosAlertRepository", "SOS 재생 완료 ACK 실패: ${exception.message}")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Log.e("SosAlertRepository", "SOS 재생 완료 ACK 예외 발생", e)
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}
