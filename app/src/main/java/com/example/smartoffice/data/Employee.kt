package com.example.smartoffice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val leaveStatus: String,
    val dailyConsumption: Int,
    val attendanceStatus: String = "Present",
    val leaveBalance: Int = 10,
    val role: String = "Employee",
    val department: String = "General",
    val departmentLocalized: Map<String, String>? = null,
    val password: String = "dummy123",
    val profilePhotoUri: String? = null,
    val mobile: String = "",
    val email: String = "",
    val isOnline: Boolean = false
)
