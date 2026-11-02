package com.example.ui.screens.deposits

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.Deposit
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.AppTextField
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.util.currentStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddDepositScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val strings = currentStrings()
    val extended = LocalExtendedColors.current
    val members by viewModel.members.collectAsState()
    val selectedPeriodId by viewModel.selectedPeriodId.collectAsState()

    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id.orEmpty()) }
    var memberDropdownExpanded by remember { mutableStateOf(false) }
    var amountStr by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var dateString by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var note by remember { mutableStateOf("") }

    val paymentMethods = listOf("Cash", "bKash", "Nagad", "Bank")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "Add Deposit",
            backgroundColor = AppColors.Positive,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AppCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Member:", fontWeight = FontWeight.Bold, color = extended.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                                .clickable { memberDropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("deposit_member_picker"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val memberName = members.firstOrNull { it.id == selectedMemberId }?.name ?: "Select Member"
                            Text(text = memberName, color = extended.textPrimary, fontWeight = FontWeight.SemiBold)
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
                                        selectedMemberId = m.id
                                        memberDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AppTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = strings.amount,
                        placeholder = "e.g. 2000 (negative for correction)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = Icons.Default.Payments,
                        testTag = "input_deposit_amount"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = strings.paymentMethod, fontWeight = FontWeight.Bold, color = extended.textPrimary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentMethods.forEach { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(method, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppColors.Positive,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AppTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = "Date (yyyy-MM-dd)",
                        testTag = "input_deposit_date"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AppTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = strings.noteOptional,
                        placeholder = "e.g. November initial meal deposit",
                        testTag = "input_deposit_note"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Save Deposit",
                backgroundColor = AppColors.Positive,
                onClick = {
                    val member = members.firstOrNull { it.id == selectedMemberId }
                    val memberName = member?.name ?: "Member"
                    val amountVal = amountStr.toDoubleOrNull() ?: 0.0
                    val amountPaisa = kotlin.math.round(amountVal * 100).toLong()
                    val periodId = if (dateString.length >= 7) dateString.substring(0, 7) else selectedPeriodId

                    val dep = Deposit(
                        messId = member?.messId.orEmpty(),
                        periodId = periodId,
                        date = dateString,
                        memberId = selectedMemberId,
                        memberName = memberName,
                        amountPaisa = amountPaisa,
                        paymentMethod = selectedMethod,
                        note = note.trim()
                    )

                    viewModel.saveDeposit(dep) {
                        onBackClick()
                    }
                },
                testTag = "submit_deposit_button"
            )
        }
    }
}
