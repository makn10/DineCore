package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberRole
import com.example.ui.theme.AppColors

@Composable
fun RoleBadge(
    role: MemberRole,
    modifier: Modifier = Modifier
) {
    val (bgColor, fgColor) = when (role) {
        MemberRole.MANAGER -> Pair(Color(0xFFF3E5F5), AppColors.Roles)
        MemberRole.SUB_MANAGER -> Pair(Color(0xFFE8EAF6), AppColors.Indigo)
        MemberRole.MEMBER -> Pair(AppColors.TealTint, AppColors.PrimaryDark)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(width = 1.dp, color = fgColor.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("role_badge_${role.name.lowercase()}")
    ) {
        Text(
            text = role.displayName,
            color = fgColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
