package com.example.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.util.AppLanguage
import com.example.ui.util.currentStrings
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToMessSetup: () -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val context = LocalContext.current
    val strings = currentStrings()
    val extended = LocalExtendedColors.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val uiState by viewModel.uiState.collectAsState()

    // Silent Auto-Sign-In on launch if already authenticated or authorized
    LaunchedEffect(Unit) {
        if (viewModel.currentUser.value != null) {
            val mess = viewModel.currentMess.value
            if (mess == null) onNavigateToMessSetup() else onNavigateToHome()
        } else {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                viewModel = viewModel,
                onAuthSuccess = {
                    val mess = viewModel.currentMess.value
                    if (mess == null) onNavigateToMessSetup() else onNavigateToHome()
                },
                onUnauthenticated = {
                    Log.d(TAG, "No cached credentials found; ready for interactive Google sign-in.")
                },
                scope = coroutineScope
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Language switcher toggle at top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilterChip(
                selected = currentLanguage == AppLanguage.ENGLISH,
                onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                label = { Text("English", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Primary,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("lang_en_button")
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = currentLanguage == AppLanguage.BANGLA,
                onClick = { onLanguageChange(AppLanguage.BANGLA) },
                label = { Text("বাংলা", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Primary,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("lang_bn_button")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Logo Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = strings.appName,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = strings.appName,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.PrimaryDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = strings.appSubtitle,
            fontSize = 14.sp,
            color = extended.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Main Auth Card
        AppCard(testTag = "auth_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.BANGLA) "স্বাগতম" else "Welcome to Dine Core",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = extended.textPrimary
                )

                Text(
                    text = if (currentLanguage == AppLanguage.BANGLA)
                        "১-ট্যাপে আপনার গুগল একাউন্ট দিয়ে লগইন অথবা নতুন একাউন্ট তৈরি করুন"
                    else
                        "1-tap sign-in or instant account creation with your Google Account",
                    fontSize = 13.sp,
                    color = extended.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp, start = 8.dp, end = 8.dp)
                )

                if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.Negative.copy(alpha = 0.12f))
                            .border(1.dp, AppColors.Negative.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            color = AppColors.Negative,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Primary 1-Tap Google Sign-In Button
                Button(
                    onClick = {
                        viewModel.clearMessages()
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            viewModel = viewModel,
                            onAuthSuccess = {
                                val mess = viewModel.currentMess.value
                                if (mess == null) onNavigateToMessSetup() else onNavigateToHome()
                            },
                            coroutineScope = coroutineScope
                        )
                    },
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.5.dp, extended.border, RoundedCornerShape(28.dp))
                        .testTag("google_sign_in_button")
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AppColors.Primary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.BANGLA)
                                    "Google দিয়ে চালিয়ে যান"
                                else
                                    "Continue with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (currentLanguage == AppLanguage.BANGLA)
                        "কোন পাসওয়ার্ডের ঝামেলা নেই • নিরাপদ ও তাত্ক্ষণিক"
                    else
                        "No password required • Secure & Instant",
                    fontSize = 12.sp,
                    color = extended.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Highlights
        AppCard(testTag = "features_card") {
            Column(modifier = Modifier.fillMaxWidth()) {
                FeatureRow(
                    icon = Icons.Default.CheckCircle,
                    iconTint = AppColors.Positive,
                    title = if (currentLanguage == AppLanguage.BANGLA) "সহজ ১-ক্লিক একাউন্ট" else "Easy 1-Tap Access",
                    desc = if (currentLanguage == AppLanguage.BANGLA)
                        "যে কেউ মুহূর্তেই গুগল দিয়ে যুক্ত হতে পারবে"
                    else
                        "Anyone can join and access mess data with one click"
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.CloudSync,
                    iconTint = AppColors.Charts,
                    title = if (currentLanguage == AppLanguage.BANGLA) "রিয়েলটাইম ক্লাউড সিঙ্ক" else "Real-Time Cloud Sync",
                    desc = if (currentLanguage == AppLanguage.BANGLA)
                        "মিল, বাজার ও জমা স্বয়ংক্রিয়ভাবে সকল মেম্বারদের কাছে আপডেট হবে"
                    else
                        "Live meal sheet, bazar, and deposit updates across all members"
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.Calculate,
                    iconTint = AppColors.Primary,
                    title = if (currentLanguage == AppLanguage.BANGLA) "পয়সা নির্ভুল মিল রেট" else "Paisa-Accurate Calculation",
                    desc = if (currentLanguage == AppLanguage.BANGLA)
                        "স্বচ্ছ হিসাব, অডিট হিস্টোরি ও মান্থলি সামারি রিপোর্ট"
                    else
                        "Transparent balance calculations, audit logs & monthly bills"
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    desc: String
) {
    val extended = LocalExtendedColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = extended.textPrimary
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = extended.textMuted
            )
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Draw Google 'G' icon geometry with official colors
        // Blue, Red, Yellow, Green segments
        val strokeWidth = w * 0.18f
        val radius = (w - strokeWidth) / 2f
        val center = Offset(w / 2f, h / 2f)

        // Blue right arc + horizontal bar
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2, radius * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2, radius * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Yellow bottom-left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2, radius * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2, radius * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Horizontal bar for G
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(w / 2f, h / 2f),
            end = Offset(w - strokeWidth / 2f, h / 2f),
            strokeWidth = strokeWidth
        )
    }
}

/**
 * Interactive Sign-In with Google via Jetpack CredentialManager
 */
fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit,
    coroutineScope: CoroutineScope
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        viewModel.setErrorMessage("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    viewModel.setLoading(true)
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    coroutineScope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                viewModel.signInWithGoogleIdToken(googleIdToken, onAuthSuccess)
            } else {
                viewModel.setErrorMessage("Unexpected credential type received")
                viewModel.setLoading(false)
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In cancelled or dismissed: ${e.message}", e)
            viewModel.setLoading(false)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            viewModel.setErrorMessage(e.localizedMessage ?: "Google Sign-In failed. Please try again.")
            viewModel.setLoading(false)
        }
    }
}

/**
 * Silent auto-sign-in on startup using authorized Google accounts
 */
fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                viewModel.signInWithGoogleIdToken(googleIdToken, onAuthSuccess)
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}
