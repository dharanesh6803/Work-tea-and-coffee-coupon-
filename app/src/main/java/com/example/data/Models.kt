package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departments")
data class Department(
    @PrimaryKey val id: String, // String ID/Name of department
    val name: String,
    val hodEmployeeId: String? = null // Assigned Head of Department ID
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeId: String, // Manually set employee ID
    val name: String,
    val mobile: String,
    val email: String,
    val departmentId: String, // Matches department code
    val role: String, // "Admin", "HOD", "Employee", "Canteen"
    val dailyTeaQuota: Int = 2,
    val dailyCoffeeQuota: Int = 2,
    val dummyPassword: String,
    val photoUri: String? = null,
    val aadhaar: String? = null,
    val address: String? = null,
    val customFieldsJson: String? = null // Serialized JSON string of custom fields (Key-Value)
)

@Entity(tableName = "coupon_usages")
data class CouponUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val type: String, // "Tea", "Coffee", "Unknown"
    val timestamp: Long,
    val count: Int = 1,
    val recordedBy: String // Employee ID of recording canteen member
)

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val employeeRole: String,
    val departmentId: String,
    val startDate: String, // "YYYY-MM-DD" style
    val endDate: String, // "YYYY-MM-DD" style
    val reason: String,
    val status: String, // "Pending_HOD", "Pending_Admin", "Approved", "Rejected"
    val rejectReason: String? = null,
    val requestTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "event_duties")
data class EventDuty(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val eventDate: String, // "YYYY-MM-DD" style
    val assignedEmployeeId: String,
    val assignedEmployeeName: String,
    val isOvertime: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val senderName: String,
    val senderRole: String,
    val recipientId: String?, // String ID for private, null for group broadcast
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)
