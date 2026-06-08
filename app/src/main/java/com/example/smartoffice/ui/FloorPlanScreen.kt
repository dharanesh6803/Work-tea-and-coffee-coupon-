package com.example.smartoffice.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun FloorPlanScreen() {
    var pins by remember { mutableStateOf(listOf<androidx.compose.ui.geometry.Offset>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Interactive Floor Plan - Pin Duty Locations", style = MaterialTheme.typography.titleMedium)
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset -> pins = pins + offset }
            }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw floor plan
                drawRect(color = Color.LightGray)
                pins.forEach { pin ->
                    drawCircle(color = Color.Red, radius = 20f, center = pin)
                }
            }
        }
    }
}
