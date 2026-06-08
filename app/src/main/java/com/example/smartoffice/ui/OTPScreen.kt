package com.example.smartoffice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OTPScreen(onOtpVerified: () -> Unit) {
    var otp by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Enter 4-digit OTP", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 4) otp = it },
            label = { Text("OTP") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { if (otp == "1234") onOtpVerified() }) {
            Text("Verify")
        }
    }
}
