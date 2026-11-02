package com.example.ui.screens.summary

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calc.CalculationService
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.BalanceText
import com.example.ui.components.EmptyState
import com.example.ui.components.RoleBadge
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.components.StatTile
import com.example.ui.components.StatTileType
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.util.currentStrings

@Composable
fun MonthSummaryScreen(
    viewModel: MainViewModel,
    onMemberClick: (String) -> Unit
) {
    val context = LocalContext.current
    val strings = currentStrings()
    val extended = LocalExtendedColors.current
    val summary by viewModel.calculationSummary.collectAsState()
    val selectedPeriodId by viewModel.selectedPeriodId.collectAsState()
    val mess by viewModel.currentMess.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "Month Summary",
            backgroundColor = AppColors.Primary,
            actions = {
                IconButton(
                    onClick = {
                        val report = buildString {
                            appendLine("📊 *${mess?.name ?: "Mess"} - Month Summary ($selectedPeriodId)*")
                            appendLine("Total Meals: ${CalculationService.formatMeals(summary.totalMeals)}")
                            appendLine("Bazar Cost: ${CalculationService.formatTaka(summary.totalBazarCostPaisa)}")
                            appendLine("Meal Rate: ${CalculationService.formatMealRate(summary.mealRatePaisa)}")
                            appendLine("Mess Balance: ${CalculationService.formatTaka(summary.messBalancePaisa)}")
                            appendLine("-------------------------------")
                            summary.memberCalculations.forEach { m ->
                                appendLine("👤 *${m.member.name}*")
                                appendLine("  Meals: ${CalculationService.formatMeals(m.totalMeals)} | Cost: ${CalculationService.formatTaka(m.mealCostPaisa)}")
                                appendLine("  Deposits: ${CalculationService.formatTaka(m.totalDepositsPaisa)}")
                                appendLine("  *Balance: ${CalculationService.formatTaka(m.currentBalancePaisa)} (${if (m.isDue) "DUE" else "ADVANCE"})*")
                            }
                        }
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, report)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Month Summary"))
                    },
                    modifier = Modifier.testTag("share_summary_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                StatTile(
                    label = strings.messBalance,
                    value = CalculationService.formatTaka(summary.messBalancePaisa),
                    type = StatTileType.BALANCE
                )
                StatTile(
                    label = strings.mealRate,
                    value = CalculationService.formatMealRate(summary.mealRatePaisa),
                    type = StatTileType.RATE
                )
            }

            if (summary.memberCalculations.isEmpty()) {
                item {
                    EmptyState(title = "No calculation data for this period")
                }
            } else {
                item {
                    Text(
                        text = "Members Breakdown",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.textPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                items(summary.memberCalculations, key = { it.member.id }) { mCalc ->
                    AppCard(
                        modifier = Modifier.testTag("summary_member_${mCalc.member.id}")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mCalc.member.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = extended.textPrimary
                                )
                                RoleBadge(role = mCalc.member.role)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Meals", fontSize = 11.sp, color = extended.textMuted)
                                    Text(
                                        text = CalculationService.formatMeals(mCalc.totalMeals),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = extended.textPrimary
                                    )
                                }
                                Column {
                                    Text("Meal Cost", fontSize = 11.sp, color = extended.textMuted)
                                    Text(
                                        text = CalculationService.formatTaka(mCalc.mealCostPaisa),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = extended.textPrimary
                                    )
                                }
                                Column {
                                    Text("Deposits", fontSize = 11.sp, color = extended.textMuted)
                                    Text(
                                        text = CalculationService.formatTaka(mCalc.totalDepositsPaisa),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = extended.textPrimary
                                    )
                                }
                                Column {
                                    Text("Bills", fontSize = 11.sp, color = extended.textMuted)
                                    Text(
                                        text = CalculationService.formatTaka(mCalc.billsSharePaisa + mCalc.extraCostsSharePaisa),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = extended.textPrimary
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = extended.border.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (mCalc.isDue) "Due Amount" else "Advance / Remaining",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (mCalc.isDue) AppColors.Negative else AppColors.Positive
                                )
                                BalanceText(
                                    amountPaisa = mCalc.currentBalancePaisa,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
