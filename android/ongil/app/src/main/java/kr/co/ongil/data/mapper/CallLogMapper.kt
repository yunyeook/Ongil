package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.call.CallLogDto
import kr.co.ongil.presentation.uistate.CallType
import kr.co.ongil.presentation.uistate.RecentCallUi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * CallLogDto를 RecentCallUi로 변환
 */
fun CallLogDto.toRecentCallUi(currentUserId: Long): RecentCallUi {
    // 상대방 정보 추출
    val otherUser = if (caller.id == currentUserId) {
        receiver
    } else {
        caller
    }

    // CallType 매핑
    // callType: NORMAL/EMERGENCY, source: APP(VoIP)/SYSTEM_DIALER(일반전화)
    val uiCallType = when {
        callType.uppercase() == "EMERGENCY" -> CallType.EMERGENCY
        source.uppercase() == "APP" -> CallType.VOIP
        source.uppercase() == "SYSTEM_DIALER" -> CallType.NORMAL
        callType.uppercase() == "VOIP" -> CallType.VOIP  // 기존 스펙 호환
        else -> CallType.NORMAL
    }

    // Subtitle 생성
    val subtitle = formatSubtitle(startedAt, duration ?: 0)

    return RecentCallUi(
        id = id,
        nameOrNumber = otherUser.name,
        type = uiCallType,
        subtitle = subtitle,
        receiverId = otherUser.id,
        receiverPhone = otherUser.phoneNumber
    )
}

/**
 * Subtitle 포맷팅
 * 예: "오늘 오후 3:24 · 통화시간 5분 12초"
 */
private fun formatSubtitle(startedAt: String, duration: Int): String {
    return try {
        val dateTime = LocalDateTime.parse(startedAt, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now()

        // 날짜 부분 (오늘, 어제, 날짜)
        val datePrefix = when {
            dateTime.toLocalDate() == now.toLocalDate() -> "오늘"
            dateTime.toLocalDate() == now.minusDays(1).toLocalDate() -> "어제"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("M월 d일")
                dateTime.format(formatter)
            }
        }

        // 시간 부분 (오전/오후 h:mm)
        val timeFormatter = DateTimeFormatter.ofPattern("a h:mm")
        val timeString = dateTime.format(timeFormatter)

        // 통화 시간 포맷팅
        val durationString = formatDuration(duration)

        "$datePrefix $timeString · $durationString"
    } catch (e: Exception) {
        // 파싱 실패 시 기본값
        "통화시간 ${formatDuration(duration)}"
    }
}

/**
 * 통화 시간 포맷팅
 * 예: "5분 12초", "20초", "1시간 5분"
 */
private fun formatDuration(seconds: Int): String {
    if (seconds == 0) return "통화 실패"

    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return buildString {
        append("통화시간 ")
        if (hours > 0) {
            append("${hours}시간 ")
        }
        if (minutes > 0) {
            append("${minutes}분 ")
        }
        if (secs > 0 || (hours == 0 && minutes == 0)) {
            append("${secs}초")
        }
    }.trim()
}
