package com.example.smartoffice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duties")
data class Duty(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val titleLocalized: Map<String, String>? = null,
    val employeeId: Int,
    val date: String, // String format YYYY-MM-DD for easier calendar matching
    val timestamp: Long
)
