package kr.co.ongil.presentation.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat

import kr.co.ongil.domain.model.PlaceDetail
import kr.co.ongil.domain.model.Route

/**
 * 지도 위 플로팅 패널. 장소 상세 정보와 길안내 정보를 모두 표시.
 * @param isNavigating '길안내 중' 모드인지 여부.
 */
@Composable
fun MapFloatingPanel(
    modifier: Modifier = Modifier,
    placeDetail: PlaceDetail? = null,
    route: Route? = null,
    isNavigating: Boolean = false,
    onDismiss: () -> Unit,
    onSetDestinationClick: () -> Unit,
    onCallClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onStopNavigationClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDialog = false },
            onConfirm = {
                showDialog = false
                onSetDestinationClick()
            }
        )
    }

    // Box를 사용하여 화면 하단에 컴포넌트를 배치합니다.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = {
                    // 바깥 클릭 시 모달 닫기 (장소 상세 또는 길찾기 모달)
                    onDismiss()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 메인 패널 카드
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 100.dp) // 하단 네비게이션 바 위로 오도록 패딩 조절
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF8A9A8A).copy(alpha = 0.9f)) // 이미지의 녹색 계열, 약간 투명하게
                .clickable(
                    onClick = { /* 패널 내부 클릭 시 이벤트 전파 방지 */ },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(16.dp)
        ) {
            if (isNavigating) {
                // 길안내 중 UI
                route?.let {
                    NavigatingContent(
                        route = it,
                        onDismiss = onDismiss,
                        onStopNavigationClick = onStopNavigationClick
                    )
                }
            } else {
                // 장소 상세 정보 UI
                placeDetail?.let {
                    PlaceDetailContent(
                        placeDetail = it,
                        onShowDialog = { showDialog = true },
                        onCallClick = onCallClick,
                        onFavoriteClick = onFavoriteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceDetailContent(
    placeDetail: PlaceDetail,
    onShowDialog: () -> Unit,
    onCallClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val context = LocalContext.current
    var showCallDialog by remember { mutableStateOf(false) }

    // 전화 권한 요청 런처
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted && !placeDetail.phoneNumber.isNullOrEmpty()) {
                // 권한이 승인되면 통화 로그 기록 후 전화 걸기
                onCallClick()
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:${placeDetail.phoneNumber}")
                }
                context.startActivity(intent)
            } else {
                // 권한 거부 시 토스트 표시
                Toast.makeText(context, "전화 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // 전화 확인 다이얼로그
    if (showCallDialog && !placeDetail.phoneNumber.isNullOrEmpty()) {
        CallConfirmationDialog(
            phoneNumber = placeDetail.phoneNumber!!,
            onDismissRequest = { showCallDialog = false },
            onConfirm = {
                showCallDialog = false

                // 권한 체크
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    // 권한이 있으면 통화 로그 기록 후 전화 걸기
                    onCallClick()
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:${placeDetail.phoneNumber}")
                    }
                    context.startActivity(intent)
                } else {
                    // 권한이 없으면 권한 요청 (승인 시 callPermissionLauncher에서 로그 기록 & 전화 걸기)
                    callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                }
            }
        )
    }

    // 상단 정보 섹션 (장소명, 주소, 즐겨찾기)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 장소명, 주소
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = placeDetail.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = placeDetail.roadAddress ?: placeDetail.jibunAddress ?: "주소 정보 없음",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 즐겨찾기 버튼
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (placeDetail.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (placeDetail.isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                tint = if (placeDetail.isFavorite) Color(0xFFFFD700) else Color.White, // 금색 또는 흰색
                modifier = Modifier.size(28.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 중간 버튼 섹션
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FloatingButton(
            text = "목적지로 설정",
            icon = Icons.Default.Send,
            onClick = onShowDialog,
            modifier = Modifier.weight(1f)
        )

        // 전화번호가 있을 때만 버튼 표시
        if (!placeDetail.phoneNumber.isNullOrEmpty()) {
            FloatingButton(
                text = placeDetail.phoneNumber,
                icon = Icons.Default.Call,
                onClick = { showCallDialog = true }, // 시스템 전화 다이얼로그 띄우기
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 길안내 중 UI
 */
@Composable
private fun NavigatingContent(
    route: Route,
    onDismiss: () -> Unit,
    onStopNavigationClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // X 아이콘 버튼
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "목적지까지",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // 안내중지 버튼
        SmallFloatingButton(text = "안내중지", onClick = onStopNavigationClick)
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Schedule, contentDescription = "시간", tint = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${route.totalTimeMinutes}분",
            color = Color.White,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = "거리", tint = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "도보 ${route.totalDistanceMeters}m",
            color = Color.White,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(imageVector = Icons.Default.Send, contentDescription = "경로", tint = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "최단경로", color = Color.White, fontSize = 14.sp)
    }
}

/**
 * 길찾기 확인 다이얼로그
 */
@Composable
private fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "해당 목적지로\n길찾기를 시작하시겠습니까?",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8A9A8A),
                            contentColor = Color.White
                        )
                    ) {
                        Text("확인")
                    }
                }
            }
        }
    }
}

/**
 * 전화 확인 다이얼로그
 */
@Composable
private fun CallConfirmationDialog(
    phoneNumber: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "다음 번호로 전화하시겠습니까?\n$phoneNumber",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8A9A8A),
                            contentColor = Color.White
                        )
                    ) {
                        Text("전화")
                    }
                }
            }
        }
    }
}

/**
 * 플로팅 패널 내부의 공용 버튼 컴포넌트
 */
@Composable
private fun FloatingButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.2f),
            contentColor = Color.White
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * 길안내 중 사용하는 작은 버튼
 */
@Composable
private fun SmallFloatingButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.2f),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(text = text, fontSize = 13.sp)
    }
}