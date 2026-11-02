package com.example.ui.screens.bazar

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.BazarEntry
import com.example.data.model.BazarItem
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.AppTextField
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.util.currentStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ItemDraft(
    val name: String = "",
    val quantity: String = "",
    val costStr: String = ""
)

@Composable
fun AddBazarScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val strings = currentStrings()
    val extended = LocalExtendedColors.current
    val members by viewModel.members.collectAsState()
    val selectedPeriodId by viewModel.selectedPeriodId.collectAsState()

    var shopperMemberId by remember { mutableStateOf(members.firstOrNull()?.id.orEmpty()) }
    var shopperDropdownExpanded by remember { mutableStateOf(false) }
    var dateString by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var isAddedToDeposit by remember { mutableStateOf(true) }
    var note by remember { mutableStateOf("") }

    val itemsList = remember {
        mutableStateListOf(
            ItemDraft("Rice", "5 kg", "350"),
            ItemDraft("Chicken", "2 kg", "480")
        )
    }

    // Calculate total cost in paisa
    val totalCostPaisa = itemsList.sumOf {
        val amount = it.costStr.toDoubleOrNull() ?: 0.0
        kotlin.math.round(amount * 100).toLong()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "Add Daily Bazar",
            backgroundColor = AppColors.Primary,
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
                    // Shopper Picker
                    Text(text = "Shopper (Who bought):", fontWeight = FontWeight.Bold, color = extended.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                                .clickable { shopperDropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("shopper_picker_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val shopperName = members.firstOrNull { it.id == shopperMemberId }?.name ?: "Select Shopper"
                            Text(text = shopperName, color = extended.textPrimary, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = shopperDropdownExpanded,
                            onDismissRequest = { shopperDropdownExpanded = false }
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = {
                                        shopperMemberId = m.id
                                        shopperDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date
                    AppTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = "Date (yyyy-MM-dd)",
                        testTag = "input_bazar_date"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items Card
            AppCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Itemized Products",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.textPrimary
                        )
                        Text(
                            text = "Total: ${CalculationService.formatTaka(totalCostPaisa)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.PrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    itemsList.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppTextField(
                                value = item.name,
                                onValueChange = { itemsList[index] = item.copy(name = it) },
                                label = "Item",
                                modifier = Modifier.weight(1.8f),
                                testTag = "item_name_$index"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AppTextField(
                                value = item.quantity,
                                onValueChange = { itemsList[index] = item.copy(quantity = it) },
                                label = "Qty",
                                modifier = Modifier.weight(1f),
                                testTag = "item_qty_$index"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AppTextField(
                                value = item.costStr,
                                onValueChange = { itemsList[index] = item.copy(costStr = it) },
                                label = "৳ Price",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.2f),
                                testTag = "item_price_$index"
                            )
                            IconButton(
                                onClick = {
                                    if (itemsList.size > 1) {
                                        itemsList.removeAt(index)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = AppColors.Negative.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SecondaryButton(
                        text = strings.addItem,
                        onClick = { itemsList.add(ItemDraft("", "", "")) },
                        modifier = Modifier.height(44.dp),
                        testTag = "add_item_row_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add to Deposit Switch Card
            AppCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.addToDeposit,
                                fontWeight = FontWeight.Bold,
                                color = extended.textPrimary
                            )
                            Text(
                                text = strings.addToDepositHint,
                                fontSize = 12.sp,
                                color = extended.textMuted
                            )
                        }
                        Switch(
                            checked = isAddedToDeposit,
                            onCheckedChange = { isAddedToDeposit = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Primary,
                                checkedTrackColor = AppColors.TealTint
                            ),
                            modifier = Modifier.testTag("add_to_deposit_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = "Note (Optional)",
                        placeholder = "e.g. Weekly big market bazar",
                        testTag = "input_bazar_note"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Save Bazar Entry",
                onClick = {
                    val shopper = members.firstOrNull { it.id == shopperMemberId }
                    val shopperName = shopper?.name ?: "Shopper"
                    val periodId = if (dateString.length >= 7) dateString.substring(0, 7) else selectedPeriodId

                    val finalItems = itemsList.filter { it.name.isNotBlank() }.map {
                        val c = it.costStr.toDoubleOrNull() ?: 0.0
                        BazarItem(
                            name = it.name.trim(),
                            quantity = it.quantity.trim(),
                            costPaisa = kotlin.math.round(c * 100).toLong()
                        )
                    }

                    val entry = BazarEntry(
                        messId = shopper?.messId.orEmpty(),
                        periodId = periodId,
                        date = dateString,
                        shopperMemberId = shopperMemberId,
                        shopperName = shopperName,
                        items = finalItems,
                        totalCostPaisa = totalCostPaisa,
                        isAddedToDeposit = isAddedToDeposit,
                        note = note.trim()
                    )

                    viewModel.saveBazarEntry(entry) {
                        onBackClick()
                    }
                },
                testTag = "submit_bazar_button"
            )
        }
    }
}
