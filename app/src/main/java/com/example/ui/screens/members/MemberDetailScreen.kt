package com.example.ui.screens.members

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calc.CalculationService
import com.example.data.model.MemberRole
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.BalanceText
import com.example.ui.components.EmptyState
import com.example.ui.components.PrimaryButton
import com.example.ui.components.RoleBadge
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.util.currentStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: String,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val strings = currentStrings()
    val extended = LocalExtendedColors.current

    val members by viewModel.members.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val deposits by viewModel.deposits.collectAsState()
    val summary by viewModel.calculationSummary.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    val member = members.firstOrNull { it.id == memberId }
    val calc = summary.memberCalculations.firstOrNull { it.member.id == memberId }

    val memberMeals = meals.filter { it.memberId == memberId }.sortedByDescending { it.date }
    val memberDeposits = deposits.filter { it.memberId == memberId }.sortedByDescending { it.date }
    val memberLogs = auditLogs.filter { it.entityId == memberId || it.details.contains(member?.name.orEmpty()) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Meals", "Deposits", "Expenses", "Timeline")

    var showReportSheet by remember { mutableStateOf(false) }

    if (member == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeaderAppBar(title = "Member Details", onBackClick = onBackClick)
            EmptyState(title = "Member not found")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = member.name,
            onBackClick = onBackClick,
            actions = {
                IconButton(
                    onClick = { showReportSheet = true },
                    modifier = Modifier.testTag("member_report_button")
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = "Report", tint = Color.White)
                }
            }
        )

        // Member Header Card
        AppCard(testTag = "member_header_card") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(AppColors.TealTint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.name.take(1).uppercase(),
                        color = AppColors.PrimaryDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        color = extended.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (member.phone.isNotEmpty()) member.phone else "No phone",
                        color = extended.textMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    RoleBadge(role = member.role)
                }
            }
        }

        // Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = extended.cardBackground,
            contentColor = AppColors.Primary,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_member_${title.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTabIndex) {
            0 -> {
                // OVERVIEW
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    AppCard(testTag = "overview_balance_card") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Current Net Balance",
                                fontSize = 14.sp,
                                color = extended.textMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (calc != null) {
                                BalanceText(
                                    amountPaisa = calc.currentBalancePaisa,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (calc.isDue) "Status: Due (Payment Needed)" else "Status: Advance / Remaining",
                                    color = if (calc.isDue) AppColors.Negative else AppColors.Positive,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    AppCard(testTag = "overview_metrics_card") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            MetricRow("Opening Balance", CalculationService.formatTaka(member.openingBalancePaisa))
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = extended.border.copy(alpha = 0.3f))
                            MetricRow("Total Meals", CalculationService.formatMeals(calc?.totalMeals ?: 0.0))
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = extended.border.copy(alpha = 0.3f))
                            MetricRow("Meal Cost", CalculationService.formatTaka(calc?.mealCostPaisa ?: 0L))
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = extended.border.copy(alpha = 0.3f))
                            MetricRow("Total Deposits", CalculationService.formatTaka(calc?.totalDepositsPaisa ?: 0L))
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = extended.border.copy(alpha = 0.3f))
                            MetricRow("Shared Bills Share", CalculationService.formatTaka(calc?.billsSharePaisa ?: 0L))
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = extended.border.copy(alpha = 0.3f))
                            MetricRow("Personal Extra Costs", CalculationService.formatTaka(calc?.extraCostsSharePaisa ?: 0L))
                        }
                    }

                    PrimaryButton(
                        text = "View Full Financial Report",
                        onClick = { showReportSheet = true },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        testTag = "view_financial_report_button"
                    )
                }
            }
            1 -> {
                // MEALS
                if (memberMeals.isEmpty()) {
                    EmptyState(title = "No meals logged for this month")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(memberMeals) { meal ->
                            AppCard(padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = meal.date, fontWeight = FontWeight.Bold, color = extended.textPrimary)
                                        Text(
                                            text = "B: ${meal.breakfast}  |  L: ${meal.lunch}  |  D: ${meal.dinner}",
                                            fontSize = 12.sp,
                                            color = extended.textMuted
                                        )
                                    }
                                    Text(
                                        text = "${CalculationService.formatMeals(meal.totalMeals)} meals",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppColors.MealSheet,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // DEPOSITS
                if (memberDeposits.isEmpty()) {
                    EmptyState(title = "No deposits recorded yet")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(memberDeposits) { dep ->
                            AppCard(padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = dep.date, fontWeight = FontWeight.Bold, color = extended.textPrimary)
                                        Text(
                                            text = "${dep.paymentMethod}${if (dep.note.isNotEmpty()) " - ${dep.note}" else ""}",
                                            fontSize = 12.sp,
                                            color = extended.textMuted
                                        )
                                    }
                                    Text(
                                        text = CalculationService.formatTaka(dep.amountPaisa),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (dep.amountPaisa >= 0) AppColors.Positive else AppColors.Negative,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // EXPENSES FORMULA
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    AppCard {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Formula Breakdown",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = extended.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "1. Meal Cost Calculation:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AppColors.PrimaryDark
                            )
                            Text(
                                text = "${CalculationService.formatMeals(calc?.totalMeals ?: 0.0)} meals × ${CalculationService.formatMealRate(summary.mealRatePaisa)} = ${CalculationService.formatTaka(calc?.mealCostPaisa ?: 0L)}",
                                fontSize = 13.sp,
                                color = extended.textPrimary,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 12.dp)
                            )

                            Text(
                                text = "2. Shared Bills Share:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AppColors.PrimaryDark
                            )
                            Text(
                                text = "Allocated share of Mess Bills = ${CalculationService.formatTaka(calc?.billsSharePaisa ?: 0L)}",
                                fontSize = 13.sp,
                                color = extended.textPrimary,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 12.dp)
                            )

                            Text(
                                text = "3. Personal Extra Costs:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AppColors.PrimaryDark
                            )
                            Text(
                                text = "Special items/feasts = ${CalculationService.formatTaka(calc?.extraCostsSharePaisa ?: 0L)}",
                                fontSize = 13.sp,
                                color = extended.textPrimary,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 12.dp)
                            )

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                text = "Total Expense = Meal Cost + Bills + Extra Costs\n= ${CalculationService.formatTaka(calc?.totalCostPaisa ?: 0L)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = extended.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Final Balance = Opening Balance + Deposits - Total Expense\n= ${CalculationService.formatTaka(calc?.currentBalancePaisa ?: 0L)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if ((calc?.currentBalancePaisa ?: 0L) >= 0) AppColors.Positive else AppColors.Negative
                            )
                        }
                    }
                }
            }
            4 -> {
                // TIMELINE
                if (memberLogs.isEmpty()) {
                    EmptyState(title = "No audit history recorded")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(memberLogs) { log ->
                            AppCard(padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = log.action, fontWeight = FontWeight.Bold, color = AppColors.PrimaryDark, fontSize = 13.sp)
                                        Text(
                                            text = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(log.timestamp)),
                                            fontSize = 11.sp,
                                            color = extended.textMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = log.details, fontSize = 13.sp, color = extended.textPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Financial Report Bottom Sheet
        if (showReportSheet && calc != null) {
            ModalBottomSheet(
                onDismissRequest = { showReportSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = extended.cardBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Financial Report: ${member.name}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.textPrimary
                    )
                    Text(
                        text = "Period: ${summary.totalMeals} total mess meals | Rate: ${CalculationService.formatMealRate(summary.mealRatePaisa)}",
                        fontSize = 12.sp,
                        color = extended.textMuted,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AppCard {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            MetricRow("Opening Balance", CalculationService.formatTaka(member.openingBalancePaisa))
                            MetricRow("Total Meals", CalculationService.formatMeals(calc.totalMeals))
                            MetricRow("Meal Cost", CalculationService.formatTaka(calc.mealCostPaisa))
                            MetricRow("Total Deposits", CalculationService.formatTaka(calc.totalDepositsPaisa))
                            MetricRow("Shared Bills", CalculationService.formatTaka(calc.billsSharePaisa))
                            MetricRow("Extra Costs", CalculationService.formatTaka(calc.extraCostsSharePaisa))
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            MetricRow("Net Balance", CalculationService.formatTaka(calc.currentBalancePaisa), isBold = true)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = "Share via WhatsApp / Text",
                        onClick = {
                            val shareText = """
                                *Dine Core - Report*
                                Member: ${member.name}
                                Meals: ${CalculationService.formatMeals(calc.totalMeals)}
                                Meal Cost: ${CalculationService.formatTaka(calc.mealCostPaisa)}
                                Deposits: ${CalculationService.formatTaka(calc.totalDepositsPaisa)}
                                Bills Share: ${CalculationService.formatTaka(calc.billsSharePaisa)}
                                Total Cost: ${CalculationService.formatTaka(calc.totalCostPaisa)}
                                *Net Balance: ${CalculationService.formatTaka(calc.currentBalancePaisa)} (${if (calc.isDue) "DUE" else "ADVANCE"})*
                            """.trimIndent()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Report"))
                        },
                        testTag = "share_report_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, isBold: Boolean = false) {
    val extended = LocalExtendedColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isBold) extended.textPrimary else extended.textMuted,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = extended.textPrimary,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}
