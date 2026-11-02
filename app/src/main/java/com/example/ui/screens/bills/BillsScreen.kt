package com.example.ui.screens.bills

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calc.CalculationService
import com.example.data.model.BillSplitMode
import com.example.data.model.ExtraCost
import com.example.data.model.MonthlyBill
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.AppTextField
import com.example.ui.components.EmptyState
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillsScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val extended = LocalExtendedColors.current
    val members by viewModel.members.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val extraCosts by viewModel.extraCosts.collectAsState()
    val selectedPeriodId by viewModel.selectedPeriodId.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Monthly Bills, 1: Extra Costs

    var showAddBillDialog by remember { mutableStateOf(false) }
    var showAddExtraCostDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "Bills & Extra Costs",
            backgroundColor = AppColors.Indigo,
            onBackClick = onBackClick
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = extended.cardBackground,
            contentColor = AppColors.Indigo
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Monthly Bills", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_monthly_bills")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Special Extra Costs", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_extra_costs")
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedTab == 0) {
                // MONTHLY BILLS
                if (bills.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                        EmptyState(icon = Icons.Default.ReceiptLong, title = "No monthly bills added yet")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 88.dp)
                    ) {
                        items(bills, key = { it.id }) { bill ->
                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = bill.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = extended.textPrimary)
                                        Text(
                                            text = "Category: ${bill.category} • Split: ${bill.splitMode.name}",
                                            fontSize = 12.sp,
                                            color = extended.textMuted
                                        )
                                        if (bill.note.isNotEmpty()) {
                                            Text(text = bill.note, fontSize = 12.sp, color = extended.textMuted)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = CalculationService.formatTaka(bill.amountPaisa),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = AppColors.Indigo
                                        )
                                        IconButton(onClick = { viewModel.deleteBill(bill.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = AppColors.Negative.copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // EXTRA COSTS
                if (extraCosts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                        EmptyState(icon = Icons.Default.ReceiptLong, title = "No extra costs recorded yet")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 88.dp)
                    ) {
                        items(extraCosts, key = { it.id }) { cost ->
                            val participantsCount = cost.participantMemberIds.size.coerceAtLeast(1)
                            val perPersonPaisa = cost.amountPaisa / participantsCount

                            AppCard {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = cost.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = extended.textPrimary)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = CalculationService.formatTaka(cost.amountPaisa),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = AppColors.Indigo
                                            )
                                            IconButton(onClick = { viewModel.deleteExtraCost(cost.id) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = AppColors.Negative.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${cost.date} • $participantsCount participant(s) (${CalculationService.formatTaka(perPersonPaisa)} each)",
                                        fontSize = 12.sp,
                                        color = extended.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Add Button
            Button(
                onClick = {
                    if (selectedTab == 0) showAddBillDialog = true else showAddExtraCostDialog = true
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Indigo, contentColor = Color.White),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .height(48.dp)
                    .testTag("add_bill_or_cost_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = if (selectedTab == 0) "Add Bill" else "Add Extra Cost", fontWeight = FontWeight.Bold)
            }
        }

        // Add Bill Dialog
        if (showAddBillDialog) {
            AddBillDialog(
                onDismiss = { showAddBillDialog = false },
                onSave = { name, category, amountPaisa, splitMode, note ->
                    val bill = MonthlyBill(
                        periodId = selectedPeriodId,
                        name = name,
                        category = category,
                        amountPaisa = amountPaisa,
                        splitMode = splitMode,
                        note = note
                    )
                    viewModel.saveBill(bill)
                    showAddBillDialog = false
                }
            )
        }

        // Add Extra Cost Dialog
        if (showAddExtraCostDialog) {
            AddExtraCostDialog(
                members = members,
                onDismiss = { showAddExtraCostDialog = false },
                onSave = { title, amountPaisa, participants, date, note ->
                    val cost = ExtraCost(
                        periodId = selectedPeriodId,
                        title = title,
                        amountPaisa = amountPaisa,
                        participantMemberIds = participants,
                        date = date,
                        note = note
                    )
                    viewModel.saveExtraCost(cost)
                    showAddExtraCostDialog = false
                }
            )
        }
    }
}

@Composable
private fun AddBillDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long, BillSplitMode, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Gas") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val categories = listOf("Gas", "Electricity", "Maid", "Internet", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Monthly Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AppTextField(value = name, onValueChange = { name = it }, label = "Bill Name (e.g. Gas Bill)")
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = "Amount (৳)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("Category:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = note, onValueChange = { note = it }, label = "Note (Optional)")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                onSave(name.ifBlank { "$category Bill" }, category, kotlin.math.round(amount * 100).toLong(), BillSplitMode.EQUAL, note)
            }) {
                Text("Save", fontWeight = FontWeight.Bold, color = AppColors.Indigo)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun AddExtraCostDialog(
    members: List<com.example.data.model.Member>,
    onDismiss: () -> Unit,
    onSave: (String, Long, List<String>, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var note by remember { mutableStateOf("") }
    val selectedParticipantIds = remember { mutableStateListOf<String>().apply { addAll(members.map { it.id }) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Special Extra Cost", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AppTextField(value = title, onValueChange = { title = it }, label = "Title (e.g. Chicken Feast)")
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = "Total Cost (৳)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = dateString, onValueChange = { dateString = it }, label = "Date (yyyy-MM-dd)")
                Spacer(modifier = Modifier.height(10.dp))
                Text("Participants (Who shares):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                members.forEach { m ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (selectedParticipantIds.contains(m.id)) selectedParticipantIds.remove(m.id) else selectedParticipantIds.add(m.id)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedParticipantIds.contains(m.id),
                            onCheckedChange = { checked ->
                                if (checked) selectedParticipantIds.add(m.id) else selectedParticipantIds.remove(m.id)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = AppColors.Indigo)
                        )
                        Text(text = m.name, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = note, onValueChange = { note = it }, label = "Note (Optional)")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                onSave(title.ifBlank { "Special Cost" }, kotlin.math.round(amount * 100).toLong(), selectedParticipantIds.toList(), dateString, note)
            }) {
                Text("Save", fontWeight = FontWeight.Bold, color = AppColors.Indigo)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
