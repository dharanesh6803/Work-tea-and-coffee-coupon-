package com.example.smartoffice.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val type: String, // e.g., "Casual", "Sick", "Annual"
    val startDate: String, // YYYY-MM-DD
    val endDate: String, // YYYY-MM-DD
    val reason: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val submissionTimestamp: Long = System.currentTimeMillis()
)

@Dao
interface LeaveRequestDao {
    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId ORDER BY submissionTimestamp DESC")
    fun getLeaveRequestsForEmployee(employeeId: Int): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests ORDER BY submissionTimestamp DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>>

    @Insert
    suspend fun insert(request: LeaveRequest)

    @Update
    suspend fun update(request: LeaveRequest)

    @Delete
    suspend fun delete(request: LeaveRequest)

    @Query("UPDATE leave_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)
}
