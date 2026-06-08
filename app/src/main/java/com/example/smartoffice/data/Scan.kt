package com.example.smartoffice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class Scan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeName: String,
    val timestamp: Long
)
