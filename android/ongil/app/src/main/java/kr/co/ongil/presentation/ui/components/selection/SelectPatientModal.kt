package kr.co.ongil.presentation.ui.components.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.ui.components.patient.PatientCard

data class PatientInfoUi(
    val id: String,
    val name: String,
    val position: String,
    val profileImageUrl: String?
)

@Composable
fun SelectPatientModal(
    patients: List<PatientInfoUi>,
    modifier: Modifier = Modifier,
    onSelectPatient: (PatientInfoUi) -> Unit = {}
) {
    var selectedId by remember { mutableStateOf(patients.firstOrNull()?.id) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 0.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = "관찰할 어르신을 선택해주세요.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF4B5563),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 15.dp, bottom = 30.dp)
        )
//        Spacer()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(patients) { p ->
                PatientCard(
                    name = p.name,
                    position = p.position,
                    profileImageUrl = p.profileImageUrl,
                    isSelected = (p.id == selectedId),
                    onClick = {
                        selectedId = p.id
                        onSelectPatient(p)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_SelectPatientModal() {
    val dummy = listOf(
        PatientInfoUi(
            id = "1",
            name = "김복자",
            position = "이동중",
            profileImageUrl = "https://example.com/p1.jpg"
        ),
        PatientInfoUi(
            id = "2",
            name = "박복순",
            position = "집",
            profileImageUrl = "https://example.com/p2.jpg"
        ),
        PatientInfoUi(
            id = "3",
            name = "황경례",
            position = "집",
            profileImageUrl = null
        )
    )

    SelectPatientModal(
        patients = dummy,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(0xFFEFF2EF))
            .padding(16.dp)
    )
}