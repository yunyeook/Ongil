package kr.co.ongil.presentation.ui.components.favorite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PatientListCard(
    patientLabel: String,
    basicInfo: String,
    phoneNumber: String,
    onClickCard: () -> Unit,
    onClickCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickCard() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = patientLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = basicInfo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF6B7280),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "phone icon small",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = phoneNumber,
                            fontSize = 16.sp,
                            color = Color(0xFF9CA3AF),
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = { onClickCall() },
                    modifier = Modifier
                        .size(48.dp) // 시안상 원이 좀 더 크니까 48.dp로 넉넉하게
                        .clip(CircleShape)
                        .background(Color(0xFF5C7165)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "call button",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Divider(
                color = Color(0xFFE5E7EB),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun PatientListPreview() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color(0xFF1E1E1E)
    ) {
        PatientListCard(
            patientLabel = "환자 1",
            basicInfo = "김철수 (남, 75세)",
            phoneNumber = "010-1234-5678",
            onClickCard = {},
            onClickCall = {},
            modifier = Modifier
        )
    }
}