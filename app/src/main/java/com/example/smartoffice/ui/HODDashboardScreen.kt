package com.example.smartoffice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartoffice.data.EmployeeDao

@Composable
fun HODDashboardScreen(employeeDao: EmployeeDao) {
    val employees by employeeDao.getAllEmployees().collectAsState(initial = emptyList())
    val myTeam = employees.filter { it.department == "Development" && it.role != "HOD" }
    val totalExpenditure = myTeam.sumOf { it.dailyConsumption * 15 }
    val quota = 2000
    val nearingLimit = myTeam.filter { it.dailyConsumption > 100 }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "My Team", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Performance Summary", style = MaterialTheme.typography.titleMedium)
                Text(text = "Total Monthly Tea/Coffee Budget: $$totalExpenditure / Quota: $$quota")
                if (nearingLimit.isNotEmpty()) {
                    Text(text = "Nearing Limit: ${nearingLimit.joinToString { it.name }}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        LazyColumn {
            items(myTeam) { employee ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = employee.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Attendance: ${employee.attendanceStatus}")
                        Text(text = "Leave Balance: ${employee.leaveBalance}")
                    }
                }
            }
        }
    }
}
