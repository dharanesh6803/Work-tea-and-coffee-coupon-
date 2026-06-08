package com.example.smartoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartoffice.data.Employee
import com.example.smartoffice.data.EmployeeDao
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(employee: Employee, employeeDao: EmployeeDao) {
    var name by remember { mutableStateOf(employee.name) }
    var password by remember { mutableStateOf(employee.password) }
    var mobile by remember { mutableStateOf(employee.mobile) }
    var email by remember { mutableStateOf(employee.email) }
    var photoUri by remember { mutableStateOf(employee.profilePhotoUri ?: "") }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showIdCard by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(16.dp).padding(paddingValues).verticalScroll(rememberScrollState())) {
            Text("Your Profile", style = MaterialTheme.typography.titleLarge)
            
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = photoUri, onValueChange = { photoUri = it }, label = { Text("Profile Photo URL (Sample Image)") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = { 
                scope.launch { 
                    employeeDao.update(employee.copy(name = name, password = password, mobile = mobile, email = email, profilePhotoUri = photoUri.ifBlank { null }))
                    snackbarHostState.showSnackbar("Profile updated successfully")
                } 
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Save Changes")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showIdCard = !showIdCard }, 
                modifier = Modifier.fillMaxWidth(),
                colors = if (showIdCard) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.buttonColors()
            ) {
                Text(if (showIdCard) "Hide ID Card" else "Preview ID Card")
            }
            
            if (showIdCard) {
                Spacer(modifier = Modifier.height(24.dp))
                BangaloreClubIdCard(name = name, id = employee.id.toString(), role = employee.role, photoUrl = photoUri)
            }
        }
    }
}

@Composable
fun BangaloreClubIdCard(name: String, id: String, role: String, photoUrl: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background decoration
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Brush.horizontalGradient(listOf(Color(0xFF004D40), Color(0xFF00796B)))))
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BANGALORE CLUB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Official Identity Card", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = photoUrl.ifBlank { "https://via.placeholder.com/150" },
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
                        Text(role, color = Color.DarkGray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("EMPLOYEE ID: $id", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            
            // Footer
            Box(modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)) {
                Text("AUTHORIZED SIGN", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
