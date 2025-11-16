package kr.co.ongil.wear.data.repository

import android.util.Log
import kr.co.ongil.wear.data.datasource.remote.api.WearCallApi
import kr.co.ongil.wear.data.model.call.CallCreateRequest
import kr.co.ongil.wear.data.model.call.CallStatusUpdateRequest
import kr.co.ongil.wear.domain.model.CallState
import kr.co.ongil.wear.domain.model.CallStatus
import kr.co.ongil.wear.domain.model.TurnCredentials
import kr.co.ongil.wear.domain.repository.CallRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor(
    private val callApi: WearCallApi
) : CallRepository {

    companion object {
        private const val TAG = "CallRepositoryImpl"
    }

    override suspend fun createCall(
        targetUserId: String,
        targetName: String,
        targetPhone: String
    ): Result<Long> {
        return try {
            val response = callApi.createVoipCall(
                body = CallCreateRequest(
                    targetUserId = targetUserId,
                    targetName = targetName,
                    targetPhone = targetPhone
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val callId = response.body()!!.data.id
                Log.d(TAG, "Call created: $callId")
                Result.success(callId)
            } else {
                Log.e(TAG, "Failed to create call: ${response.code()}")
                Result.failure(Exception("Failed to create call: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating call", e)
            Result.failure(e)
        }
    }

    override suspend fun updateCallStatus(
        callId: Long,
        status: String
    ): Result<Unit> {
        return try {
            val response = callApi.updateVoipCallStatus(
                callId = callId,
                body = CallStatusUpdateRequest(status = status)
            )

            if (response.isSuccessful) {
                Log.d(TAG, "Call status updated: $callId -> $status")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to update call status: ${response.code()}")
                Result.failure(Exception("Failed to update call status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating call status", e)
            Result.failure(e)
        }
    }

    override suspend fun getCall(callId: Long): Result<CallState> {
        return try {
            val response = callApi.getVoipCall(callId)

            if (response.isSuccessful && response.body() != null) {
                val call = response.body()!!.data
                val callState = CallState(
                    callId = call.id,
                    targetUserId = call.calleeUserId,
                    targetName = call.calleeName,
                    status = when (call.status) {
                        "CALLING" -> CallStatus.CALLING
                        "RINGING" -> CallStatus.RINGING
                        "CONNECTING" -> CallStatus.CONNECTING
                        "CONNECTED" -> CallStatus.CONNECTED
                        "ENDED" -> CallStatus.ENDED
                        "FAILED" -> CallStatus.FAILED
                        else -> CallStatus.IDLE
                    }
                )
                Result.success(callState)
            } else {
                Log.e(TAG, "Failed to get call: ${response.code()}")
                Result.failure(Exception("Failed to get call: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting call", e)
            Result.failure(e)
        }
    }

    override suspend fun getTurnCredentials(): Result<TurnCredentials> {
        return try {
            val response = callApi.getTurnCredentials()

            if (response.isSuccessful && response.body() != null) {
                val cred = response.body()!!.data
                val turnCredentials = TurnCredentials(
                    urls = cred.urls,
                    username = cred.username,
                    credential = cred.credential
                )
                Log.d(TAG, "TURN credentials retrieved")
                Result.success(turnCredentials)
            } else {
                Log.e(TAG, "Failed to get TURN credentials: ${response.code()}")
                Result.failure(Exception("Failed to get TURN credentials: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting TURN credentials", e)
            Result.failure(e)
        }
    }
}
