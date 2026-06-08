package com.example.smartoffice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartoffice.data.Employee
import com.example.smartoffice.data.EmployeeDao
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(employee: Employee, employeeDao: EmployeeDao) {
    var newPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Change Password", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("New Password") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { scope.launch { employeeDao.update(employee.copy(password = newPassword)) } }) {
            Text("Update Password")
        }
    }
}
