package com.example.smartoffice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartoffice.data.SettingsDao
import com.example.smartoffice.data.UserSettings
import kotlinx.coroutines.launch

@Composable
fun DataManagementScreen(settingsDao: SettingsDao) {
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Data Management", style = MaterialTheme.typography.titleLarge)
        
        Button(onClick = { /* Export logic: toJson -> File */ }) {
            Text("Export Data (JSON)")
        }
        
        Button(onClick = { /* Import logic: File -> fromJson -> update Room */ }) {
            Text("Import Data (JSON)")
        }
        
        val settings by settingsDao.getSettings().collectAsState(initial = UserSettings())
        OutlinedTextField(
            value = settings?.inactivityTimeoutMinutes.toString(),
            onValueChange = { newVal ->
                scope.launch { settingsDao.updateSettings(settings!!.copy(inactivityTimeoutMinutes = newVal.toIntOrNull() ?: 10)) }
            },
            label = { Text("Inactivity Timeout (min)") }
        )
    }
}
