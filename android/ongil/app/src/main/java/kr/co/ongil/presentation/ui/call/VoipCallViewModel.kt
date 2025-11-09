package kr.co.ongil.presentation.ui.call

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.ongil.data.model.call.*
import kr.co.ongil.domain.repository.CallRepository
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.common.location.LocationPoint
import java.time.Instant

@HiltViewModel
class VoipCallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val locationStreamBus: LocationStreamBus,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoipCallUiState())
    val uiState: StateFlow<VoipCallUiState> = _uiState.asStateFlow()

    private var currentCall: VoipCallDto? = null

    init {
        // 위치 정보 실시간 업데이트
        viewModelScope.launch {
            locationStreamBus.updates.collect { point ->
                _uiState.update {
                    it.copy(
                        currentLocation = "위도: ${point.latitude}, 경도: ${point.longitude}, 정확도: ${point.accuracyMeters}m"
                    )
                }
            }
        }
    }

    /** 📞 발신 */
    fun startVoipCall(receiverId: Long, userType: String, callType: String = "NORMAL") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            callRepository.createVoipCall(receiverId, callType)
                .onSuccess { call ->
                    currentCall = call
                    _uiState.update {
                        it.copy(isLoading = false, call = call, message = "통화 요청 완료")
                    }
                    if(userType == "PATIENT"){
                        sendStartLocationOnce(call.id)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /** 📲 수신자 */
    fun loadIncomingCall(callId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            callRepository.getVoipCall(callId)
                .onSuccess { call ->
                    currentCall = call
                    _uiState.update { it.copy(isLoading = false, call = call) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /** 통화 수락 */
    fun acceptCall(userType: String) {
        val callId = currentCall?.id ?: return
        viewModelScope.launch {
            callRepository.updateVoipCallStatus(callId, "CONNECTED")
                .onSuccess { updated ->
                    currentCall = updated
                    _uiState.update { it.copy(call = updated, message = "통화 연결됨") }

                    // 환자면 위치 한 번 전송
                    if (userType == "PATIENT") sendStartLocationOnce(callId)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    /** 통화 종료 */
    fun endCall() {
        val callId = currentCall?.id ?: return
        viewModelScope.launch {
            callRepository.updateVoipCallStatus(callId, "ENDED")
                .onSuccess {
                    _uiState.update { it.copy(message = "통화 종료됨") }
                }
        }
    }

    /** 현재 위치 가져오기 (테스트용) */
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            // 위치 권한 확인
            if (!hasLocationPermission()) {
                _uiState.update {
                    it.copy(error = "⚠️ 위치 권한이 필요합니다. 앱 설정에서 위치 권한을 허용해주세요.")
                }
                android.util.Log.w("VoipCallViewModel", "위치 권한 없음")
                return@launch
            }

            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(application)
                val lastLocation = fusedClient.lastLocation.await()

                if (lastLocation != null) {
                    val point = LocationPoint(
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude,
                        accuracyMeters = lastLocation.accuracy,
                        bearing = lastLocation.bearing,
                        speedMps = lastLocation.speed,
                        timeMillis = lastLocation.time
                    )
                    _uiState.update {
                        it.copy(
                            currentLocation = "위도: ${point.latitude}, 경도: ${point.longitude}, 정확도: ${point.accuracyMeters}m",
                            message = "✓ 위치 가져오기 성공"
                        )
                    }
                    android.util.Log.d("VoipCallViewModel", "위치 가져오기 성공: ${point.latitude}, ${point.longitude}")
                } else {
                    _uiState.update { it.copy(error = "위치 정보가 없습니다. GPS를 켜고 잠시 기다려주세요.") }
                    android.util.Log.w("VoipCallViewModel", "lastLocation이 null")
                }
            } catch (e: SecurityException) {
                _uiState.update { it.copy(error = "⚠️ 위치 권한이 거부되었습니다: ${e.message}") }
                android.util.Log.e("VoipCallViewModel", "위치 권한 없음", e)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "위치 가져오기 실패: ${e.message}") }
                android.util.Log.e("VoipCallViewModel", "위치 가져오기 실패", e)
            }
        }
    }

    /** 위치 권한 확인 */
    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /** 위치 한 번 전송 */
    private fun sendStartLocationOnce(callId: Long) {
        viewModelScope.launch {
            // 위치 권한 확인
            if (!hasLocationPermission()) {
                _uiState.update {
                    it.copy(error = "⚠️ 위치 권한이 필요합니다. 앱 설정에서 위치 권한을 허용해주세요.")
                }
                android.util.Log.w("VoipCallViewModel", "위치 권한 없음")
                return@launch
            }

            // 1. LocationStreamBus에서 먼저 시도
            var point = locationStreamBus.lastValue

            // 2. LocationStreamBus에 없으면 FusedLocation에서 직접 가져오기
            if (point == null) {
                android.util.Log.w("VoipCallViewModel", "LocationStreamBus에 위치 없음. FusedLocation에서 가져오는 중...")
                try {
                    val fusedClient = LocationServices.getFusedLocationProviderClient(application)
                    val lastLocation = fusedClient.lastLocation.await()

                    if (lastLocation != null) {
                        point = LocationPoint(
                            latitude = lastLocation.latitude,
                            longitude = lastLocation.longitude,
                            accuracyMeters = lastLocation.accuracy,
                            bearing = lastLocation.bearing,
                            speedMps = lastLocation.speed,
                            timeMillis = lastLocation.time
                        )
                        android.util.Log.d("VoipCallViewModel", "FusedLocation에서 위치 가져옴: ${point.latitude}, ${point.longitude}")

                        // UI 상태 업데이트
                        _uiState.update {
                            it.copy(currentLocation = "위도: ${point.latitude}, 경도: ${point.longitude}, 정확도: ${point.accuracyMeters}m")
                        }
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e("VoipCallViewModel", "위치 권한 없음", e)
                } catch (e: Exception) {
                    android.util.Log.e("VoipCallViewModel", "FusedLocation에서 위치 가져오기 실패", e)
                }
            }

            if (point == null) {
                _uiState.update { it.copy(error = "⚠️ 위치 정보를 가져올 수 없습니다. 위치 권한과 GPS를 확인하세요.") }
                android.util.Log.w("VoipCallViewModel", "위치 정보가 없어 전송 실패")
                return@launch
            }

            val request = CallStartLocationRequest(
                latitude = point.latitude,
                longitude = point.longitude,
                source = "GPS",
                accuracy = point.accuracyMeters?.toDouble(),
                timestamp = Instant.ofEpochMilli(point.timeMillis).toString()
            )

            android.util.Log.d("VoipCallViewModel", "위치 전송: lat=${point.latitude}, lng=${point.longitude}")

            callRepository.sendVoipCallStartLocation(callId, request)
                .onSuccess {
                    _uiState.update { it.copy(message = "✓ 위치 전송 완료 (${point.latitude}, ${point.longitude})") }
                    android.util.Log.i("VoipCallViewModel", "위치 전송 성공")
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "✗ 위치 전송 실패: ${e.message}") }
                    android.util.Log.e("VoipCallViewModel", "위치 전송 실패", e)
                }
        }
    }
}