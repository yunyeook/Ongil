package kr.co.ongil.presentation.ui.safezonesetting

/**
 * 안전구역 설정 화면 UI 이벤트
 */
sealed interface SafeZoneSettingUiEvent {
    // 레벨 설정 변경
    data class ChangeLevelDistance(val level: Int, val meters: Int) : SafeZoneSettingUiEvent
    data class ChangeLevelDwell(val level: Int, val minutes: Int) : SafeZoneSettingUiEvent

    // 알림 설정
    data class TogglePush(val enabled: Boolean) : SafeZoneSettingUiEvent
    data class ToggleAutoCall(val enabled: Boolean) : SafeZoneSettingUiEvent

    // 저장
    data class Save(val onSuccess: () -> Unit ): SafeZoneSettingUiEvent
}
