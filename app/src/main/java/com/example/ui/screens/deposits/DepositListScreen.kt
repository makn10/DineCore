package com.example.ui.screens.deposits

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.Deposit
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors

@Composable
fun DepositListScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onNavigateToAddDeposit: () -> Unit
) {
    val extended = LocalExtendedColors.current
    val deposits by viewModel.deposits.collectAsState()
    val totalDepositsPaisa = deposits.sumOf { it.amountPaisa }

    var depositToDelete by remember { mutableStateOf<Deposit?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeaderAppBar(
                title = "Deposits",
                backgroundColor = AppColors.Positive,
                onBackClick = onBackClick,
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("deposits_total_pill")
                    ) {
                        Text(
                            text = "Total: ${CalculationService.formatTaka(totalDepositsPaisa)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            if (deposits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.Payments,
                        title = "No deposits recorded yet!"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 88.dp)
                        .testTag("deposits_list")
                ) {
                    items(deposits, key = { it.id }) { dep ->
                        AppCard(testTag = "deposit_item_${dep.id}") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dep.memberName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = extended.textPrimary
                                    )
                                    Text(
                                        text = "${dep.date} • ${dep.paymentMethod}",
                                        fontSize = 12.sp,
                                        color = extended.textMuted
                                    )
                                    if (dep.note.isNotEmpty()) {
                                        Text(
                                            text = dep.note,
                                            fontSize = 12.sp,
                                            color = extended.textMuted
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = CalculationService.formatTaka(dep.amountPaisa),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = if (dep.amountPaisa >= 0) AppColors.Positive else AppColors.Negative
                                    )
                                    IconButton(onClick = { depositToDelete = dep }) {
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
        }

        Button(
            onClick = onNavigateToAddDeposit,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Positive,
                contentColor = Color.White
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .height(48.dp)
                .testTag("add_deposit_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Add Deposit", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (depositToDelete != null) {
            ConfirmDialog(
                title = "Delete Deposit?",
                message = "Are you sure you want to delete this deposit entry?",
                confirmText = "Delete",
                isDestructive = true,
                onConfirm = {
                    depositToDelete?.let { viewModel.deleteDeposit(it.id) }
                    depositToDelete = null
                },
                onDismiss = { depositToDelete = null }
            )
        }
    }
}
