package kr.co.ongil.presentation.ui.favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.favorite.PatientCard
import androidx.compose.foundation.layout.fillMaxWidth

// 환자 목록 섹션만 담당 (상단의 "+ 새로운 환자 등록" 버튼 포함 예정이라면 여기에 배치 가능)
@Composable
fun PatientList(
    patients: List<PatientItem>,
    onCallClick: (Long) -> Unit,
    onAddPatientClick: () -> Unit
) {
    Column {
        GreenButton(
            text = " + 새로운 환자 등록",
            onClick = onAddPatientClick,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .fillMaxWidth()

        )
        Spacer(modifier = Modifier.height(8.dp))

        // 일부러 일단 레이지칼럼으로 묶어뒀는데, 필요하면 그냥 전체 다 렌더링하는걸로 바꿀게요
        LazyColumn {
            items(patients) { patient ->
                PatientCard(
                    patientLabel = "환자 ${patient.id}",
                    basicInfo = patient.displayName,
                    phoneNumber = patient.phoneNumber,
                    onClickCard = { /* 환자 상세보기로 이동 */ },
                    onClickIcon = { /* 여기는 전화 걸기로 이동해야됨 */ },
                    modifier = Modifier
                        .padding(
                                bottom = 16.dp
                        )
            )

            }
        }

    }
}


@Composable
@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
fun PatientListPreview() {
    val samplePatients = listOf(
        PatientItem(
            id = 1L,
            displayName = "김철수 (남, 75세)",
            phoneNumber = "010-1234-5678"
        ),
        PatientItem(
            id = 2L,
            displayName = "이영희 (여, 68세)",
            phoneNumber = "010-2345-6789"
        ),
        PatientItem(
            id = 3L,
            displayName = "박민수 (남, 82세)",
            phoneNumber = "010-3456-7890"
        )
    )

    PatientList(
        patients = samplePatients,
        onCallClick = { /* no-op for preview */ },
        onAddPatientClick = { /* no-op for preview */ }
    )
}