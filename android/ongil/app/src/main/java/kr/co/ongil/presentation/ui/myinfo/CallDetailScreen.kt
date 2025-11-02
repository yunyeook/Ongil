package kr.co.ongil.presentation.ui.myinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.co.ongil.presentation.uistate.*
import kr.co.ongil.presentation.viewmodel.CallDetailViewModel

private val Accent = Color(0xFF8CA898)
private val Danger = Color(0xFFD85B4E)
private val ChipBg = Color(0xFFEFF4F2)
private val SurfaceBg = Color(0xFFF8F9FA)
private val TextPrimary = Color(0xFF222B2E)
private val TextSecondary = Color(0xFF6B767A)
private val DividerColor = Color(0xFFE9ECEF)

/**
 * 통화 상세정보 화면 (ViewModel 기반)
 */
@Composable
fun CallDetailScreen(
    callLogId: Long,
    modifier: Modifier = Modifier,
    viewModel: CallDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(callLogId) {
        viewModel.onEvent(CallDetailEvent.LoadCallDetail(callLogId))
    }

    CallDetailContent(
        contactInfo = uiState.contactInfo,
        callSummary = uiState.callSummary,
        messages = uiState.messages,
        isLoading = uiState.isLoading,
        error = uiState.error,
        modifier = modifier
    )
}

/**
 * 통화 상세정보 화면 컨텐츠 (Stateless)
 */
@Composable
private fun CallDetailContent(
    contactInfo: ContactInfo?,
    callSummary: CallSummary?,
    messages: List<ChatMessage>,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 로딩 상태
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Accent)
            }
            return@Column
        }

        // 에러 상태
        error?.let { errorMessage ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            return@Column
        }

        // 데이터가 있을 때만 표시
        contactInfo?.let { contact ->
            ContactHeaderCard(
                initials = contact.initials,
                name = contact.name,
                phone = contact.phone,
                rightBadge = contact.badge,
                isEmergency = contact.isEmergency
            )
            Spacer(Modifier.height(16.dp))
        }

        callSummary?.let { summary ->
            CallSummaryCard(
                direction = summary.direction,
                duration = summary.duration,
                location = summary.location ?: "",
                date = summary.date,
                start = summary.startTime,
                end = summary.endTime
            )
            Spacer(Modifier.height(16.dp))
        }

        if (messages.isNotEmpty()) {
            ChatSection(messages = messages)
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ----------------------------- Components ------------------------------ */

@Composable
private fun ContactHeaderCard(
    initials: String,
    name: String,
    phone: String,
    rightBadge: String?,
    isEmergency: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEBF0EF),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // 아바타
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5C7165)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    rightBadge?.let {
                        SmallBadge(text = it)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                if (isEmergency) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "긴급 연락처",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Danger,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFDBEAFE))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun CallSummaryCard(
    direction: String,
    duration: String,
    location: String,
    date: String,
    start: String,
    end: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = "통화",
                        tint = Color(0xFF1F9A54),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = direction,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                }
                Text(
                    text = duration,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            InfoRow(icon = Icons.Outlined.LocationOn, iconTint = Color(0xFF1F9A54), label = "환자 위치", value = location)
            InfoRow(icon = Icons.Outlined.Event, iconTint = Color(0xFF9333EA), label = "통화일시", value = date, top = 14.dp)
            InfoRow(icon = Icons.Outlined.PlayArrow, iconTint = Color(0xFF2563EB), label = "시작 시간", value = start, top = 14.dp)
            InfoRow(icon = Icons.Outlined.Stop, iconTint = Color(0xFFDC2626), label = "종료 시간", value = end, top = 14.dp)
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    top: Dp = 0.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = top)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
            )
        }
    }
}

@Composable
private fun ChatSection(messages: List<ChatMessage>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp, top = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Message,
            contentDescription = "통화 내용",
            tint = Color(0xFF1F9A54),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "통화 내용",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            messages.forEach { msg ->
                ChatBubble(
                    text = msg.text,
                    time = msg.time,
                    mine = msg.isMine
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    time: String,
    mine: Boolean
) {
    val bg = if (mine) Color(0xFFEAF6F0) else Color(0xFFF4F6F8)
    val align = if (mine) Alignment.End else Alignment.Start
    val corner = if (mine)
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    else
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)

    Column(
        horizontalAlignment = align,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(corner)
                .background(bg)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                lineHeight = 20.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
            textAlign = if (mine) TextAlign.End else TextAlign.Start,
            modifier = Modifier
                .widthIn(min = 80.dp)
        )
    }
}

/* ----------------------------- Preview ------------------------------ */

private fun sampleMessages() = listOf(
    ChatMessage("CareLink 응급상담센터입니다. 어떤 도움이 필요하신가요?", "15:24", isMine = false),
    ChatMessage("아, 안녕하세요. 갑자기 가슴이 너무 아파서… 숨쉬기도 힘들어요.", "15:24", isMine = true),
    ChatMessage("네, 상황을 파악하겠습니다. 현재 몇 살이시고, 기존에 앓고 계신 질병이 있으신가요?", "15:24", isMine = false),
    ChatMessage("57세이고… 고혈압 약을 먹고 있어요. 가슴 가운데가 꽉 조이는 것 같아요.", "15:25", isMine = true),
    ChatMessage("알겠습니다. 지금 계시는 위치를 알려주시고, 즉시 119에 연계해드리겠습니다.", "15:25", isMine = false),
    ChatMessage("서울시 강남구 역삼동이에요… 제발 빨리 도와주세요.", "15:26", isMine = true),
)

@Preview(
    name = "Call Detail",
    showBackground = true
)
@Composable
private fun PreviewCallDetailScreen() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val sampleContactInfo = ContactInfo(
            initials = "이지",
            name = "이지현",
            phone = "010-1234-5678",
            badge = "VoIP",
            isEmergency = true
        )

        val sampleCallSummary = CallSummary(
            direction = "발신 통화",
            duration = "5분 12초",
            location = "서울시 강남구 역삼동",
            date = "2024년 10월 28일",
            startTime = "오후 3:24:15",
            endTime = "오후 3:29:27"
        )

        CallDetailContent(
            contactInfo = sampleContactInfo,
            callSummary = sampleCallSummary,
            messages = sampleMessages(),
            isLoading = false,
            error = null
        )
    }
}