package kr.co.ongil.common.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 안전범위 표시 상태를 전역적으로 관리하는 Singleton 클래스
 * 홈 화면과 위치 화면의 지도에서 동일한 토글 상태를 공유
 */
@Singleton
class SafetyZoneStateManager @Inject constructor() {

    private val _showSafetyZones = MutableStateFlow(false)
    val showSafetyZones: StateFlow<Boolean> = _showSafetyZones.asStateFlow()

    /**
     * 안전범위 표시 상태 토글
     */
    fun toggleSafetyZones() {
        _showSafetyZones.value = !_showSafetyZones.value
    }

    /**
     * 안전범위 표시 상태 설정
     */
    fun setSafetyZonesVisible(visible: Boolean) {
        _showSafetyZones.value = visible
    }

    /**
     * 현재 안전범위 표시 상태 가져오기
     */
    fun isShowingSafetyZones(): Boolean = _showSafetyZones.value
}