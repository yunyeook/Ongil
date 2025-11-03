package kr.co.ongil.presentation.ui.patientdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.InputBox


@Composable
fun PatientDetailScreen(
    viewModel: PatientDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()


    var name by remember(uiState.patientId) { mutableStateOf(uiState.name) }
    var phoneNumber by remember(uiState.patientId) { mutableStateOf(uiState.phoneNumber) }
    var gender by remember(uiState.patientId) { mutableStateOf(uiState.gender) }

    PatientDetailContent(
        name = name,
        phoneNumber = phoneNumber,
        gender = gender,
        onNameChange = { name = it },
        onPhoneChange = { phoneNumber = it },
        onGenderChange = { gender = it },
        onSaveClick = {
            viewModel.updatePatientInfo(
                newName = name,
                newPhoneNumber = phoneNumber,
                newGender = gender
            )
            onNavigateBack()
        },
        onDeleteClick = {
            viewModel.deletePatient {
                onNavigateBack()
            }
        },
        modifier = modifier
    )
}


@Composable
fun PatientDetailContent(
    name: String,
    phoneNumber: String,
    gender: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = Color(0xFFFFFFFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Spacer(modifier = Modifier.height(150.dp))

            // 이름
            InputBox(
                label = "이름",
                placeholder = "",
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 전화번호
            InputBox(
                label = "전화번호",
                placeholder = "",
                value = phoneNumber,
                onValueChange = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 성별
            InputBox(
                label = "성별",
                placeholder = "",
                value = gender,
                onValueChange = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 저장하기 버튼
            GreenButton(
                text = "저장하기",
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 삭제하기 버튼
            GreenButton(
                text = "삭제하기",
                onClick = onDeleteClick,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PatientDetailScreenPreview() {
    PatientDetailContent(
        name = "김철수",
        phoneNumber = "010-1234-5678",
        gender = "남성",
        onNameChange = {},
        onPhoneChange = {},
        onGenderChange = {},
        onSaveClick = {},
        onDeleteClick = {}
    )
}