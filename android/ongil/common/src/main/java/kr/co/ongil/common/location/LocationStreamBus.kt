package kr.co.ongil.common.location

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// 앱 전역 위치 스트림버스임
// emit/tryEmit으로 좌표 전송
class LocationStreamBus {
    private val _updates = MutableSharedFlow<LocationPoint>(
        replay = 1,
        extraBufferCapacity = 16
    )
    val updates: SharedFlow<LocationPoint> = _updates.asSharedFlow()

    @Volatile
    private var last: LocationPoint? = null
    val lastValue: LocationPoint? get() = last


    suspend fun emit(point: LocationPoint) {
        last = point
        _updates.emit(point)
    }

    // 즉시 전송 시도, 버펴 여유 없으면 폴스임
    fun tryEmit(point: LocationPoint): Boolean {
        val ok = _updates.tryEmit(point)
        if (ok) last = point
        return ok
    }
}