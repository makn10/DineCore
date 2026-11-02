package com.example.ui.screens.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.AppTextField
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.util.AppLanguage
import com.example.ui.util.currentStrings

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    currentThemeMode: String,
    onThemeModeChange: (String) -> Unit,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val strings = currentStrings()
    val extended = LocalExtendedColors.current

    val userProfile by viewModel.userProfile.collectAsState()
    val mess by viewModel.currentMess.collectAsState()
    val currentUserId = viewModel.currentUser.value?.uid.orEmpty()
    val isManager = mess?.managerIds?.contains(currentUserId) == true || mess?.creatorId == currentUserId

    var showStartMonthDialog by remember { mutableStateOf(false) }
    var showDeleteMessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "Profile & Settings",
            backgroundColor = AppColors.Primary
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 88.dp)
        ) {
            // User Profile Card
            AppCard {
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
                            text = (userProfile?.displayName ?: "U").take(1).uppercase(),
                            color = AppColors.PrimaryDark,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile?.displayName ?: "User",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.textPrimary
                        )
                        Text(
                            text = userProfile?.email ?: "",
                            fontSize = 13.sp,
                            color = extended.textMuted
                        )
                        if (!userProfile?.phone.isNullOrEmpty()) {
                            Text(
                                text = userProfile?.phone.orEmpty(),
                                fontSize = 13.sp,
                                color = extended.textMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mess Details & Invite Code Card
            if (mess != null) {
                AppCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = mess?.name ?: "Mess",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.textPrimary
                        )
                        if (!mess?.address.isNullOrEmpty()) {
                            Text(text = mess?.address.orEmpty(), fontSize = 13.sp, color = extended.textMuted)
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = extended.border.copy(alpha = 0.3f))

                        Text(
                            text = "6-Digit Invite Code",
                            fontSize = 13.sp,
                            color = extended.textMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.TealTint)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mess?.inviteCode.orEmpty(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.PrimaryDark,
                                letterSpacing = 2.sp
                            )

                            Row {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Invite Code", mess?.inviteCode)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Invite code copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("copy_invite_code_button")
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppColors.PrimaryDark)
                                }
                                IconButton(
                                    onClick = {
                                        val shareText = "Join our mess on Dine Core! Invite Code: ${mess?.inviteCode}"
                                        val intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Invite Code"))
                                    },
                                    modifier = Modifier.testTag("share_invite_code_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = AppColors.PrimaryDark)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App Preferences
            AppCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Preferences", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = extended.textPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Language
                    Text(text = "Language:", fontSize = 13.sp, color = extended.textMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentLanguage == AppLanguage.ENGLISH,
                            onClick = {
                                onLanguageChange(AppLanguage.ENGLISH)
                                viewModel.updateThemeAndLanguage(currentThemeMode, "en")
                            },
                            label = { Text("English", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = currentLanguage == AppLanguage.BANGLA,
                            onClick = {
                                onLanguageChange(AppLanguage.BANGLA)
                                viewModel.updateThemeAndLanguage(currentThemeMode, "bn")
                            },
                            label = { Text("বাংলা", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme
                    Text(text = "Theme Mode:", fontSize = 13.sp, color = extended.textMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (mode, label) ->
                            FilterChip(
                                selected = currentThemeMode == mode,
                                onClick = {
                                    onThemeModeChange(mode)
                                    viewModel.updateThemeAndLanguage(mode, currentLanguage.code)
                                },
                                label = { Text(label, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppColors.Primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Manager Controls Section
            if (isManager) {
                Spacer(modifier = Modifier.height(8.dp))
                AppCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Manager Tools",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Roles
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        PrimaryButton(
                            text = strings.startNewMonth,
                            backgroundColor = AppColors.Danger,
                            onClick = { showStartMonthDialog = true },
                            testTag = "start_new_month_button"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SecondaryButton(
                            text = "Delete Mess",
                            color = AppColors.Danger,
                            onClick = { showDeleteMessDialog = true },
                            testTag = "delete_mess_button"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryButton(
                text = "Sign Out",
                color = extended.textMuted,
                onClick = { viewModel.signOut(onSignedOut) },
                testTag = "sign_out_button"
            )
        }

        // Start New Month Dialog
        if (showStartMonthDialog) {
            var carryOver by remember { mutableStateOf(true) }

            AlertDialog(
                onDismissRequest = { showStartMonthDialog = false },
                title = { Text(strings.startNewMonth, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(strings.closeMonthConfirm)
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.carryOverBalances,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = carryOver,
                                onCheckedChange = { carryOver = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Primary)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.closeMonthAndStartNew(carryOver) {
                            showStartMonthDialog = false
                            Toast.makeText(context, "New month started!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(strings.confirm, fontWeight = FontWeight.Bold, color = AppColors.Danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartMonthDialog = false }) { Text(strings.cancel) }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Delete Mess Dialog
        if (showDeleteMessDialog) {
            var confirmationText by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showDeleteMessDialog = false },
                title = { Text("Delete Mess?", fontWeight = FontWeight.Bold, color = AppColors.Danger) },
                text = {
                    Column {
                        Text("This will permanently remove the mess and all associated records. Type DELETE to confirm:")
                        Spacer(modifier = Modifier.height(10.dp))
                        AppTextField(
                            value = confirmationText,
                            onValueChange = { confirmationText = it },
                            label = "Type DELETE"
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = confirmationText.trim() == "DELETE",
                        onClick = {
                            viewModel.deleteMess {
                                showDeleteMessDialog = false
                                onSignedOut()
                            }
                        }
                    ) {
                        Text("Delete Mess", fontWeight = FontWeight.Bold, color = AppColors.Danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteMessDialog = false }) { Text("Cancel") }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
