package com.example.ui.screens.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calc.CalculationService
import com.example.data.model.MealRecord
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.AppTextField
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddMealScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val extended = LocalExtendedColors.current
    val members by viewModel.members.collectAsState()
    val selectedPeriodId by viewModel.selectedPeriodId.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Members, 1: Single Member
    var dateString by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }

    // Multi-member state: memberId -> Triple(breakfast, lunch, dinner)
    val memberMealValues = remember(members) {
        mutableStateMapOf<String, Triple<Double, Double, Double>>().apply {
            members.forEach { m -> put(m.id, Triple(1.0, 1.0, 1.0)) }
        }
    }

    // Single member state
    var singleMemberId by remember { mutableStateOf(members.firstOrNull()?.id.orEmpty()) }
    var singleBreakfast by remember { mutableStateOf(1.0) }
    var singleLunch by remember { mutableStateOf(1.0) }
    var singleDinner by remember { mutableStateOf(1.0) }
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "Add Daily Meal",
            backgroundColor = AppColors.MealSheet,
            onBackClick = onBackClick
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = extended.cardBackground,
            contentColor = AppColors.MealSheet
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All Members (Bulk)", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_all_members_meal")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Single Member", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_single_member_meal")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Date selection field
        AppCard(padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Date (yyyy-MM-dd):", fontWeight = FontWeight.Bold, color = extended.textPrimary)
                AppTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = "Date",
                    modifier = Modifier.width(160.dp),
                    testTag = "input_meal_date"
                )
            }
        }

        if (selectedTab == 0) {
            // ALL MEMBERS BULK
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                // Quick Set Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Quick Actions:", fontSize = 12.sp, color = extended.textMuted)
                    Row {
                        FilterChip(
                            selected = false,
                            onClick = {
                                members.forEach { m ->
                                    val cur = memberMealValues[m.id] ?: Triple(1.0, 1.0, 1.0)
                                    memberMealValues[m.id] = Triple(1.0, 1.0, 1.0)
                                }
                            },
                            label = { Text("Set All 1.0", fontSize = 11.sp) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                members.forEach { m -> memberMealValues[m.id] = Triple(0.0, 0.0, 0.0) }
                            },
                            label = { Text("Reset 0", fontSize = 11.sp) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(members, key = { it.id }) { member ->
                        val (b, l, d) = memberMealValues[member.id] ?: Triple(1.0, 1.0, 1.0)
                        val total = b + l + d

                        AppCard(padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = member.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = extended.textPrimary
                                    )
                                    Text(
                                        text = "Total: ${CalculationService.formatMeals(total)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = AppColors.MealSheet
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Breakfast Stepper
                                    MealSlotStepper(
                                        label = "B",
                                        color = AppColors.Breakfast,
                                        value = b,
                                        onValueChange = { memberMealValues[member.id] = Triple(it, l, d) }
                                    )
                                    // Lunch Stepper
                                    MealSlotStepper(
                                        label = "L",
                                        color = AppColors.Lunch,
                                        value = l,
                                        onValueChange = { memberMealValues[member.id] = Triple(b, it, d) }
                                    )
                                    // Dinner Stepper
                                    MealSlotStepper(
                                        label = "D",
                                        color = AppColors.Dinner,
                                        value = d,
                                        onValueChange = { memberMealValues[member.id] = Triple(b, l, it) }
                                    )
                                }
                            }
                        }
                    }
                }

                PrimaryButton(
                    text = "Save All Members Meals",
                    backgroundColor = AppColors.MealSheet,
                    onClick = {
                        val periodId = if (dateString.length >= 7) dateString.substring(0, 7) else selectedPeriodId
                        val records = members.map { m ->
                            val (b, l, d) = memberMealValues[m.id] ?: Triple(1.0, 1.0, 1.0)
                            MealRecord(
                                id = "${periodId}_${dateString}_${m.id}",
                                periodId = periodId,
                                date = dateString,
                                memberId = m.id,
                                memberName = m.name,
                                breakfast = b,
                                lunch = l,
                                dinner = d,
                                totalMeals = b + l + d
                            )
                        }
                        viewModel.saveMeals(records) { onBackClick() }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    testTag = "save_all_meals_button"
                )
            }
        } else {
            // SINGLE MEMBER
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AppCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Select Member", fontWeight = FontWeight.Bold, color = extended.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                                    .clickable { memberDropdownExpanded = true }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .testTag("member_picker_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val currentMemberName = members.firstOrNull { it.id == singleMemberId }?.name ?: "Select Member"
                                Text(text = currentMemberName, color = extended.textPrimary, fontWeight = FontWeight.SemiBold)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = memberDropdownExpanded,
                                onDismissRequest = { memberDropdownExpanded = false }
                            ) {
                                members.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m.name) },
                                        onClick = {
                                            singleMemberId = m.id
                                            memberDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(text = "Breakfast (Orange):", fontWeight = FontWeight.SemiBold, color = AppColors.Breakfast)
                        MealCountSelector(value = singleBreakfast, onSelected = { singleBreakfast = it })

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Lunch (Green):", fontWeight = FontWeight.SemiBold, color = AppColors.Lunch)
                        MealCountSelector(value = singleLunch, onSelected = { singleLunch = it })

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Dinner (Blue):", fontWeight = FontWeight.SemiBold, color = AppColors.Dinner)
                        MealCountSelector(value = singleDinner, onSelected = { singleDinner = it })

                        Spacer(modifier = Modifier.height(20.dp))

                        val singleTotal = singleBreakfast + singleLunch + singleDinner
                        Text(
                            text = "Total Meals: ${CalculationService.formatMeals(singleTotal)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.MealSheet
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = "Save Meal Record",
                    backgroundColor = AppColors.MealSheet,
                    onClick = {
                        val mName = members.firstOrNull { it.id == singleMemberId }?.name ?: "Member"
                        val periodId = if (dateString.length >= 7) dateString.substring(0, 7) else selectedPeriodId
                        val record = MealRecord(
                            id = "${periodId}_${dateString}_$singleMemberId",
                            periodId = periodId,
                            date = dateString,
                            memberId = singleMemberId,
                            memberName = mName,
                            breakfast = singleBreakfast,
                            lunch = singleLunch,
                            dinner = singleDinner,
                            totalMeals = singleBreakfast + singleLunch + singleDinner
                        )
                        viewModel.saveMeals(listOf(record)) { onBackClick() }
                    },
                    testTag = "save_single_meal_button"
                )
            }
        }
    }
}

@Composable
private fun MealSlotStepper(
    label: String,
    color: Color,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        IconButton(
            onClick = { if (value >= 0.5) onValueChange(value - 0.5) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = color, modifier = Modifier.size(16.dp))
        }
        Text(
            text = CalculationService.formatMeals(value),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.width(26.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(
            onClick = { onValueChange(value + 0.5) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = color, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun MealCountSelector(value: Double, onSelected: (Double) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(0.0, 0.5, 1.0, 1.5, 2.0).forEach { num ->
            FilterChip(
                selected = value == num,
                onClick = { onSelected(num) },
                label = { Text(CalculationService.formatMeals(num), fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
