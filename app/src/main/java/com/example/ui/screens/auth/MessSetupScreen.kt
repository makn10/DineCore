package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.AppTextField
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.util.currentStrings

@Composable
fun MessSetupScreen(
    viewModel: MainViewModel,
    onMessReady: () -> Unit
) {
    val strings = currentStrings()
    val extended = LocalExtendedColors.current
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Create, 1: Join

    // Create Mess fields
    var messName by remember { mutableStateOf("") }
    var messAddress by remember { mutableStateOf("") }
    var creatorName by remember { mutableStateOf("") }
    var creatorPhone by remember { mutableStateOf("") }

    // Join Mess fields
    var inviteCode by remember { mutableStateOf("") }
    var joinMemberName by remember { mutableStateOf("") }
    var joinMemberPhone by remember { mutableStateOf("") }

    // Pre-populate with user profile details if available
    LaunchedEffect(userProfile) {
        userProfile?.let { prof ->
            if (creatorName.isBlank() && prof.displayName.isNotBlank()) {
                creatorName = prof.displayName
            }
            if (creatorPhone.isBlank() && prof.phone.isNotBlank()) {
                creatorPhone = prof.phone
            }
            if (joinMemberName.isBlank() && prof.displayName.isNotBlank()) {
                joinMemberName = prof.displayName
            }
            if (joinMemberPhone.isBlank() && prof.phone.isNotBlank()) {
                joinMemberPhone = prof.phone
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // App Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AppColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = strings.appName,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Welcome to ${strings.appName}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (strings.isBangla)
                "নতুন মেস তৈরি করুন অথবা ইনভাইট কোড দিয়ে যুক্ত হোন"
            else
                "Create a new mess or join an existing one using an invite code",
            fontSize = 14.sp,
            color = extended.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = extended.cardBackground,
            contentColor = AppColors.Primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0; viewModel.clearMessages() },
                text = {
                    Text(
                        text = if (strings.isBangla) "মেস তৈরি করুন" else "Create Mess",
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("tab_create_mess")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1; viewModel.clearMessages() },
                text = {
                    Text(
                        text = if (strings.isBangla) "কোড দিয়ে যোগ দিন" else "Join with Code",
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("tab_join_mess")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AppCard(testTag = "mess_setup_card") {
            if (selectedTab == 0) {
                // CREATE MESS
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (strings.isBangla) "মেসের বিবরণ" else "Mess Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppTextField(
                        value = messName,
                        onValueChange = {
                            messName = it
                            if (uiState.errorMessage != null) viewModel.clearMessages()
                        },
                        label = if (strings.isBangla) "মেসের নাম (উদাঃ গ্রিন হোস্টেল)" else "Mess Name (e.g. Green Hostel)",
                        leadingIcon = Icons.Default.Home,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_mess_name"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = messAddress,
                        onValueChange = { messAddress = it },
                        label = if (strings.isBangla) "ঠিকানা / ফ্ল্যাট নং" else "Address / Flat No.",
                        leadingIcon = Icons.Default.LocationOn,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_mess_address"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = creatorName,
                        onValueChange = {
                            creatorName = it
                            if (uiState.errorMessage != null) viewModel.clearMessages()
                        },
                        label = if (strings.isBangla) "আপনার নাম (ম্যানেজার)" else "Your Name (Manager)",
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_creator_name"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = creatorPhone,
                        onValueChange = { creatorPhone = it },
                        label = if (strings.isBangla) "আপনার ফোন নম্বর" else "Your Phone",
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        testTag = "input_creator_phone"
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.Negative.copy(alpha = 0.12f))
                                .border(1.dp, AppColors.Negative.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                color = AppColors.Negative,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = if (strings.isBangla) "মেস তৈরি করুন ও এগিয়ে যান" else "Create Mess & Continue",
                        isLoading = uiState.isLoading,
                        onClick = {
                            viewModel.createMess(messName, messAddress, creatorName, creatorPhone) {
                                onMessReady()
                            }
                        },
                        testTag = "create_mess_submit_button"
                    )
                }
            } else {
                // JOIN MESS
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (strings.isBangla) "বিদ্যমান মেসে যোগ দিন" else "Join Existing Mess",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppTextField(
                        value = inviteCode,
                        onValueChange = {
                            inviteCode = it.uppercase()
                            if (uiState.errorMessage != null) viewModel.clearMessages()
                        },
                        label = if (strings.isBangla) "ইনভাইট কোড (যেমনঃ MESS1234)" else "6-Digit Invite Code",
                        placeholder = "e.g. MESS1234",
                        leadingIcon = Icons.Default.VpnKey,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_invite_code"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = joinMemberName,
                        onValueChange = {
                            joinMemberName = it
                            if (uiState.errorMessage != null) viewModel.clearMessages()
                        },
                        label = if (strings.isBangla) "আপনার নাম" else "Your Name",
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_join_name"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = joinMemberPhone,
                        onValueChange = { joinMemberPhone = it },
                        label = if (strings.isBangla) "ফোন নম্বর" else "Phone Number",
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        testTag = "input_join_phone"
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.Negative.copy(alpha = 0.12f))
                                .border(1.dp, AppColors.Negative.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                color = AppColors.Negative,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = if (strings.isBangla) "মেসে যোগ দিন" else "Join Mess",
                        backgroundColor = AppColors.Indigo,
                        isLoading = uiState.isLoading,
                        onClick = {
                            viewModel.joinMess(inviteCode, joinMemberName, joinMemberPhone) {
                                onMessReady()
                            }
                        },
                        testTag = "join_mess_submit_button"
                    )
                }
            }
        }
    }
}
