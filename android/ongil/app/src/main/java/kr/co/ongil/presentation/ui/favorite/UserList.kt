package kr.co.ongil.presentation.ui.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.favorite.PatientCard
import kr.co.ongil.presentation.theme.ongilColors

@Composable
fun PatientList(
    patients: List<PatientData>,
    userType: String,  // 추가
    onCallClick: (Long) -> Unit,
    onPatientCardClick: (relationshipId: Long) -> Unit,
    onGoSearchUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ongilColors

    Column(modifier = modifier) {

        GreenButton(
            text = "+ 새로운 사용자 등록",
            onClick = onGoSearchUserClick,
            containerColor = colors.accent,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .fillMaxWidth()
        )



        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = 16.dp
            )
        ) {
            items(patients) { patient ->
                PatientCard(
                    patientName = patient.name,
                    phoneNumber = patient.phoneNumber,
                    isDefault = patient.isDefault,
                    userType = userType,
                    onClickCard = {
                        onPatientCardClick(patient.relationshipId)
                    },
                    onClickCall = {
                        onCallClick(patient.id)
                    },
                    modifier = Modifier.fillMaxWidth()
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
        PatientData(
            id = 1L,
            relationshipId = 1L,
            name = "김철수 할아버지",
            phoneNumber = "010-1234-5678",
            relationshipType = "할아버지",
            isDefault = true
        ),
        PatientData(
            id = 2L,
            relationshipId = 2L,
            name = "이영희 할머니",
            phoneNumber = "010-2222-3333",
            relationshipType = "할머니",
            isDefault = false
        ),
        PatientData(
            id = 3L,
            relationshipId = 3L,
            name = "박민수 어르신",
            phoneNumber = "010-9999-0000",
            relationshipType = "어르신",
            isDefault = false
        )
    )

    PatientList(
        patients = samplePatients,
        userType = "GUARDIAN",
        onCallClick = {  },
        onPatientCardClick = { _ ->  },
        onGoSearchUserClick = {  }
    )
}