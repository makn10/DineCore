package com.example.ui.screens.charts

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calc.CalculationService
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors

@Composable
fun ChartsScreen(viewModel: MainViewModel) {
    val extended = LocalExtendedColors.current
    val summary by viewModel.calculationSummary.collectAsState()
    val bazarEntries by viewModel.bazarEntries.collectAsState()

    val totalExpensesPaisa = summary.totalBazarCostPaisa + summary.totalBillsPaisa + summary.totalExtraCostsPaisa
    val bazarPercent = if (totalExpensesPaisa > 0) (summary.totalBazarCostPaisa.toFloat() / totalExpensesPaisa) else 0f
    val billsPercent = if (totalExpensesPaisa > 0) (summary.totalBillsPaisa.toFloat() / totalExpensesPaisa) else 0f
    val extraPercent = if (totalExpensesPaisa > 0) (summary.totalExtraCostsPaisa.toFloat() / totalExpensesPaisa) else 0f

    // Daily statistics
    val highestBazarEntry = bazarEntries.maxByOrNull { it.totalCostPaisa }
    val uniqueBazarDays = bazarEntries.map { it.date }.distinct().size.coerceAtLeast(1)
    val dailyAvgBazarPaisa = if (uniqueBazarDays > 0) summary.totalBazarCostPaisa / uniqueBazarDays else 0L

    // Due vs Advance members
    val advanceMembers = summary.memberCalculations.filter { !it.isDue }
    val dueMembers = summary.memberCalculations.filter { it.isDue }
    val totalDuePaisa = dueMembers.sumOf { kotlin.math.abs(it.currentBalancePaisa) }
    val totalAdvancePaisa = advanceMembers.sumOf { it.currentBalancePaisa }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "Analytics & Reports",
            backgroundColor = AppColors.Charts
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Expense Distribution Card
            AppCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Expense Breakdown",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        if (bazarPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(bazarPercent.coerceAtLeast(0.01f))
                                    .height(16.dp)
                                    .background(AppColors.Primary)
                            )
                        }
                        if (billsPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(billsPercent.coerceAtLeast(0.01f))
                                    .height(16.dp)
                                    .background(AppColors.Indigo)
                            )
                        }
                        if (extraPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(extraPercent.coerceAtLeast(0.01f))
                                    .height(16.dp)
                                    .background(AppColors.Warning)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LegendRow("Bazar Expenses (${(bazarPercent * 100).toInt()}%)", CalculationService.formatTaka(summary.totalBazarCostPaisa), AppColors.Primary)
                    LegendRow("Monthly Bills (${(billsPercent * 100).toInt()}%)", CalculationService.formatTaka(summary.totalBillsPaisa), AppColors.Indigo)
                    LegendRow("Extra Costs (${(extraPercent * 100).toInt()}%)", CalculationService.formatTaka(summary.totalExtraCostsPaisa), AppColors.Warning)
                }
            }

            // Stat Cards: Highest day & Daily average
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.TealTint)
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Highest Bazar Day", fontSize = 12.sp, color = AppColors.PrimaryDark, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CalculationService.formatTaka(highestBazarEntry?.totalCostPaisa ?: 0L),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.PrimaryDark
                        )
                        Text(
                            text = highestBazarEntry?.date ?: "N/A",
                            fontSize = 11.sp,
                            color = extended.textMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.RateTint)
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Daily Avg Bazar", fontSize = 12.sp, color = AppColors.RateText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CalculationService.formatTaka(dailyAvgBazarPaisa),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.RateText
                        )
                        Text(
                            text = "$uniqueBazarDays active market days",
                            fontSize = 11.sp,
                            color = extended.textMuted
                        )
                    }
                }
            }

            // Member Balance Health
            AppCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Member Accounts Health",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("In Advance", fontSize = 13.sp, color = AppColors.Positive, fontWeight = FontWeight.Bold)
                            Text("${advanceMembers.size} member(s)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Total: ${CalculationService.formatTaka(totalAdvancePaisa)}", fontSize = 12.sp, color = extended.textMuted)
                        }
                        Column {
                            Text("Due Payment", fontSize = 13.sp, color = AppColors.Negative, fontWeight = FontWeight.Bold)
                            Text("${dueMembers.size} member(s)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Total: ${CalculationService.formatTaka(totalDuePaisa)}", fontSize = 12.sp, color = extended.textMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(label: String, value: String, color: Color) {
    val extended = LocalExtendedColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, color = extended.textPrimary)
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extended.textPrimary)
    }
}
