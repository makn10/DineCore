package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppColors
import com.example.ui.util.currentStrings

enum class NavTab(val index: Int) {
    CHARTS(0),
    HISTORY(1),
    HOME(2),
    SUMMARY(3),
    PROFILE(4)
}

@Composable
fun PillNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()
    val isDark = isSystemInDarkTheme()

    val backgroundBrush = if (isDark) {
        Brush.horizontalGradient(
            listOf(Color(0xFF162529), Color(0xFF1D2F33))
        )
    } else {
        Brush.horizontalGradient(
            listOf(Color(0xFFDDF3F5), Color(0xFFF6EEF8))
        )
    }

    val unselectedColor = if (isDark) Color(0xFF90A4AE) else Color(0xFF78909C)
    val activeColor = AppColors.Indigo

    val navItems = listOf(
        Triple(NavTab.CHARTS, Icons.Default.BarChart, strings.navCharts),
        Triple(NavTab.HISTORY, Icons.Default.History, strings.navHistory),
        Triple(NavTab.HOME, Icons.Default.Home, strings.navHome),
        Triple(NavTab.SUMMARY, Icons.Default.Assessment, strings.navSummary),
        Triple(NavTab.PROFILE, Icons.Default.Person, strings.navProfile)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(76.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(38.dp),
                spotColor = Color(0x33000000),
                ambientColor = Color(0x11000000)
            )
            .clip(RoundedCornerShape(38.dp))
            .background(backgroundBrush)
            .border(
                width = 1.dp,
                color = if (isDark) Color(0xFF2E4348) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(38.dp)
            )
            .testTag("pill_nav_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (tab, icon, label) ->
                val isSelected = selectedTab == tab
                val yOffset by animateDpAsState(
                    targetValue = if (isSelected) (-8).dp else 0.dp,
                    animationSpec = spring(),
                    label = "pill_offset"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = yOffset),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .shadow(elevation = 8.dp, shape = CircleShape, spotColor = activeColor)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF263238) else Color.White)
                                    .border(width = 2.dp, color = Color(0xFFB39DDB), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = activeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = unselectedColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        text = label,
                        color = if (isSelected) activeColor else unselectedColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
