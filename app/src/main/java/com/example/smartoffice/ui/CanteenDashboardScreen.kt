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
    val snackbarHostState = remember { SnackbarHostState() }
    val currentDate = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    
    val dailyConsumptions by if (currentUser != null) {
        consumptionDao.getConsumptionsForDate(currentDate).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<Consumption>()) }
    }
    
    val userDailyCount = remember(dailyConsumptions, currentUser) {
        dailyConsumptions.filter { it.employeeId == currentUser?.id }.size
    }
    
    val BEVERAGE_LIMIT = 3

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(16.dp).padding(paddingValues)) {
            Text("Canteen Dashboard", style = MaterialTheme.typography.titleLarge)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Beverage Log", style = MaterialTheme.typography.titleMedium)
            Text("Daily Count: $userDailyCount / $BEVERAGE_LIMIT", style = MaterialTheme.typography.bodySmall, color = if (userDailyCount >= BEVERAGE_LIMIT) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))
            
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
                        if (userDailyCount >= BEVERAGE_LIMIT) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Daily beverage limit ($BEVERAGE_LIMIT) reached!")
                            }
                        } else {
                            scope.launch {
                                consumptionDao.insert(
                                    Consumption(
                                        employeeId = currentUser.id,
                                        type = beverageType,
                                        date = currentDate,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                snackbarHostState.showSnackbar("$beverageType logged successfully")
                            }
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
}
