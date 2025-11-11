package kr.co.ongil.presentation.ui.favorite

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kr.co.ongil.presentation.ui.favorite.PatientList
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState

@Composable
fun FavoriteScreen(
    navController: NavController,
    onNavigateToPlaceDetail: (patientId: Long, favoriteId: Long) -> Unit,
    onNavigateToPatientDetail: (relationshipId: Long) -> Unit,
    onGoSearchUserClick: () -> Unit,
    modifier: Modifier = Modifier
)
{
    val viewModel: FavoriteViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 상세 화면에서 수정/삭제 성공 시 갱신 플래그 감지
    val updatedFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("favorite_updated", false)
    val updated = updatedFlow?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(updated.value) {
        if (updated.value) {
            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
            android.util.Log.d("FavoriteScreen", "favorite_updated 감지됨")

            // 수정된 장소 데이터가 있으면 즉시 로컬 상태 업데이트
            val updatedFavoriteId = savedStateHandle?.get<Long>("updated_favorite_id")
            val updatedPlaceAlias = savedStateHandle?.get<String>("updated_place_alias")
            val updatedIsDefault = savedStateHandle?.get<Boolean>("updated_is_default")

            android.util.Log.d("FavoriteScreen", "savedState - favoriteId=$updatedFavoriteId, placeAlias=$updatedPlaceAlias, isDefault=$updatedIsDefault")

            if (updatedFavoriteId != null && updatedPlaceAlias != null && updatedIsDefault != null) {
                // 로컬 상태를 즉시 업데이트
                android.util.Log.d("FavoriteScreen", "로컬 업데이트 실행: favoriteId=$updatedFavoriteId, placeAlias=$updatedPlaceAlias")
                viewModel.updatePlaceLocally(updatedFavoriteId, updatedPlaceAlias, updatedIsDefault)

                // savedStateHandle 정리
                savedStateHandle.remove<Long>("updated_favorite_id")
                savedStateHandle.remove<String>("updated_place_alias")
                savedStateHandle.remove<Boolean>("updated_is_default")
            } else {
                android.util.Log.d("FavoriteScreen", "로컬 업데이트 건너뜀 - 데이터 없음")
            }

            // 백엔드에서도 최신 데이터 가져오기 (비동기)
            android.util.Log.d("FavoriteScreen", "백엔드 새로고침 실행")
            viewModel.refresh()

            savedStateHandle?.set("favorite_updated", false)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            scope.launch { snackbarHostState.showSnackbar(errorMessage) }
        }
    }

    // 화면이 다시 보일 때마다 데이터 새로고침
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFFFFFFF)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                // 타이틀 / 설명 영역
                FavoriteTitleSection(
                    userName = uiState.userName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 탭 바 (환자 목록 / 장소 목록)
                FavoriteTabBar(
                    selectedTab = uiState.selectedTab,
                    userType = uiState.userType,
                    onTabSelected = { tab ->
                        viewModel.onEvent(FavoriteUiEvent.OnTabSelected(tab))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 탭 컨텐츠
                when (uiState.selectedTab) {
                    FavoriteTab.PATIENTS -> {
                        PatientList(
                            patients = uiState.patients,
                            onCallClick = { id ->
                                viewModel.onEvent(FavoriteUiEvent.OnCallClick(id))
                            },
                            onPatientCardClick = { relationshipId ->
                                onNavigateToPatientDetail(relationshipId)
                            },
                            onGoSearchUserClick = {
                                onGoSearchUserClick()
                            }
                        )
                    }

                    FavoriteTab.PLACES -> {
                        PlaceList(
                            places = uiState.places,
                            onAddPlaceClick = {
                                viewModel.onEvent(FavoriteUiEvent.OnAddPlaceClick)
                            },
                            onClickPlaceIcon = { placeId ->
                                // TODO: 지도 화면으로 이동 or 지도 열기 처리 예정
                            },
                            onClickPlaceCardWithPatient = { _, favoriteId ->
                                // place.patientId 대신 실제 조회에 사용한 currentPatientId 사용 (403 방지)
                                onNavigateToPlaceDetail(uiState.currentPatientId, favoriteId)
                            },
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}


@Composable
private fun FavoriteTitleSection(
    userName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = if (userName.isNotEmpty()) "${userName}님의 즐겨찾기" else "사용자님의 즐겨찾기",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "자주 연락하는 보호 대상자와 관리 장소를 한 곳에서 확인하세요.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6B7280),
            lineHeight = 20.sp
        )
    }
}


@Composable
private fun PlaceholderPlacesSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "즐겨찾는 장소",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4B5563)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "등록된 장소가 없습니다.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF9CA3AF),
            lineHeight = 20.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteScreenPatientsPreview() {
    // 더미 환자 데이터
    val samplePatients = listOf(
        PatientData(
            id = 1L,
            relationshipId = 1L,
            name = "홍길동",
            phoneNumber = "010-1234-5678",
            relationshipType = "자녀"
        ),
        PatientData(
            id = 2L,
            relationshipId = 2L,
            name = "김영희",
            phoneNumber = "010-2345-6789",
            relationshipType = "부모"
        ),
        PatientData(
            id = 3L,
            relationshipId = 3L,
            name = "이철수",
            phoneNumber = "010-3456-7890",
            relationshipType = "친구"
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFFFF)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 타이틀 영역
            FavoriteTitleSection(
                userName = "홍길동",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 탭 바 (현재는 환자 탭)
            FavoriteTabBar(
                selectedTab = FavoriteTab.PATIENTS,
                userType = "GUARDIAN",
                onTabSelected = { },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 환자 목록
            PatientList(
                patients = samplePatients,
                onCallClick = { },
                onPatientCardClick = { _ -> },
                onGoSearchUserClick = { }
            )
        }
    }
}
