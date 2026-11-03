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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.ui.components.AppTextField
import com.example.ui.components.PrimaryButton
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

    var authTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Create Account

    // Sign In form fields
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Sign Up form fields
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupPhone by remember { mutableStateOf("") }

    // Silent Auto-Sign-In on launch if already authenticated
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
                    Log.d(TAG, "No cached credentials found; ready for sign-in.")
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
            .padding(20.dp),
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

        Spacer(modifier = Modifier.height(12.dp))

        // App Logo Icon
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(AppColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = strings.appName,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = strings.appName,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.PrimaryDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = strings.appSubtitle,
            fontSize = 13.sp,
            color = extended.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Main Auth Card
        AppCard(testTag = "auth_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tab Row for Sign In vs Create Account
                TabRow(
                    selectedTabIndex = authTab,
                    containerColor = extended.screenBackground.copy(alpha = 0.5f),
                    contentColor = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = authTab == 0,
                        onClick = { authTab = 0; viewModel.clearMessages() },
                        text = {
                            Text(
                                text = if (currentLanguage == AppLanguage.BANGLA) "লগইন" else "Sign In",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("tab_login")
                    )
                    Tab(
                        selected = authTab == 1,
                        onClick = { authTab = 1; viewModel.clearMessages() },
                        text = {
                            Text(
                                text = if (currentLanguage == AppLanguage.BANGLA) "নতুন একাউন্ট" else "Create Account",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("tab_signup")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Error message banner
                if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.Negative.copy(alpha = 0.12f))
                            .border(1.dp, AppColors.Negative.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            color = AppColors.Negative,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (authTab == 0) {
                    // SIGN IN FORM
                    AppTextField(
                        value = loginEmail,
                        onValueChange = {
                            loginEmail = it
                            if (uiState.errorMessage != null) viewModel.clearMessages()
                        },
                        label = if (currentLanguage == AppLanguage.BANGLA) "ইমেইল অ্যাড্রেস" else "Email Address",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_login_email"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            if (uiState.errorMessage != null) viewModel.clearMessages()
                        },
                        label = if (currentLanguage == AppLanguage.BANGLA) "পাসওয়ার্ড" else "Password",
                        isPassword = true,
                        leadingIcon = Icons.Default.Lock,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        testTag = "input_login_password"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    PrimaryButton(
                        text = if (currentLanguage == AppLanguage.BANGLA) "লগইন করুন" else "Sign In",
                        isLoading = uiState.isLoading,
                        onClick = {
                            viewModel.signInWithEmail(loginEmail, loginPassword) {
                                val mess = viewModel.currentMess.value
                                if (mess == null) onNavigateToMessSetup() else onNavigateToHome()
                            }
                        },
                        testTag = "email_login_button"
                    )
                } else {
                    // CREATE ACCOUNT FORM
                    AppTextField(
                        value = signupName,
                        onValueChange = {
                            signupName = it
                            if (uiState.errorMessage != null) viewModel.clearMessages()
                        },
                        label = if (currentLanguage == AppLanguage.BANGLA) "আপনার পুরো নাম" else "Full Name",
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_signup_name"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = signupEmail,
                        onValueChange = {
                            signupEmail = it
                            if (uiState.errorMessage != null) viewModel.clearMessages()
                        },
                        label = if (currentLanguage == AppLanguage.BANGLA) "ইমেইল অ্যাড্রেস" else "Email Address",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_signup_email"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = signupPassword,
                        onValueChange = {
                            signupPassword = it
                            if (uiState.errorMessage != null) viewModel.clearMessages()
                        },
                        label = if (currentLanguage == AppLanguage.BANGLA) "পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)" else "Password (min 6 chars)",
                        isPassword = true,
                        leadingIcon = Icons.Default.Lock,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_signup_password"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = signupPhone,
                        onValueChange = { signupPhone = it },
                        label = if (currentLanguage == AppLanguage.BANGLA) "ফোন নম্বর" else "Phone Number",
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        testTag = "input_signup_phone"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    PrimaryButton(
                        text = if (currentLanguage == AppLanguage.BANGLA) "একাউন্ট তৈরি করুন" else "Create Account & Continue",
                        backgroundColor = AppColors.Indigo,
                        isLoading = uiState.isLoading,
                        onClick = {
                            viewModel.signUpWithEmail(signupName, signupEmail, signupPassword, signupPhone) {
                                onNavigateToMessSetup()
                            }
                        },
                        testTag = "email_signup_button"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = extended.border)
                    Text(
                        text = if (currentLanguage == AppLanguage.BANGLA) " অথবা " else " OR ",
                        fontSize = 12.sp,
                        color = extended.textMuted,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = extended.border)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1-Tap Google Sign-In Button
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
                        .height(52.dp)
                        .border(1.5.dp, extended.border, RoundedCornerShape(28.dp))
                        .testTag("google_sign_in_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GoogleLogoIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.BANGLA)
                                "Google দিয়ে চালিয়ে যান"
                            else
                                "Continue with Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Guest / Quick Demo access for offline or instant testing
                TextButton(
                    onClick = {
                        viewModel.signInAsGuest(loginEmail.substringBefore("@").ifEmpty { "Guest User" }) {
                            onNavigateToMessSetup()
                        }
                    },
                    modifier = Modifier.testTag("guest_login_button")
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BANGLA)
                            "লগইন ছাড়া অ্যাপ ঘুরে দেখুন (গেস্ট মোড)"
                        else
                            "Explore as Guest (Instant Access)",
                        fontSize = 13.sp,
                        color = AppColors.TealTint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Feature Highlights
        AppCard(testTag = "features_card") {
            Column(modifier = Modifier.fillMaxWidth()) {
                FeatureRow(
                    icon = Icons.Default.CheckCircle,
                    iconTint = AppColors.Positive,
                    title = if (currentLanguage == AppLanguage.BANGLA) "সহজ ১-ক্লিক একাউন্ট" else "Easy 1-Tap Access",
                    desc = if (currentLanguage == AppLanguage.BANGLA)
                        "ইমেইল বা গুগল দিয়ে মুহূর্তেই একাউন্ট তৈরি করুন"
                    else
                        "Sign in or register in seconds via Email or Google"
                )
                Spacer(modifier = Modifier.height(10.dp))
                FeatureRow(
                    icon = Icons.Default.CloudSync,
                    iconTint = AppColors.Charts,
                    title = if (currentLanguage == AppLanguage.BANGLA) "রিয়েলটাইম ক্লাউড সিঙ্ক" else "Real-Time Cloud Sync",
                    desc = if (currentLanguage == AppLanguage.BANGLA)
                        "মিল, বাজার ও জমা স্বয়ংক্রিয়ভাবে সকল মেম্বারদের কাছে আপডেট হবে"
                    else
                        "Live meal sheet, bazar, and deposit updates across all members"
                )
                Spacer(modifier = Modifier.height(10.dp))
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
        val strokeWidth = w * 0.18f
        val radius = (w - strokeWidth) / 2f

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
