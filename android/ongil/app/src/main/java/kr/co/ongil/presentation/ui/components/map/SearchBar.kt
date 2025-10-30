package kr.co.ongil.presentation.ui.components.map

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {}
) {
    val borderColor = Color(0xFFD9D9D9)
    val placeholderColor = Color(0xFF9CA3AF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "장소를 검색해주세요",
                color = placeholderColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF101828),
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onSearchClick)
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        SearchBar()
    }
}