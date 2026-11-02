package com.example.ui.screens.bazar

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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import com.example.data.model.BazarEntry
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors

@Composable
fun BazarListScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onNavigateToAddBazar: () -> Unit
) {
    val extended = LocalExtendedColors.current
    val bazarEntries by viewModel.bazarEntries.collectAsState()
    val totalBazarPaisa = bazarEntries.sumOf { it.totalCostPaisa }

    var entryToDelete by remember { mutableStateOf<BazarEntry?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with "Total: X ৳" pill on top right matching screenshot
            SectionHeaderAppBar(
                title = "Bazar List",
                backgroundColor = AppColors.Primary,
                onBackClick = onBackClick,
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("bazar_total_pill")
                    ) {
                        Text(
                            text = "Total: ${CalculationService.formatTaka(totalBazarPaisa)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            if (bazarEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.ShoppingCart,
                        title = "No bazar items yet!"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 88.dp)
                        .testTag("bazar_list")
                ) {
                    items(bazarEntries, key = { it.id }) { entry ->
                        AppCard(testTag = "bazar_entry_${entry.id}") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = entry.shopperName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = extended.textPrimary
                                        )
                                        Text(
                                            text = entry.date,
                                            fontSize = 12.sp,
                                            color = extended.textMuted
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = CalculationService.formatTaka(entry.totalCostPaisa),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = AppColors.PrimaryDark
                                        )
                                        IconButton(onClick = { entryToDelete = entry }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = AppColors.Negative.copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                if (entry.items.isNotEmpty()) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = extended.border.copy(alpha = 0.3f))
                                    entry.items.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${item.name}${if (item.quantity.isNotEmpty()) " (${item.quantity})" else ""}",
                                                fontSize = 13.sp,
                                                color = extended.textPrimary
                                            )
                                            Text(
                                                text = CalculationService.formatTaka(item.costPaisa),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = extended.textPrimary
                                            )
                                        }
                                    }
                                }

                                if (entry.isAddedToDeposit) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppColors.TealTint)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "✓ Added to deposit",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.PrimaryDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Add Bazar Button matching screenshot
        Button(
            onClick = onNavigateToAddBazar,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary,
                contentColor = Color.White
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .height(48.dp)
                .testTag("add_bazar_button")
        ) {
            Icon(
                imageVector = Icons.Default.AddShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Add Bazar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (entryToDelete != null) {
            ConfirmDialog(
                title = "Delete Bazar Entry?",
                message = "Are you sure you want to delete this bazar entry? If it was added to deposit, that deposit will also be removed.",
                confirmText = "Delete",
                isDestructive = true,
                onConfirm = {
                    entryToDelete?.let { viewModel.deleteBazarEntry(it) }
                    entryToDelete = null
                },
                onDismiss = { entryToDelete = null }
            )
        }
    }
}
