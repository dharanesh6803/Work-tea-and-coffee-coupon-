package com.example.smartoffice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartoffice.data.ScanDao

@Composable
fun ScanHistoryScreen(scanDao: ScanDao) {
    val scans by scanDao.getRecentScans().collectAsState(initial = emptyList())
    
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(scans) { scan ->
            Text(text = "${scan.employeeName} scanned at ${scan.timestamp}")
            Divider()
        }
    }
}
