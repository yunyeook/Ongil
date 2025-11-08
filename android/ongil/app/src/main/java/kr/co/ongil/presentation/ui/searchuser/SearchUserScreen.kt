package kr.co.ongil.presentation.ui.searchuser

import kr.co.ongil.presentation.navigation.Routes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kr.co.ongil.presentation.ui.common.InputBox
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.GreyButton
import kr.co.ongil.presentation.ui.common.AlertModal
import kr.co.ongil.domain.model.UserSummary
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.border

@Composable
fun SearchUserScreen(
    navController: NavController,
    viewModel: SearchUserViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 단발성 사이드이펙트 처리 (토스트 / 네비게이션 / 초기화 등)
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SearchUserUiSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SearchUserUiSideEffect.ShowSnackBar -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SearchUserUiSideEffect.NavigateToRegisterUser -> {
                    // TODO: navController.navigate(...) 실제 라우트 연결
                }
                SearchUserUiSideEffect.BackToSearchStart -> {
                    // 필요 시 스크롤 초기화 등 추가 가능
                }
            }
        }
    }

    // 뒤로가기 눌렀을 때 동작 전략
    val backPress: () -> Unit = {
        when (uiState.mode) {
            SearchUserMode.SEARCH_INPUT -> {
                navController.popBackStack()
            }
            SearchUserMode.REGISTER_DONE -> {
                // 완료 모달은 AlertModal 내부 액션으로만 닫도록 유지
            }
            else -> {
                viewModel.onEvent(SearchUserUiEvent.OnClickBackToSearch)
            }
        }
    }

    SearchUserScreenContent(
        uiState = uiState,
        onEvent = { evt -> viewModel.onEvent(evt) },
        onGoHome = {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        },
    )
}

@Composable
private fun SearchUserScreenContent(
    uiState: SearchUserUiState,
    onEvent: (SearchUserUiEvent) -> Unit,
    onGoHome: () -> Unit,
) {
    Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
        ) {
            when (uiState.mode) {
                SearchUserMode.SEARCH_INPUT -> {
                    SearchInputSection(
                        phone = uiState.phoneInput,
                        isSearching = uiState.isSearching,
                        isPhoneValid = uiState.isPhoneValid,
                        onPhoneChange = {
                            onEvent(SearchUserUiEvent.OnPhoneInputChange(it))
                        },
                        onSearchClick = {
                            onEvent(SearchUserUiEvent.OnClickSearch)
                        }
                    )
                }

                SearchUserMode.SEARCH_SUCCESS -> {
                    SearchSuccessSection(
                        foundUser = uiState.foundUser,
                        onAddFriend = {
                            onEvent(SearchUserUiEvent.OnClickAddFriend)
                        },
                        onBackToSearch = {
                            onEvent(SearchUserUiEvent.OnClickBackToSearch)
                        }
                    )
                }

                SearchUserMode.SEARCH_NOT_FOUND -> {
                    SearchNotFoundSection(
                        errorMessage = uiState.searchErrorMessage,
                        onRetry = {
                            onEvent(SearchUserUiEvent.OnClickRetrySearch)
                        }
                    )
                }

                SearchUserMode.REGISTER -> {
                    RegisterSection(
                        foundUserPhone = uiState.foundUser?.phoneNumber ?: "",
                        name = uiState.relationshipName,
                        relationshipType = uiState.relationshipType,
                        relationshipTypeOptions = uiState.relationshipTypeOptions,
                        isCodeRequested = uiState.isCodeRequested,
                        code = uiState.verificationCodeInput,
                        isCodeVerified = uiState.isVerificationValid,
                        remainSeconds = uiState.verificationCountdownSec,
                        memo = uiState.memo,
                        isSubmitting = uiState.isSubmitting,
                        canSubmit = uiState.canSubmitRegister,
                        onNameChange = {
                            onEvent(SearchUserUiEvent.OnRelationshipNameChange(it))
                        },
                        onRelationshipTypeSelect = {
                            onEvent(SearchUserUiEvent.OnRelationshipTypeSelect(it))
                        },
                        onRequestVerification = {
                            onEvent(SearchUserUiEvent.OnClickRequestVerification)
                        },
                        onCodeChange = {
                            onEvent(SearchUserUiEvent.OnVerificationCodeChange(it))
                        },
                        onVerifyCode = {
                            onEvent(SearchUserUiEvent.OnClickVerifyCode)
                        },
                        onMemoChange = {
                            onEvent(SearchUserUiEvent.OnMemoChange(it))
                        },
                        onSubmit = {
                            onEvent(SearchUserUiEvent.OnClickSubmitRegister)
                        },
                        onCancel = {
                            onEvent(SearchUserUiEvent.OnClickBackToSearch)
                        }
                    )
                }

                SearchUserMode.REGISTER_DONE -> {
                    RegisterDoneSection(
                        onGoHome = onGoHome,
                        onSearchMore = {
                            onEvent(SearchUserUiEvent.OnClickSearchMore)
                        }
                    )
                }
            }
        }
    }



@Composable
private fun SearchHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 상단 전화 아이콘 박스
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(35.dp))
                .background(Color(0xFF7D9686)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "사용자 찾기",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "전화번호를 입력하면",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "등록된 사용자를 찾을 수 있어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SearchInputSection(
    phone: String,
    isSearching: Boolean,
    isPhoneValid: Boolean,
    onPhoneChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        SearchHeader()

        Spacer(modifier = Modifier.height(32.dp))

        // 카드 영역 (라운드 박스)
        Surface(
            shape = RoundedCornerShape(30.dp),
            color=Color(0xFFE9EAEC),
            tonalElevation = 15.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x00EBEBEB))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // 전화번호 라벨 + 인풋
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "전화번호",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    InputBox(
                        value = phone,
                        onValueChange = onPhoneChange,
                        label = "",
                        placeholder = "010-0000-0000",
                    )

                    // 안내 문구
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "하이픈(-) 없이 숫자만 입력하셔도 자동으로 형식이 맞춰집니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                GreenButton(
                    text = if (isSearching) "검색 중..." else "검색하기",
                    enabled = !isSearching && isPhoneValid,
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SearchSuccessSection(
    foundUser: UserSummary?,
    onAddFriend: () -> Unit,
    onBackToSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 상단 헤더 (아이콘 + 안내 텍스트)
        SearchHeader()

        Spacer(modifier = Modifier.height(32.dp))

        // 가운데 카드 (검색된 사용자 정보 + 버튼들)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFE9EAEC),
            tonalElevation = 8.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // 프로필 썸네일 (큰 원, 실제 디자인에서는 회색 톤 placeholder)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD3D3D3)),
                    contentAlignment = Alignment.Center
                ) {
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = foundUser?.displayName ?: "이름 없음",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = foundUser?.phoneNumber ?: "-",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GreenButton(
                        text = "친구 추가하기",
                        onClick = onAddFriend,
                        modifier = Modifier.fillMaxWidth()
                    )

                    GreyButton(
                        text = "다시 검색하기",
                        onClick = onBackToSearch,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 안내 문구 (카드 아래 info 아이콘 + 설명)
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "하이픈(-) 없이 숫자만 입력해주세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SearchNotFoundSection(
    errorMessage: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 상단 헤더 (아이콘 + 안내 텍스트)
        SearchHeader()

        Spacer(modifier = Modifier.height(32.dp))

        // 가운데 카드 (검색 실패 안내 + 버튼)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFE9EAEC),
            tonalElevation = 8.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // 상단 아이콘 박스 (회색 톤 아이콘 배경)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFFDCDCDC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "검색 결과 없음",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = errorMessage ?: "해당 전화번호로 등록된 사용자를 찾을 수 없습니다.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                GreenButton(
                    text = "다시 시도",
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 안내 문구 (카드 아래 info 아이콘 + 설명)
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "하이픈(-) 없이 숫자만 입력해주세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun RegisterSection(
    foundUserPhone: String,
    name: String,
    relationshipType: String,
    relationshipTypeOptions: List<String>,
    isCodeRequested: Boolean,
    code: String,
    isCodeVerified: Boolean,
    remainSeconds: Int,
    memo: String,
    isSubmitting: Boolean,
    canSubmit: Boolean,
    onNameChange: (String) -> Unit,
    onRelationshipTypeSelect: (String) -> Unit,
    onRequestVerification: () -> Unit,
    onCodeChange: (String) -> Unit,
    onVerifyCode: () -> Unit,
    onMemoChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // 상단 프로필 요약 (아바타 + 이름 + 번호)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFD3D3D3)),
                        contentAlignment = Alignment.Center
                    ) {
                        // placeholder avatar (회색 사각형)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = name.ifBlank { "이름 없음" },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )
                        // 아직 phone 파라미터가 없으므로 임시 표시
                        Text(
                            text = "010-0000-0000",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 입력 폼 영역
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // 이름
                    InputBox(
                        value = name,
                        onValueChange = onNameChange,
                        label = "이름",
                        placeholder = "이름을 입력하세요"
                    )

                    // 관계 (드롭다운)
                    RelationshipDropdown(
                        selectedType = relationshipType,
                        options = relationshipTypeOptions,
                        onTypeSelect = onRelationshipTypeSelect
                    )

                    // 휴대폰 번호 + 인증번호 발송 / 재발송
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "휴대폰 번호",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF374151)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InputBox(
                                value = foundUserPhone,
                                onValueChange = {},
                                label = "",
                                placeholder = "010-0000-0000",
                                modifier = Modifier.weight(1f),
                                enabled = false
                            )

                            GreenButton(
                                text = if (isCodeRequested) "재발송" else "발송",
                                enabled = true,
                                onClick = onRequestVerification,
                                modifier = Modifier
                                    .weight(0.6f)
                                    .height(56.dp)
                            )
                        }
                    }

                    // 인증번호 입력/확인/타이머/결과 메시지
                    if (isCodeRequested) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                InputBox(
                                    value = code,
                                    onValueChange = onCodeChange,
                                    label = "인증번호",
                                    placeholder = "인증번호 6자리",
                                    modifier = Modifier.weight(1f)
                                )

                                GreenButton(
                                    text = if (isCodeVerified) "확인됨" else "확인",
                                    enabled = !isCodeVerified,
                                    onClick = onVerifyCode,
                                    modifier = Modifier
                                        .weight(0.6f)
                                        .height(56.dp)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 남은 시간
                                if (remainSeconds > 0) {
                                    Text(
                                        text = "남은 시간: ${formatSeconds(remainSeconds)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFD32F2F)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                // 성공 메시지
                                if (isCodeVerified) {
                                    Text(
                                        text = "인증 완료",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }
                            }
                        }
                    }

                    // 메모
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InputBox(
                            value = memo,
                            onValueChange = onMemoChange,
                            label = "메모 (선택사항)",
                            placeholder = "추가 정보를 입력하세요"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${memo.length}/500",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // 하단 액션 버튼 (취소 / 등록하기) - 가로 나란히
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    GreyButton(
                        text = "취소",
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )

                    GreenButton(
                        text = if (isSubmitting) "등록 중..." else "등록하기",
                        enabled = !isSubmitting && canSubmit,
                        onClick = onSubmit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterDoneSection(
    onGoHome: () -> Unit,
    onSearchMore: () -> Unit
) {
    // dim + AlertModal 중앙 배치
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 흐려진 배경 (dim)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                )
        )

        // 가운데 모달
        AlertModal(
            onDismiss = { /* 모달 외부 클릭시 처리 */ },
            message = "연락처에 성공적으로 등록됐어요!",
            buttonText = "확인",
            onButtonClick = onGoHome
        )
    }
}


private fun formatSeconds(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "00:00"
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val mm = if (minutes < 10) "0$minutes" else "$minutes"
    val ss = if (seconds < 10) "0$seconds" else "$seconds"
    return "$mm:$ss"
}

@Composable
private fun RelationshipDropdown(
    selectedType: String,
    options: List<String>,
    onTypeSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "관계",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { expanded = true }
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedType.ifEmpty { "관계를 선택하세요" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedType.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF111827),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF6B7280)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onTypeSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true, name = "Search Input")
@Composable
private fun PreviewSearchUserScreen_SearchInput() {
    val dummyState = SearchUserUiState(
        mode = SearchUserMode.SEARCH_INPUT,
        phoneInput = "01012345678",
        isSearching = false
    )
    SearchUserScreenContent(
        uiState = dummyState,
        onEvent = {},
        onGoHome = {},
    )
}


@Preview(showBackground = true, name = "Search Success")
@Composable
private fun PreviewSearchUserScreen_SearchSuccess() {
    val dummyState = SearchUserUiState(
        mode = SearchUserMode.SEARCH_SUCCESS,
        foundUser = UserSummary(
            id = "user-001",
            displayName = "김서현",
            phoneNumber = "01012345678",
            avatarUrl = null
        )
    )
    SearchUserScreenContent(
        uiState = dummyState,
        onEvent = {},
        onGoHome = {},
    )
}


@Preview(showBackground = true, name = "Register (연락처 등록)")
@Composable
private fun PreviewSearchUserScreen_Register() {
    val dummyState = SearchUserUiState(
        mode = SearchUserMode.REGISTER,
        relationshipName = "김서현",
        relationshipType = "자녀",
        relationshipTypeOptions = listOf("자녀", "부모", "배우자", "기타"),
        verificationCodeInput = "123456",
        isVerificationValid = false,
        verificationCountdownSec = 165,
        memo = "수희야 일찍 자라",
        isSubmitting = false,
//        canSubmitRegister = true
    )
    SearchUserScreenContent(
        uiState = dummyState,
        onEvent = {},
        onGoHome = {},
    )
}

@Preview(showBackground = true, name = "Register Done")
@Composable
private fun PreviewSearchUserScreen_RegisterDone() {
    val dummyState = SearchUserUiState(
        mode = SearchUserMode.REGISTER_DONE
    )
    SearchUserScreenContent(
        uiState = dummyState,
        onEvent = {},
        onGoHome = {},
    )
}