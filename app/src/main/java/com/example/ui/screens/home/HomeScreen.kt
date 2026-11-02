package com.example.ui.screens.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calc.CalculationService
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.BalanceText
import com.example.ui.components.MealSlotBox
import com.example.ui.components.StatTile
import com.example.ui.components.StatTileType
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.util.currentStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToAddMeal: () -> Unit,
    onNavigateToAddBazar: () -> Unit,
    onNavigateToAddDeposit: () -> Unit,
    onNavigateToAllMembers: () -> Unit,
    onNavigateToMealSheet: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToMemberDetail: (String) -> Unit
) {
    val strings = currentStrings()
    val extended = LocalExtendedColors.current

    val mess by viewModel.currentMess.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val periods by viewModel.periods.collectAsState()
    val selectedPeriodId by viewModel.selectedPeriodId.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val extraCosts by viewModel.extraCosts.collectAsState()
    val summary by viewModel.calculationSummary.collectAsState()

    var showQuickActions by remember { mutableStateOf(false) }
    var periodDropdownExpanded by remember { mutableStateOf(false) }

    // Today's date calculations
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayMealsList = meals.filter { it.date == todayDateStr }
    val todayBreakfast = todayMealsList.sumOf { it.breakfast }
    val todayLunch = todayMealsList.sumOf { it.lunch }
    val todayDinner = todayMealsList.sumOf { it.dinner }

    // Find my member calculation
    val currentUserId = viewModel.currentUser.value?.uid.orEmpty()
    val myCalc = summary.memberCalculations.firstOrNull { it.member.userId == currentUserId }
    val myExtraCosts = extraCosts.filter { it.participantMemberIds.contains(myCalc?.member?.id) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Primary)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("drawer_menu_button")
                ) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mess?.name ?: strings.appName,
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Period Selector
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { periodDropdownExpanded = true }
                                .padding(vertical = 2.dp)
                                .testTag("period_selector_button")
                        ) {
                            Text(
                                text = periods.firstOrNull { it.id == selectedPeriodId }?.name ?: selectedPeriodId,
                                color = AppColors.TealTint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = AppColors.TealTint,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = periodDropdownExpanded,
                            onDismissRequest = { periodDropdownExpanded = false }
                        ) {
                            periods.forEach { period ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = period.name + if (period.isClosed) " (Closed)" else "",
                                            fontWeight = if (period.id == selectedPeriodId) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectPeriod(period.id)
                                        periodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onNavigateToNotices,
                    modifier = Modifier.testTag("notice_bell_button")
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notices", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // StatTiles: Mess Balance & Meal Rate
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

            // Today's Meals Section
            AppCard(testTag = "today_meals_card") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.todayMeals,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.textPrimary
                        )
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
                            fontSize = 13.sp,
                            color = extended.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MealSlotBox(
                            label = strings.breakfast,
                            count = CalculationService.formatMeals(todayBreakfast),
                            color = AppColors.Breakfast
                        )
                        MealSlotBox(
                            label = strings.lunch,
                            count = CalculationService.formatMeals(todayLunch),
                            color = AppColors.Lunch
                        )
                        MealSlotBox(
                            label = strings.dinner,
                            count = CalculationService.formatMeals(todayDinner),
                            color = AppColors.Dinner
                        )
                    }
                }
            }

            // My Summary Card
            if (myCalc != null) {
                AppCard(
                    modifier = Modifier.clickable {
                        onNavigateToMemberDetail(myCalc.member.id)
                    },
                    testTag = "my_summary_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.mySummary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = extended.textPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = extended.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = strings.totalMeals, fontSize = 13.sp, color = extended.textMuted)
                                Text(
                                    text = CalculationService.formatMeals(myCalc.totalMeals),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.textPrimary
                                )
                            }
                            Column {
                                Text(text = strings.mealCost, fontSize = 13.sp, color = extended.textMuted)
                                Text(
                                    text = CalculationService.formatTaka(myCalc.mealCostPaisa),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.textPrimary
                                )
                            }
                            Column {
                                Text(text = strings.myDeposits, fontSize = 13.sp, color = extended.textMuted)
                                Text(
                                    text = CalculationService.formatTaka(myCalc.totalDepositsPaisa),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.textPrimary
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = extended.border.copy(alpha = 0.4f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.currentBalance,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = extended.textPrimary
                            )
                            BalanceText(
                                amountPaisa = myCalc.currentBalancePaisa,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // My Personal Expenses Card
            AppCard(testTag = "my_personal_expenses_card") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.myPersonalExpenses,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (myExtraCosts.isEmpty()) {
                        Text(
                            text = strings.noPersonalExpenses,
                            fontSize = 13.sp,
                            color = extended.textMuted
                        )
                    } else {
                        myExtraCosts.forEach { cost ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cost.title,
                                    fontSize = 14.sp,
                                    color = extended.textPrimary
                                )
                                Text(
                                    text = CalculationService.formatTaka(cost.amountPaisa / cost.participantMemberIds.size.coerceAtLeast(1)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Quick Action Button
        FloatingActionButton(
            onClick = { showQuickActions = true },
            containerColor = AppColors.Primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 96.dp)
                .testTag("floating_quick_actions_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Quick Actions")
        }

        // Quick Actions Modal Bottom Sheet
        if (showQuickActions) {
            ModalBottomSheet(
                onDismissRequest = { showQuickActions = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = extended.cardBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = strings.quickActions,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.textPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val actions = listOf(
                        QuickActionItem("Add Daily Meal", Icons.Default.Restaurant, AppColors.MealSheet) {
                            showQuickActions = false
                            onNavigateToAddMeal()
                        },
                        QuickActionItem("Add Daily Bazar", Icons.Default.ShoppingCart, AppColors.Primary) {
                            showQuickActions = false
                            onNavigateToAddBazar()
                        },
                        QuickActionItem("Add Deposit", Icons.Default.Payments, AppColors.Positive) {
                            showQuickActions = false
                            onNavigateToAddDeposit()
                        },
                        QuickActionItem("All Members", Icons.Default.Group, AppColors.Roles) {
                            showQuickActions = false
                            onNavigateToAllMembers()
                        },
                        QuickActionItem("Meal Sheet Matrix", Icons.Default.TableChart, AppColors.MealSheet) {
                            showQuickActions = false
                            onNavigateToMealSheet()
                        },
                        QuickActionItem("Monthly Bills & Costs", Icons.Default.ReceiptLong, AppColors.Indigo) {
                            showQuickActions = false
                            onNavigateToBills()
                        },
                        QuickActionItem("Notice Board", Icons.Default.Notifications, AppColors.Warning) {
                            showQuickActions = false
                            onNavigateToNotices()
                        },
                        QuickActionItem("Weekly Meal Menu", Icons.Default.DateRange, AppColors.PrimaryDark) {
                            showQuickActions = false
                            onNavigateToMenu()
                        },
                        QuickActionItem("Bazar Routine", Icons.Default.Schedule, AppColors.Charts) {
                            showQuickActions = false
                            onNavigateToRoutine()
                        }
                    )

                    // 3x3 Grid
                    Column {
                        actions.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowItems.forEach { item ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { item.action() }
                                            .padding(4.dp)
                                            .testTag("quick_action_${item.title.replace(" ", "_").lowercase()}"),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(item.color.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = item.color,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = extended.textPrimary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                                if (rowItems.size < 3) {
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val action: () -> Unit
)
