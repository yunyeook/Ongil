package kr.co.ongil.presentation.ui.common.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.ui.common.patient.PatientCard

data class PatientInfoUi(
    val id: String,
    val name: String,
    val profileImageUrl: String?
)

@Composable
fun SelectPatientModal(
    patients: List<PatientInfoUi>,
    modifier: Modifier = Modifier,
    selectedPatientId: String? = null,
    onSelectPatient: (PatientInfoUi) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var selectedId by remember(selectedPatientId) { mutableStateOf(selectedPatientId ?: patients.firstOrNull()?.id) }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFDEDEE0),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(start = 25.dp, top = 24.dp, end = 25.dp, bottom = 25.dp)
        ) {
            Text(
                text = "관찰할 어르신을 선택해주세요.",
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF4B5563),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 15.dp, bottom = 30.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(patients) { p ->
                    PatientCard(
                        name = p.name,
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

        // 닫기 버튼 (우측 상단)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
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
            profileImageUrl = "https://example.com/p1.jpg"
        ),
        PatientInfoUi(
            id = "2",
            name = "박복순",
            profileImageUrl = "https://example.com/p2.jpg"
        ),
        PatientInfoUi(
            id = "3",
            name = "황경례",
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
