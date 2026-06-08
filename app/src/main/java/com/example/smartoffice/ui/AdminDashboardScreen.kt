package com.example.smartoffice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.smartoffice.data.SettingsDao
import com.example.smartoffice.data.Employee
import com.example.smartoffice.data.EmployeeDao
import com.example.smartoffice.data.Coupon
import com.example.smartoffice.data.CouponDao
import com.example.smartoffice.data.LeaveRequestDao
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(employeeDao: EmployeeDao, settingsDao: SettingsDao, couponDao: CouponDao, leaveRequestDao: LeaveRequestDao) {
    val employees by employeeDao.getAllEmployees().collectAsState(initial = emptyList())
    val leaveRequests by leaveRequestDao.getAllLeaveRequests().collectAsState(initial = emptyList())
    val settings by settingsDao.getSettings().collectAsState(initial = null)
    val currentLang = settings?.language ?: "en"
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredEmployees = employees.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val scope = rememberCoroutineScope()
    
    var currentSubTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.padding(16.dp)) {
        TabRow(selectedTabIndex = currentSubTab) {
            Tab(selected = currentSubTab == 0, onClick = { currentSubTab = 0 }, text = { Text("Employees") })
            Tab(selected = currentSubTab == 1, onClick = { currentSubTab = 1 }, text = { Text("Leaves") })
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (currentSubTab == 0) {
            // Employee Management (Existing Logic)
            AdminEmployeeManagement(filteredEmployees, employeeDao, couponDao, scope, currentLang)
        } else {
            // Leave Management
            AdminLeaveManagement(leaveRequests, employees, leaveRequestDao, scope)
        }
    }
}

@Composable
fun AdminEmployeeManagement(filteredEmployees: List<Employee>, employeeDao: EmployeeDao, couponDao: CouponDao, scope: kotlinx.coroutines.CoroutineScope, currentLang: String) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Development") }
    var searchQuery by remember { mutableStateOf("") }

    Column {
        // Add Employee Form
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add New Employee", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Initial Password") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department (Default)") }, modifier = Modifier.fillMaxWidth())
                
                var deptHi by remember { mutableStateOf("") }
                var deptTa by remember { mutableStateOf("") }
                OutlinedTextField(value = deptHi, onValueChange = { deptHi = it }, label = { Text("Department (Hindi)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = deptTa, onValueChange = { deptTa = it }, label = { Text("Department (Tamil)") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(value = "Admin", onValueChange = { }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                Button(onClick = { 
                    scope.launch { 
                        val localized = mutableMapOf<String, String>()
                        if (deptHi.isNotBlank()) localized["hi"] = deptHi
                        if (deptTa.isNotBlank()) localized["ta"] = deptTa
                        
                        val newEmployee = Employee(
                            name = name, 
                            leaveStatus = "Active", 
                            dailyConsumption = 0, 
                            password = password, 
                            department = department, 
                            departmentLocalized = localized,
                            role = "Admin"
                        )
                        employeeDao.insert(newEmployee)
                        couponDao.insertCoupon(Coupon(employeeId = 0, date = "2026-06-07", type = "Morning"))
                    } 
                }) {
                    Text("Create Employee")
                }
            }
        }

        // Table Body
        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
            items(filteredEmployees) { employee ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = employee.name, modifier = Modifier.weight(1f))
                    val deptDisplay = employee.departmentLocalized?.get(currentLang) ?: employee.department
                    Text(text = deptDisplay, modifier = Modifier.weight(1f))
                    Text(text = employee.role, modifier = Modifier.weight(1f))
                    Button(onClick = { scope.launch { employeeDao.update(employee.copy(role = "HOD")) } }) {
                        Text("Promote")
                    }
                }
                Divider()
            }
        }
    }
}

@Composable
fun AdminLeaveManagement(leaveRequests: List<com.example.smartoffice.data.LeaveRequest>, employees: List<Employee>, leaveRequestDao: com.example.smartoffice.data.LeaveRequestDao, scope: kotlinx.coroutines.CoroutineScope) {
    LazyColumn {
        items(leaveRequests) { request ->
            val emp = employees.find { it.id == request.employeeId }
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Employee: ${emp?.name ?: "Unknown"}", fontWeight = FontWeight.Bold)
                    Text("Type: ${request.type} (${request.startDate} to ${request.endDate})")
                    Text("Reason: ${request.reason}")
                    Text("Status: ${request.status}", color = if (request.status == "Approved") Color(0xFF4CAF50) else if (request.status == "Rejected") Color.Red else Color.Gray)
                    
                    if (request.status == "Pending") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { scope.launch { leaveRequestDao.updateStatus(request.id, "Rejected") } }) {
                                Text("Reject", color = Color.Red)
                            }
                            Button(onClick = { scope.launch { leaveRequestDao.updateStatus(request.id, "Approved") } }) {
                                Text("Approve")
                            }
                        }
                    }
                }
            }
        }
    }
}
