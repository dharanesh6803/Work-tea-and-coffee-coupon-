package com.example.smartoffice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartoffice.data.*
import kotlinx.coroutines.launch

@Composable
fun CanteenDashboardScreen(scanDao: ScanDao, employeeDao: EmployeeDao, consumptionDao: ConsumptionDao, currentUser: Employee?) {
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Canteen Dashboard", style = MaterialTheme.typography.titleLarge)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Beverage Log", style = MaterialTheme.typography.titleMedium)
        
        var beverageType by remember { mutableStateOf("Tea") }
        val beverages = listOf("Tea", "Coffee")
        
        Row {
            beverages.forEach { type ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = beverageType == type, onClick = { beverageType = type })
                    Text(type)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
        Button(
            onClick = {
                if (currentUser != null) {
                    scope.launch {
                        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                        consumptionDao.insert(
                            Consumption(
                                employeeId = currentUser.id,
                                type = beverageType,
                                date = currentDate,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log $beverageType")
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        if (currentUser?.role == "Canteen Staff" || currentUser?.role == "Admin") {
            Text("QR Scanner Management", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            Text("Scanner active for canteen checkouts.")
        } else {
            Text("Canteen staff use this section for scanning meal coupons.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
