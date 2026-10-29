package com.example.atuneslegacy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atuneslegacy.ui.theme.ATunesLegacyTheme
import com.example.atuneslegacy.ui.theme.ITunesActiveSegmentEnd
import com.example.atuneslegacy.ui.theme.ITunesActiveSegmentStart

@Composable
fun <T> ITunesSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    getItemLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = item == selectedItem
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                items.lastIndex -> RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp)
                else -> RoundedCornerShape(0.dp)
            }

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .weight(1f)
                    .clip(shape)
                    .background(
                        brush = if (isSelected) {
                            Brush.verticalGradient(
                                listOf(ITunesActiveSegmentStart, ITunesActiveSegmentEnd)
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xFFF9FAFC), Color(0xFFE2E5EB))
                            )
                        }
                    )
                    .border(
                        width = 0.5.dp,
                        color = if (isSelected) Color(0xFF1252B4) else Color(0xFFB0B5C0)
                    )
                    .clickable { onItemSelected(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getItemLabel(item),
                    style = TextStyle(
                        color = if (isSelected) Color.White else Color(0xFF32363F),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ITunesSegmentedControlPreview() {
    ATunesLegacyTheme {
        ITunesSegmentedControl(
            items = listOf("Songs", "Albums", "Artists", "Genres", "Playlists", "Music Videos"),
            selectedItem = "Songs",
            onItemSelected = {},
            getItemLabel = { it }
        )
    }
}
