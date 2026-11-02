package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalExtendedColors

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    padding: PaddingValues = PaddingValues(20.dp),
    testTag: String = "app_card",
    content: @Composable BoxScope.() -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val bg = backgroundColor ?: extendedColors.cardBackground
    val border = borderColor ?: extendedColors.border.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x22000000),
                ambientColor = Color(0x11000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(24.dp))
            .padding(padding)
            .testTag(testTag),
        content = content
    )
}
