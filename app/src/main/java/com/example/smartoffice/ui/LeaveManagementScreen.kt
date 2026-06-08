package com.example.smartoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartoffice.data.Employee
import com.example.smartoffice.data.LeaveRequest
import com.example.smartoffice.data.LeaveRequestDao
import kotlinx.coroutines.launch

@Composable
fun LeaveManagementScreen(leaveRequestDao: LeaveRequestDao, currentUser: Employee?) {
    val scope = rememberCoroutineScope()
    val leaveRequests by if (currentUser != null) {
        leaveRequestDao.getLeaveRequestsForEmployee(currentUser.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<LeaveRequest>()) }
    }

    var showApplyDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Leave Management", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { showApplyDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Apply")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("My Leave Requests", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (leaveRequests.isEmpty()) {
            Text("No leave requests found.", color = Color.Gray)
        } else {
            // Using Box with height for Column container since this is nested in a scrollable Column
            Column {
                leaveRequests.forEach { request ->
                    LeaveRequestItem(request)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showApplyDialog && currentUser != null) {
        var leaveType by remember { mutableStateOf("Casual") }
        var startDate by remember { mutableStateOf("") }
        var endDate by remember { mutableStateOf("") }
        var reason by remember { mutableStateOf("") }
        val leaveTypes = listOf("Casual", "Sick", "Annual", "Maternity/Paternity")

        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Apply for Leave") },
            text = {
                Column {
                    Text("Type:")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        leaveTypes.take(2).forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = leaveType == type, onClick = { leaveType = type })
                                Text(type)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        leaveTypes.drop(2).forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = leaveType == type, onClick = { leaveType = type })
                                Text(type)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date (YYYY-MM-DD)") })
                    OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date (YYYY-MM-DD)") })
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.height(100.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (startDate.isNotBlank() && endDate.isNotBlank()) {
                        scope.launch {
                            leaveRequestDao.insert(
                                LeaveRequest(
                                    employeeId = currentUser.id,
                                    type = leaveType,
                                    startDate = startDate,
                                    endDate = endDate,
                                    reason = reason
                                )
                            )
                            showApplyDialog = false
                        }
                    }
                }) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun LeaveRequestItem(request: LeaveRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(request.type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                StatusBadge(request.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${request.startDate} to ${request.endDate}", style = MaterialTheme.typography.bodyMedium)
            if (request.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Reason: ${request.reason}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Approved" -> Color(0xFF4CAF50)
        "Rejected" -> Color(0xFFF44336)
        else -> Color(0xFFFFC107) // Pending
    }
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
