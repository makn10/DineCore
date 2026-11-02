package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalExtendedColors

@Composable
fun RowScope.MealSlotBox(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier,
    testTag: String = "meal_slot_${label.lowercase()}"
) {
    val extended = LocalExtendedColors.current

    Column(
        modifier = modifier
            .weight(1f)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .border(width = 1.5.dp, color = color.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = count,
            color = extended.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
