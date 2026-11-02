package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.SmartMessTheme
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.util.LocalAppStrings
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val userProfile by mainViewModel.userProfile.collectAsState()

            // Initialize default language from device locale if bn
            val initialLang = remember {
                val defaultLocale = Locale.getDefault().language
                if (defaultLocale.equals("bn", ignoreCase = true)) AppLanguage.BANGLA else AppLanguage.ENGLISH
            }

            var currentLanguage by remember { mutableStateOf(initialLang) }
            var currentThemeMode by remember { mutableStateOf("system") }

            // Sync with profile preferences if available
            userProfile?.let { profile ->
                if (profile.language.isNotBlank()) {
                    val lang = AppLanguage.fromCode(profile.language)
                    if (lang != currentLanguage) currentLanguage = lang
                }
                if (profile.themeMode.isNotBlank() && profile.themeMode != currentThemeMode) {
                    currentThemeMode = profile.themeMode
                }
            }

            val appStrings = remember(currentLanguage) { AppStrings(currentLanguage) }

            CompositionLocalProvider(LocalAppStrings provides appStrings) {
                SmartMessTheme(themeMode = currentThemeMode) {
                    AppNavigation(
                        viewModel = mainViewModel,
                        currentLanguage = currentLanguage,
                        onLanguageChange = { currentLanguage = it },
                        currentThemeMode = currentThemeMode,
                        onThemeModeChange = { currentThemeMode = it }
                    )
                }
            }
        }
    }
}
