package kr.co.ongil.presentation.ui.favorite

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kr.co.ongil.presentation.ui.favorite.PatientList
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.co.ongil.presentation.ui.favorite.FavoriteUiEvent.onGoSearchUserClick

@Composable
fun FavoriteScreen(
    patientId: Long? = null,
    onNavigateToPlaceDetail: (favoriteId: Long, placeName: String, address: String) -> Unit,
    onNavigateToPatientDetail: (patientId: Long, name: String, phoneNumber: String, gender: String) -> Unit,
    onGoSearchUserClick: () -> Unit
)
//    viewModel: FavoriteViewModel = viewModel()
//    - 이거 더미테스트용이니까 일단 빼고 나중에 위에 집어넣기
//      그리고 밑에 val uistate이거 빼기
 {    val viewModel: FavoriteViewModel =
     androidx.lifecycle.viewmodel.compose.viewModel(
         factory = FavoriteDummyFactory(
             initialPatientId = patientId ?: 1L
         )
     )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val effectivePatientId = patientId ?: 1L
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(effectivePatientId) {
        viewModel.loadData(effectivePatientId)
    }

    // 화면이 다시 보일 때마다 데이터 새로고침
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadData(effectivePatientId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFFFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // 타이틀 / 설명 영역
            FavoriteTitleSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 탭 바 (환자 목록 / 장소 목록)
            FavoriteTabBar(
                selectedTab = uiState.selectedTab,
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
                        onPatientCardClick = { id, name, phoneNumber, gender ->
                            viewModel.onEvent(FavoriteUiEvent.OnPatientCardClick(id))
                            onNavigateToPatientDetail(
                                id,
                                name,
                                phoneNumber,
                                gender
                            )
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
                        onClickPlaceCard = { favoriteId, placeName, address ->
                            onNavigateToPlaceDetail(
                                favoriteId,
                                placeName,
                                address
                            )
                        },
                        onClickPlaceIcon = { placeId ->
                            // TODO: 지도 화면으로 이동 or 지도 열기 처리 예정
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteTitleSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "사용자님의 즐겨찾기",
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
