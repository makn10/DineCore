package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalExtendedColors

enum class StatTileType {
    BALANCE,
    RATE
}

@Composable
fun StatTile(
    label: String,
    value: String,
    type: StatTileType,
    modifier: Modifier = Modifier,
    customIcon: ImageVector? = null,
    testTag: String = if (type == StatTileType.BALANCE) "stat_tile_balance" else "stat_tile_rate"
) {
    val extended = LocalExtendedColors.current

    val (bg, fg, defaultIcon) = when (type) {
        StatTileType.BALANCE -> Triple(
            extended.tealTint,
            com.example.ui.theme.AppColors.PrimaryDark,
            Icons.Outlined.AccountBalanceWallet
        )
        StatTileType.RATE -> Triple(
            extended.rateTint,
            extended.rateText,
            Icons.Outlined.Calculate
        )
    }

    val icon = customIcon ?: defaultIcon

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(width = 1.dp, color = fg.copy(alpha = 0.25f), shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = fg,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = fg,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = fg,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
