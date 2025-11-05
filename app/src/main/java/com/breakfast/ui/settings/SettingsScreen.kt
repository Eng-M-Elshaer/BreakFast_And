package com.breakfast.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.breakfast.BreakfastApplication
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.SettingsViewModel

/**
 * Settings screen that displays app version, "About Us" content and allows changing
 * the application's language. It uses [SettingsViewModel] to fetch data and
 * persist the language preference via [com.breakfast.managers.PreferenceManager].
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController? = null) {
    val context = BreakfastApplication.get()
    val preferenceManager = context.preferenceManager
    val apiService = ApiClient.apiService
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(preferenceManager, apiService))

    val versionState by viewModel.versionState.collectAsState()
    val aboutUsState by viewModel.aboutUsState.collectAsState()
    val language by viewModel.languageState.collectAsState()

    // Load initial data when screen appears
    LaunchedEffect(Unit) {
        viewModel.fetchVersion()
        viewModel.fetchAboutUs()
        viewModel.loadLanguage()
    }

    Scaffold(topBar = { TopAppBar(title = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.settings_title)) }) }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxWidth()) {
            // Version section
            Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.app_version), modifier = Modifier.padding(vertical = 8.dp))
            when (val v = versionState) {
                is Result.Loading -> {
                    CircularProgressIndicator()
                }
                is Result.Error -> {
                    Text(text = v.message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.failed_load_version))
                }
                is Result.Success<*> -> {
                    val versions = v.data as? List<com.breakfast.models.VersionModel> ?: emptyList()
                    versions.forEach { ver ->
                        Text(text = "${ver.key ?: ""}: ${ver.value ?: ""}")
                    }
                }
                else -> {}
            }

            // About Us section
            Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.about_us), modifier = Modifier.padding(vertical = 8.dp))
            when (val about = aboutUsState) {
                is Result.Loading -> {
                    CircularProgressIndicator()
                }
                is Result.Error -> {
                    Text(text = about.message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.failed_load_content))
                }
                is Result.Success<*> -> {
                    val map = about.data as? Map<*, *> ?: emptyMap<String, String>()
                    // Show content based on current language, fallback to first value
                    val content: String = when (language) {
                        "ar" -> map["ar"] as? String ?: map.values.firstOrNull() as? String ?: ""
                        else -> map["en"] as? String ?: map.values.firstOrNull() as? String ?: ""
                    }
                    Text(text = content)
                }
                else -> {}
            }

            // Language selection section
            Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.language), modifier = Modifier.padding(vertical = 8.dp))
            // English option
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = language == "en",
                    onClick = { viewModel.setLanguage("en") }
                )
                Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.english), modifier = Modifier.padding(start = 8.dp))
            }
            // Arabic option
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = language == "ar",
                    onClick = { viewModel.setLanguage("ar") }
                )
                Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.arabic), modifier = Modifier.padding(start = 8.dp))
            }

            // Button to go back
            Button(onClick = { navController?.popBackStack() }, modifier = Modifier.padding(top = 16.dp)) {
                Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.back))
            }
        }
    }
}