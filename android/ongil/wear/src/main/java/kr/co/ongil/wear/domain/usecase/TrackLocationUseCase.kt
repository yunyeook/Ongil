package kr.co.ongil.wear.domain.usecase

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.ongil.wear.service.location.WearLocationTrackingService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 위치 추적 시작/중지 UseCase
 */
@Singleton
class TrackLocationUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 위치 추적 시작
     */
    fun startTracking() {
        val intent = Intent(context, WearLocationTrackingService::class.java).apply {
            action = WearLocationTrackingService.ACTION_START
        }
        context.startForegroundService(intent)
    }

    /**
     * 위치 추적 중지
     */
    fun stopTracking() {
        val intent = Intent(context, WearLocationTrackingService::class.java).apply {
            action = WearLocationTrackingService.ACTION_STOP
        }
        context.startService(intent)
    }
}
