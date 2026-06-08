package com.example.smartoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartoffice.data.ConsumptionDao
import kotlinx.coroutines.delay

@Composable
fun ChartsScreen(consumptionDao: ConsumptionDao) {
    val scope = rememberCoroutineScope()
    val allConsumptions by consumptionDao.getConsumptionsForDate("2026-06-07").collectAsState(initial = emptyList()) // Simplified for demo, should be range query
    
    // In a real app, we'd query for a range of dates. For this demo, let's process the 
    // last 7 days including mock data if empty.
    val weeklyData = remember(allConsumptions) {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        days.map { day ->
            // Mocking some variation for the demo visual
            val tea = (10..30).random()
            val coffee = (5..20).random()
            day to (tea to coffee)
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text(text = "Weekly Consumption", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { (day, counts) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.height(160.dp)) {
                            // Tea bar
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(counts.first / 40f)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Coffee bar
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(counts.second / 40f)
                                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = day, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            LegendItem(color = MaterialTheme.colorScheme.primary, label = "Tea")
            Spacer(modifier = Modifier.width(24.dp))
            LegendItem(color = MaterialTheme.colorScheme.secondary, label = "Coffee")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
