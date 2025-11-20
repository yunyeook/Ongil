package kr.co.ongil.wear.presentation.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.*
import kr.co.ongil.wear.domain.model.PatientInfo
import kr.co.ongil.wear.presentation.viewmodel.PatientViewModel

/**
 * 환자 선택 화면 (보호자용)
 *
 * 주요 기능:
 * 1. 보호자가 관리하는 환자 목록 표시
 * 2. 환자 선택 (Chip 클릭)
 * 3. 선택한 환자 ID DataStore 저장
 * 4. Phone 앱과 동기화
 */
@Composable
fun PatientSelectionScreen(
    viewModel: PatientViewModel = hiltViewModel(),
    onPatientSelected: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        timeText = {
            TimeText()
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        }
    ) {
        when {
            uiState.isLoading -> {
                LoadingState()
            }
            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage!!,
                    onDismiss = {
                        viewModel.clearError()
                        onBackPressed()
                    }
                )
            }
            uiState.patients.isEmpty() -> {
                EmptyState(
                    onBackPressed = onBackPressed
                )
            }
            else -> {
                PatientListContent(
                    patients = uiState.patients,
                    selectedPatientId = uiState.selectedPatientId,
                    onPatientClick = { patient ->
                        viewModel.selectPatient(patient.patientId)
                        onPatientSelected()
                    }
                )
            }
        }
    }
}

/**
 * 로딩 상태
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 에러 상태
 */
@Composable
private fun ErrorState(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.body2,
                color = Color.Red,
                textAlign = TextAlign.Center
            )

            CompactButton(onClick = onDismiss) {
                Text("확인")
            }
        }
    }
}

/**
 * 빈 목록 상태
 */
@Composable
private fun EmptyState(
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "등록된 환자가 없습니다",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Phone 앱에서 환자를 등록해주세요",
                style = MaterialTheme.typography.caption2,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactButton(onClick = onBackPressed) {
                Text("뒤로")
            }
        }
    }
}

/**
 * 환자 목록
 */
@Composable
private fun PatientListContent(
    patients: List<PatientInfo>,
    selectedPatientId: Long?,
    onPatientClick: (PatientInfo) -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 32.dp,
            start = 10.dp,
            end = 10.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 헤더
        item {
            Text(
                text = "환자 선택",
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 환자 카드들
        items(patients.size) { index ->
            val patient = patients[index]
            PatientCard(
                patient = patient,
                isSelected = patient.patientId == selectedPatientId,
                onClick = { onPatientClick(patient) }
            )
        }
    }
}

/**
 * 환자 카드
 */
@Composable
private fun PatientCard(
    patient: PatientInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick,
        label = {
            Text(
                text = patient.name,
                style = MaterialTheme.typography.title3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = patient.relationship?.let { relationship ->
            {
                Text(
                    text = relationship,
                    style = MaterialTheme.typography.caption2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        colors = if (isSelected) {
            ChipDefaults.primaryChipColors(
                backgroundColor = Color(0xFF4CAF50)
            )
        } else {
            ChipDefaults.secondaryChipColors()
        }
    )
}
