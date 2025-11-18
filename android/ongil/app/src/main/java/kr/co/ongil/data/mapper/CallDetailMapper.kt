package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.call.CallDetailDto
import kr.co.ongil.presentation.uistate.CallSummary
import kr.co.ongil.presentation.uistate.ChatMessage
import kr.co.ongil.presentation.uistate.ContactInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * CallDetailDto를 UI 모델로 변환
 */
fun CallDetailDto.toContactInfo(currentUserId: Long): ContactInfo {
    // 발신자/수신자 이름 또는 번호 (실제로는 User API에서 가져와야 함)
    val (name, initials) = if (senderId == currentUserId) {
        "수신자 $receiverId" to "수${receiverId.toString().takeLast(1)}"
    } else {
        "발신자 $senderId" to "발${senderId.toString().takeLast(1)}"
    }

    // Badge 텍스트
    val badge = when (callType.uppercase()) {
        "VOIP" -> "VoIP"
        "BASIC" -> "일반전화"
        else -> null
    }

    return ContactInfo(
        initials = initials,
        name = name,
        phone = "010-XXXX-XXXX", // TODO: User 정보로 교체
        badge = badge,
        isEmergency = false // TODO: 긴급 연락처 여부 판단 로직
    )
}

fun CallDetailDto.toCallSummary(currentUserId: Long): CallSummary {
    // 발신/수신 판단
    val direction = if (senderId == currentUserId) {
        "발신 통화"
    } else {
        "수신 통화"
    }

    // 통화 시간 포맷팅
    val durationString = formatDuration(duration)

    // 위치 포맷팅
    val locationString = location?.let {
        "위도: ${it.latitude}, 경도: ${it.longitude}" // TODO: 주소 변환
    } ?: "위치 정보 없음"

    // 날짜/시간 파싱
    val startDateTime = try {
        LocalDateTime.parse(startedAt, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: Exception) {
        null
    }

    val dateString = startDateTime?.let {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
        it.format(formatter)
    } ?: "날짜 정보 없음"

    val startTimeString = startDateTime?.let {
        val formatter = DateTimeFormatter.ofPattern("a h:mm:ss")
        it.format(formatter)
    } ?: "시간 정보 없음"

    val endDateTime = try {
        LocalDateTime.parse(endedAt, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: Exception) {
        null
    }

    val endTimeString = endDateTime?.let {
        val formatter = DateTimeFormatter.ofPattern("a h:mm:ss")
        it.format(formatter)
    } ?: "시간 정보 없음"

    return CallSummary(
        direction = direction,
        duration = durationString,
        location = locationString,
        date = dateString,
        startTime = startTimeString,
        endTime = endTimeString
    )
}

/**
 * 통화 시간 포맷팅
 * 예: "5분 12초", "20초", "1시간 5분"
 */
private fun formatDuration(seconds: Int): String {
    if (seconds == 0) return "부재중"

    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return buildString {
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

/**
 * Mock 채팅 메시지 생성 (실제로는 별도 API에서 가져와야 함)
 */
fun CallDetailDto.toMockChatMessages(): List<ChatMessage> {
    // TODO: 실제 통화 내용 API 연동
    return listOf(
        ChatMessage("CareLink 응급상담센터입니다. 어떤 도움이 필요하신가요?", "15:24", isMine = false),
        ChatMessage("아, 안녕하세요. 갑자기 가슴이 너무 아파서… 숨쉬기도 힘들어요.", "15:24", isMine = true),
        ChatMessage("네, 상황을 파악하겠습니다. 현재 몇 살이시고, 기존에 앓고 계신 질병이 있으신가요?", "15:24", isMine = false),
        ChatMessage("57세이고… 고혈압 약을 먹고 있어요. 가슴 가운데가 꽉 조이는 것 같아요.", "15:25", isMine = true),
        ChatMessage("알겠습니다. 지금 계시는 위치를 알려주시고, 즉시 119에 연계해드리겠습니다.", "15:25", isMine = false),
        ChatMessage("서울시 강남구 역삼동이에요… 제발 빨리 도와주세요.", "15:26", isMine = true)
    )
}
