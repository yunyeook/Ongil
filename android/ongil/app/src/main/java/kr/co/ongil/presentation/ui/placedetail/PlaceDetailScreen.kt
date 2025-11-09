package kr.co.ongil.presentation.ui.placedetail

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.InputBox



@Composable
fun PlaceDetailScreen(
    viewModel: PlaceDetailViewModel,
    onBackClick: () -> Unit,
    onSavedSuccess: () -> Unit,
    onDeletedSuccess: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    PlaceDetailScreen(
        favoriteId = uiState.favoriteId,
        placeName = uiState.placeName,
        address = uiState.address,
        isDefault = uiState.isDefault,
        initialIsDefault = uiState.initialIsDefault,
        onBackClick = onBackClick,
        onSetDefaultClick = { viewModel.setAsDefault() },
        onSaveClick = { name, addr, isDefault ->
            viewModel.updatePlaceInfo(name, addr, isDefault) {
                onSavedSuccess()
            }
        },
        onDeleteClick = {
            viewModel.deletePlace {
                onDeletedSuccess()
            }
        },
        isLoading = uiState.isLoading,
        error = uiState.error,
        successMessage = uiState.successMessage
    )
}

@Composable
fun PlaceDetailScreen(
    favoriteId: Long,
    placeName: String,
    address: String,
    isDefault: Boolean,
    initialIsDefault: Boolean?,
    onBackClick: () -> Unit,
    onSetDefaultClick: () -> Unit,
    onSaveClick: (String, String, Boolean?) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    error: String? = null,
    successMessage: String? = null
) {
    var editedPlaceName by remember { mutableStateOf(placeName) }
    var editedAddress by remember { mutableStateOf(address) }

    // API에서 데이터를 새로 받으면 편집 필드도 업데이트
    LaunchedEffect(placeName, address) {
        editedPlaceName = placeName
        editedAddress = address
    }

    val hasChanges = remember(placeName, address, isDefault, editedPlaceName, editedAddress, initialIsDefault) {
        val nameChanged = editedPlaceName.trim() != placeName
        val addressChanged = editedAddress.trim() != address
        val defaultChanged = initialIsDefault?.let { it != isDefault } ?: false
        nameChanged || addressChanged || defaultChanged
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(error, successMessage) {
        error?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
        successMessage?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                modifier = modifier.fillMaxSize(),
                color = Color(0xFFF9FAFB) // 배경 연한 그레이 톤 (즐겨찾기 화면과 동일)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {

            InputBox(
                label = "장소명",
                placeholder = "",
                value = editedPlaceName,
                onValueChange = {
                    editedPlaceName = it
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            InputBox(
                label = "주소",
                placeholder = "",
                value = editedAddress,
                onValueChange = {
//                    근데 주소도 수정할 수 있어요?
                    editedAddress = it
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ===== 버튼 영역 =====
            GreenButton(
                text = if (isDefault) "기본 목적지 설정됨" else "기본 목적지로 설정",
                onClick = onSetDefaultClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                containerColor = if (isDefault) Color(0xFF87A293) else Color(0xFFD1D5DB)
            )

            GreenButton(
                text = "저장하기",
                onClick = {
                    val name = editedPlaceName.trim()
                    val addr = editedAddress.trim()
                    if (name.isNotEmpty() && hasChanges) {
                        // isDefault가 변경되었는지 확인
                        val isDefaultChanged = initialIsDefault?.let { it != isDefault } ?: false
                        // 변경되었고 true로 변경된 경우에만 true 전달, 아니면 null
                        val isDefaultToSend = if (isDefaultChanged && isDefault) true else null
                        android.util.Log.d("PlaceDetailScreen", "저장 버튼 클릭 - name=$name, addr=$addr, isDefault=$isDefault, initialIsDefault=$initialIsDefault, isDefaultChanged=$isDefaultChanged, isDefaultToSend=$isDefaultToSend")
                        onSaveClick(name, addr, isDefaultToSend)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                containerColor = if (hasChanges) Color(0xFF87A293) else Color(0xFFD1D5DB)
            )

            GreenButton(
                text = "삭제하기",
                onClick = onDeleteClick,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // ===== 하단 탭바 영역 (Bottom Navigation) =====

            Spacer(modifier = Modifier.height(80.dp))
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFE5E7EB)
@Composable
private fun PlaceDetailScreenPreview() {
    PlaceDetailScreen(
        favoriteId = 1L,
        placeName = "뭐라고할까요",
        address = "주소가어디게",
        isDefault = false,
        initialIsDefault = false,
        onBackClick = {},
        onSetDefaultClick = {},
        onSaveClick = { _: String, _: String, _: Boolean? -> },
        onDeleteClick = {}
    )
}