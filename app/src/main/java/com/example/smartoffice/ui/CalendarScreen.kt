package com.example.smartoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartoffice.data.Duty
import com.example.smartoffice.data.DutyDao
import com.example.smartoffice.data.Employee
import com.example.smartoffice.data.EmployeeDao
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun CalendarScreen(dutyDao: DutyDao, employeeDao: EmployeeDao) {
    val duties by dutyDao.getAllDuties().collectAsState(initial = emptyList())
    val employees by employeeDao.getAllEmployees().collectAsState(initial = emptyList())
    val today = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    var selectedDate by remember { mutableStateOf(today) }
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Duty Roster", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "June 2026", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(260.dp)
        ) {
            items(30) { index ->
                val day = index + 1
                val dateStr = "2026-06-%02d".format(day)
                val isSelected = selectedDate == dateStr
                val hasDuty = duties.any { it.date == dateStr }
                
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clickable { selectedDate = dateStr },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                        else if (hasDuty) MaterialTheme.colorScheme.secondaryContainer 
                                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$day", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (hasDuty) {
                                Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape))
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Shifts for $selectedDate", style = MaterialTheme.typography.titleMedium)
        
        val shiftsForDate = duties.filter { it.date == selectedDate }
        
        if (shiftsForDate.isEmpty()) {
            Text("No shifts assigned for this date.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(shiftsForDate) { duty ->
                    val employee = employees.find { it.id == duty.employeeId }
                    ListItem(
                        headlineContent = { Text(duty.title) },
                        supportingContent = { Text("Assigned to: ${employee?.name ?: "Unknown"}") },
                        trailingContent = {
                            IconButton(onClick = { scope.launch { dutyDao.delete(duty) } }) {
                                Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Simple Add Shift FAB or Button
        var showAddDialog by remember { mutableStateOf(false) }
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Assign New Shift")
        }
        
        if (showAddDialog) {
            var shiftTitle by remember { mutableStateOf("") }
            var selectedEmpId by remember { mutableStateOf<Int?>(null) }
            
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Assign Shift") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = shiftTitle,
                            onValueChange = { shiftTitle = it },
                            label = { Text("Shift Title (e.g. Night Shift)") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select Employee:")
                        LazyColumn(modifier = Modifier.height(150.dp)) {
                            items(employees) { emp ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { selectedEmpId = emp.id }.padding(8.dp)
                                ) {
                                    RadioButton(selected = selectedEmpId == emp.id, onClick = { selectedEmpId = emp.id })
                                    Text(emp.name)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (shiftTitle.isNotBlank() && selectedEmpId != null) {
                            scope.launch {
                                dutyDao.insert(Duty(title = shiftTitle, employeeId = selectedEmpId!!, date = selectedDate, timestamp = System.currentTimeMillis()))
                                showAddDialog = false
                            }
                        }
                    }) { Text("Assign") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
