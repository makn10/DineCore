package com.example.ui.screens.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calc.CalculationService
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import java.util.Calendar

@Composable
fun MealSheetScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onNavigateToAddMeal: () -> Unit
) {
    val extended = LocalExtendedColors.current
    val members by viewModel.members.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val selectedPeriodId by viewModel.selectedPeriodId.collectAsState()

    // Determine days in period
    val daysCount = remember(selectedPeriodId) {
        val parts = selectedPeriodId.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 11
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // Map: memberId -> (dateString -> mealRecord)
    val mealsMap = remember(meals) {
        meals.groupBy { it.memberId }.mapValues { (_, list) ->
            list.associateBy { it.date }
        }
    }

    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeaderAppBar(
                title = "Meal Sheet",
                backgroundColor = AppColors.MealSheet,
                onBackClick = onBackClick
            )

            if (members.isEmpty()) {
                EmptyState(title = "No members in mess yet")
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 72.dp)
                ) {
                    // STICKY MEMBER COLUMN (Left)
                    Column(
                        modifier = Modifier
                            .width(110.dp)
                            .fillMaxHeight()
                            .background(extended.cardBackground)
                            .border(width = 1.dp, color = extended.border.copy(alpha = 0.5f))
                            .verticalScroll(verticalScroll)
                    ) {
                        // Header corner cell
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(44.dp)
                                .background(AppColors.MealSheet)
                                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Member",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Member name cells
                        members.forEach { member ->
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(48.dp)
                                    .border(width = 0.5.dp, color = extended.border.copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = member.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }

                        // Total Row label
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(44.dp)
                                .background(AppColors.TealTint)
                                .border(width = 0.5.dp, color = extended.border.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Total Meals",
                                color = AppColors.PrimaryDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // HORIZONTALLY SCROLLABLE DATE MATRIX (Right)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(horizontalScroll)
                            .verticalScroll(verticalScroll)
                    ) {
                        // Date header row
                        Row(modifier = Modifier.height(44.dp)) {
                            for (day in 1..daysCount) {
                                val dayStr = day.toString().padStart(2, '0')
                                Box(
                                    modifier = Modifier
                                        .width(46.dp)
                                        .height(44.dp)
                                        .background(AppColors.MealSheet.copy(alpha = 0.9f))
                                        .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayStr,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Member meal cells rows
                        members.forEach { member ->
                            Row(modifier = Modifier.height(48.dp)) {
                                for (day in 1..daysCount) {
                                    val dateStr = "$selectedPeriodId-${day.toString().padStart(2, '0')}"
                                    val record = mealsMap[member.id]?.get(dateStr)
                                    val count = record?.totalMeals ?: 0.0

                                    Box(
                                        modifier = Modifier
                                            .width(46.dp)
                                            .height(48.dp)
                                            .background(if (count > 0) AppColors.TealTint.copy(alpha = 0.3f) else extended.cardBackground)
                                            .border(width = 0.5.dp, color = extended.border.copy(alpha = 0.2f))
                                            .clickable { onNavigateToAddMeal() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (count > 0) CalculationService.formatMeals(count) else "-",
                                            fontSize = 12.sp,
                                            fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                                            color = if (count > 0) AppColors.PrimaryDark else extended.textMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Day totals row
                        Row(modifier = Modifier.height(44.dp)) {
                            for (day in 1..daysCount) {
                                val dateStr = "$selectedPeriodId-${day.toString().padStart(2, '0')}"
                                val dayTotal = meals.filter { it.date == dateStr }.sumOf { it.totalMeals }
                                Box(
                                    modifier = Modifier
                                        .width(46.dp)
                                        .height(44.dp)
                                        .background(AppColors.TealTint)
                                        .border(width = 0.5.dp, color = extended.border.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (dayTotal > 0) CalculationService.formatMeals(dayTotal) else "-",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppColors.PrimaryDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddMeal,
            containerColor = AppColors.MealSheet,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_meal_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Meal")
        }
    }
}
