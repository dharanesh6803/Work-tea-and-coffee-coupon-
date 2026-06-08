package com.example.smartoffice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartoffice.data.UserSettings
import com.example.smartoffice.data.SettingsDao
import kotlinx.coroutines.launch

@Composable
fun ThemeSettingsScreen(settingsDao: SettingsDao) {
    val settings by settingsDao.getSettings().collectAsState(initial = UserSettings())
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Theme Settings", style = MaterialTheme.typography.titleLarge)
        
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Dark Mode")
            Switch(checked = settings?.isDarkMode ?: false, onCheckedChange = {
                scope.launch { settingsDao.updateSettings((settings ?: UserSettings()).copy(isDarkMode = it)) }
            })
        }
        
        // Simplified color selector
        Button(onClick = {
            scope.launch { settingsDao.updateSettings((settings ?: UserSettings()).copy(accentColor = 0xFFD32F2F.toInt())) }
        }) { Text("Accent: Red") }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Language", style = MaterialTheme.typography.titleLarge)
        
        val languages = listOf(
            "en" to "English",
            "hi" to "Hindi (हिन्दी)",
            "bn" to "Bengali (বাংলা)",
            "te" to "Telugu (తెలుగు)",
            "mr" to "Marathi (मराठी)",
            "ta" to "Tamil (தமிழ்)",
            "ur" to "Urdu (اردو)",
            "gu" to "Gujarati (ગુજરાતી)",
            "kn" to "Kannada (ಕನ್ನಡ)",
            "or" to "Odia (ଓଡ଼ிଆ)",
            "ml" to "Malayalam (മലയാളம்)",
            "pa" to "Punjabi (ਪੰਜਾਬી)",
            "as" to "Assamese (অসমীয়া)",
            "mai" to "Maithili (मैथिली)"
        )
        
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(languages.find { it.first == settings?.language }?.second ?: "Select Language")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                languages.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            expanded = false
                            scope.launch {
                                settingsDao.updateSettings((settings ?: UserSettings()).copy(language = code))
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Security", style = MaterialTheme.typography.titleLarge)
        var showPasswordDialog by remember { mutableStateOf(false) }
        Button(onClick = { showPasswordDialog = true }) {
            Text("Change Password")
        }
        
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Change Password") },
                text = { /* Needs access to employee data */ },
                confirmButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("Close") } }
                )
        }
    }
}
