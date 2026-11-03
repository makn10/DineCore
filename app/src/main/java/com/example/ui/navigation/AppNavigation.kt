package com.example.ui.navigation

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.MainViewModel
import com.example.ui.components.NavTab
import com.example.ui.components.PillNavBar
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.MessSetupScreen
import com.example.ui.screens.bazar.AddBazarScreen
import com.example.ui.screens.bazar.BazarListScreen
import com.example.ui.screens.bills.BillsScreen
import com.example.ui.screens.charts.ChartsScreen
import com.example.ui.screens.deposits.AddDepositScreen
import com.example.ui.screens.deposits.DepositListScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.meals.AddMealScreen
import com.example.ui.screens.meals.MealSheetScreen
import com.example.ui.screens.members.AllMembersScreen
import com.example.ui.screens.members.MemberDetailScreen
import com.example.ui.screens.notices.NoticeBoardScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.routine.BazarRoutineScreen
import com.example.ui.screens.routine.WeeklyMenuScreen
import com.example.ui.screens.summary.MonthSummaryScreen
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.util.AppLanguage
import com.example.ui.util.currentStrings
import kotlinx.coroutines.launch

sealed class Screen {
    object MainPill : Screen()
    object AllMembers : Screen()
    data class MemberDetail(val memberId: String) : Screen()
    object MealSheet : Screen()
    object AddMeal : Screen()
    object BazarList : Screen()
    object AddBazar : Screen()
    object DepositList : Screen()
    object AddDeposit : Screen()
    object Bills : Screen()
    object Notices : Screen()
    object WeeklyMenu : Screen()
    object BazarRoutine : Screen()
    object MessSetup : Screen()
    object Login : Screen()
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    currentThemeMode: String,
    onThemeModeChange: (String) -> Unit
) {
    val strings = currentStrings()
    val extended = LocalExtendedColors.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentUser by viewModel.currentUser.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val mess by viewModel.currentMess.collectAsState()

    var selectedNavTab by remember { mutableStateOf(NavTab.HOME) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainPill) }

    // Routing decision
    val effectiveScreen = when {
        currentUser == null && userProfile == null -> Screen.Login
        currentScreen == Screen.Login -> Screen.Login
        currentScreen == Screen.MessSetup -> Screen.MessSetup
        mess == null && userProfile?.activeMessId.isNullOrEmpty() && currentScreen !is Screen.MainPill -> Screen.MessSetup
        else -> currentScreen
    }

    // BackHandler for sub-screens
    if (effectiveScreen !is Screen.MainPill && effectiveScreen !is Screen.Login) {
        BackHandler {
            currentScreen = Screen.MainPill
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = effectiveScreen is Screen.MainPill,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = extended.cardBackground,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Drawer Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.Primary)
                            .statusBarsPadding()
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = Color.White)
                                }
                                IconButton(onClick = { coroutineScope.launch { drawerState.close() } }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = mess?.name ?: strings.appName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Code: ${mess?.inviteCode.orEmpty()}",
                                color = AppColors.TealTint,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Drawer Items
                    DrawerItem(icon = Icons.Default.Home, title = "Home Dashboard", isSelected = effectiveScreen is Screen.MainPill && selectedNavTab == NavTab.HOME) {
                        selectedNavTab = NavTab.HOME
                        currentScreen = Screen.MainPill
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.Group, title = "All Members") {
                        currentScreen = Screen.AllMembers
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.TableChart, title = "Meal Sheet Matrix") {
                        currentScreen = Screen.MealSheet
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.ShoppingCart, title = "Daily Bazar List") {
                        currentScreen = Screen.BazarList
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.Payments, title = "Deposits") {
                        currentScreen = Screen.DepositList
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.ReceiptLong, title = "Monthly Bills & Extra Costs") {
                        currentScreen = Screen.Bills
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.Notifications, title = "Notice Board") {
                        currentScreen = Screen.Notices
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.DateRange, title = "Weekly Meal Menu") {
                        currentScreen = Screen.WeeklyMenu
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.Schedule, title = "Bazar Routine Schedule") {
                        currentScreen = Screen.BazarRoutine
                        coroutineScope.launch { drawerState.close() }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = extended.border.copy(alpha = 0.3f))
                    DrawerItem(icon = Icons.Default.Assessment, title = "Month Summary", isSelected = effectiveScreen is Screen.MainPill && selectedNavTab == NavTab.SUMMARY) {
                        selectedNavTab = NavTab.SUMMARY
                        currentScreen = Screen.MainPill
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.BarChart, title = "Analytics & Reports", isSelected = effectiveScreen is Screen.MainPill && selectedNavTab == NavTab.CHARTS) {
                        selectedNavTab = NavTab.CHARTS
                        currentScreen = Screen.MainPill
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.History, title = "History & Audit Log", isSelected = effectiveScreen is Screen.MainPill && selectedNavTab == NavTab.HISTORY) {
                        selectedNavTab = NavTab.HISTORY
                        currentScreen = Screen.MainPill
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem(icon = Icons.Default.Person, title = "Profile & Settings", isSelected = effectiveScreen is Screen.MainPill && selectedNavTab == NavTab.PROFILE) {
                        selectedNavTab = NavTab.PROFILE
                        currentScreen = Screen.MainPill
                        coroutineScope.launch { drawerState.close() }
                    }
                }
            }
        }
    ) {
        when (val screen = effectiveScreen) {
            Screen.Login -> {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToHome = { currentScreen = Screen.MainPill },
                    onNavigateToMessSetup = { currentScreen = Screen.MessSetup },
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange
                )
            }
            Screen.MessSetup -> {
                MessSetupScreen(
                    viewModel = viewModel,
                    onMessReady = { currentScreen = Screen.MainPill }
                )
            }
            Screen.MainPill -> {
                Scaffold(
                    bottomBar = {
                        PillNavBar(
                            selectedTab = selectedNavTab,
                            onTabSelected = { selectedNavTab = it }
                        )
                    },
                    containerColor = extended.screenBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding() - 76.dp)
                    ) {
                        when (selectedNavTab) {
                            NavTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                                onNavigateToAddMeal = { currentScreen = Screen.AddMeal },
                                onNavigateToAddBazar = { currentScreen = Screen.AddBazar },
                                onNavigateToAddDeposit = { currentScreen = Screen.AddDeposit },
                                onNavigateToAllMembers = { currentScreen = Screen.AllMembers },
                                onNavigateToMealSheet = { currentScreen = Screen.MealSheet },
                                onNavigateToBills = { currentScreen = Screen.Bills },
                                onNavigateToNotices = { currentScreen = Screen.Notices },
                                onNavigateToMenu = { currentScreen = Screen.WeeklyMenu },
                                onNavigateToRoutine = { currentScreen = Screen.BazarRoutine },
                                onNavigateToMemberDetail = { mId -> currentScreen = Screen.MemberDetail(mId) }
                            )
                            NavTab.CHARTS -> ChartsScreen(viewModel = viewModel)
                            NavTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                            NavTab.SUMMARY -> MonthSummaryScreen(
                                viewModel = viewModel,
                                onMemberClick = { mId -> currentScreen = Screen.MemberDetail(mId) }
                            )
                            NavTab.PROFILE -> ProfileScreen(
                                viewModel = viewModel,
                                currentLanguage = currentLanguage,
                                onLanguageChange = onLanguageChange,
                                currentThemeMode = currentThemeMode,
                                onThemeModeChange = onThemeModeChange,
                                onSignedOut = { currentScreen = Screen.Login }
                            )
                        }
                    }
                }
            }
            Screen.AllMembers -> AllMembersScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainPill },
                onMemberClick = { mId -> currentScreen = Screen.MemberDetail(mId) }
            )
            is Screen.MemberDetail -> MemberDetailScreen(
                memberId = screen.memberId,
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainPill }
            )
            Screen.MealSheet -> MealSheetScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainPill },
                onNavigateToAddMeal = { currentScreen = Screen.AddMeal }
            )
            Screen.AddMeal -> AddMealScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MealSheet }
            )
            Screen.BazarList -> BazarListScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainPill },
                onNavigateToAddBazar = { currentScreen = Screen.AddBazar }
            )
            Screen.AddBazar -> AddBazarScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.BazarList }
            )
            Screen.DepositList -> DepositListScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainPill },
                onNavigateToAddDeposit = { currentScreen = Screen.AddDeposit }
            )
            Screen.AddDeposit -> AddDepositScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.DepositList }
            )
            Screen.Bills -> BillsScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainPill }
            )
            Screen.Notices -> NoticeBoardScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainPill }
            )
            Screen.WeeklyMenu -> WeeklyMenuScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainPill }
            )
            Screen.BazarRoutine -> BazarRoutineScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainPill }
            )
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val extended = LocalExtendedColors.current
    val color = if (isSelected) AppColors.Primary else extended.textPrimary
    val bg = if (isSelected) AppColors.TealTint else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
