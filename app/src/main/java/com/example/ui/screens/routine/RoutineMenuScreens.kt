package com.example.ui.screens.routine

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MenuItem
import com.example.data.model.RoutineDay
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.AppTextField
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors

private val DAYS_OF_WEEK = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

@Composable
fun WeeklyMenuScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val extended = LocalExtendedColors.current
    val menuItems by viewModel.weeklyMenu.collectAsState()
    var editingDay by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "Weekly Meal Menu",
            backgroundColor = AppColors.PrimaryDark,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(DAYS_OF_WEEK) { day ->
                val item = menuItems.firstOrNull { it.dayOfWeek.equals(day, ignoreCase = true) }
                AppCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = day, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppColors.PrimaryDark)
                            IconButton(onClick = { editingDay = day }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = AppColors.Primary)
                            }
                        }

                        Text(
                            text = "Breakfast: ${item?.breakfast?.ifBlank { "Not planned" } ?: "Not planned"}",
                            fontSize = 13.sp,
                            color = extended.textPrimary
                        )
                        Text(
                            text = "Lunch: ${item?.lunch?.ifBlank { "Not planned" } ?: "Not planned"}",
                            fontSize = 13.sp,
                            color = extended.textPrimary
                        )
                        Text(
                            text = "Dinner: ${item?.dinner?.ifBlank { "Not planned" } ?: "Not planned"}",
                            fontSize = 13.sp,
                            color = extended.textPrimary
                        )
                    }
                }
            }
        }

        editingDay?.let { day ->
            val existing = menuItems.firstOrNull { it.dayOfWeek.equals(day, ignoreCase = true) }
            var b by remember { mutableStateOf(existing?.breakfast.orEmpty()) }
            var l by remember { mutableStateOf(existing?.lunch.orEmpty()) }
            var d by remember { mutableStateOf(existing?.dinner.orEmpty()) }

            AlertDialog(
                onDismissRequest = { editingDay = null },
                title = { Text("Menu for $day", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        AppTextField(value = b, onValueChange = { b = it }, label = "Breakfast (e.g. Paratha, Egg)")
                        Spacer(modifier = Modifier.height(8.dp))
                        AppTextField(value = l, onValueChange = { l = it }, label = "Lunch (e.g. Rice, Fish, Dal)")
                        Spacer(modifier = Modifier.height(8.dp))
                        AppTextField(value = d, onValueChange = { d = it }, label = "Dinner (e.g. Rice, Chicken)")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val updated = MenuItem(dayOfWeek = day, breakfast = b, lunch = l, dinner = d)
                        viewModel.saveMenuItem(updated)
                        editingDay = null
                    }) {
                        Text("Save", fontWeight = FontWeight.Bold, color = AppColors.Primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingDay = null }) { Text("Cancel") }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun BazarRoutineScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val extended = LocalExtendedColors.current
    val members by viewModel.members.collectAsState()
    val routineDays by viewModel.routine.collectAsState()

    var editingDay by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "Bazar Routine Schedule",
            backgroundColor = AppColors.Charts,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(DAYS_OF_WEEK) { day ->
                val routine = routineDays.firstOrNull { it.dayOfWeek.equals(day, ignoreCase = true) }
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = day, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.Charts)
                            Text(
                                text = "Assigned: ${routine?.memberName?.ifBlank { "None" } ?: "None"}",
                                fontSize = 14.sp,
                                color = extended.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(onClick = { editingDay = day }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Assign", tint = AppColors.Charts)
                        }
                    }
                }
            }
        }

        editingDay?.let { day ->
            var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id.orEmpty()) }
            var dropdownExpanded by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { editingDay = null },
                title = { Text("Assign Shopper for $day", fontWeight = FontWeight.Bold) },
                text = {
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                                .clickable { dropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val memberName = members.firstOrNull { it.id == selectedMemberId }?.name ?: "Select Member"
                            Text(text = memberName, color = extended.textPrimary, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = {
                                        selectedMemberId = m.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val mName = members.firstOrNull { it.id == selectedMemberId }?.name ?: "Shopper"
                        viewModel.saveRoutineDay(RoutineDay(dayOfWeek = day, memberId = selectedMemberId, memberName = mName))
                        editingDay = null
                    }) {
                        Text("Assign", fontWeight = FontWeight.Bold, color = AppColors.Charts)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingDay = null }) { Text("Cancel") }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
