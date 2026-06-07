package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<Department>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: Department)

    @Delete
    suspend fun deleteDepartment(department: Department)

    @Query("SELECT * FROM departments WHERE id = :id LIMIT 1")
    suspend fun getDepartmentById(id: String): Department?
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE employeeId = :employeeId LIMIT 1")
    suspend fun getEmployeeById(employeeId: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("SELECT * FROM employees WHERE role = :role")
    fun getEmployeesByRole(role: String): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE departmentId = :deptId")
    fun getEmployeesByDepartment(deptId: String): Flow<List<Employee>>
}

@Dao
interface CouponUsageDao {
    @Query("SELECT * FROM coupon_usages ORDER BY timestamp DESC")
    fun getAllUsages(): Flow<List<CouponUsage>>

    @Query("SELECT * FROM coupon_usages WHERE employeeId = :empId ORDER BY timestamp DESC")
    fun getUsagesForEmployee(empId: String): Flow<List<CouponUsage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: CouponUsage)

    @Query("DELETE FROM coupon_usages WHERE id = :id")
    suspend fun deleteUsageById(id: Long)
}

@Dao
interface LeaveRequestDao {
    @Query("SELECT * FROM leave_requests ORDER BY requestTimestamp DESC")
    fun getAllLeaves(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE employeeId = :empId ORDER BY requestTimestamp DESC")
    fun getLeavesForEmployee(empId: String): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE departmentId = :deptId ORDER BY requestTimestamp DESC")
    fun getLeavesForDepartment(deptId: String): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveRequest)

    @Query("SELECT * FROM leave_requests WHERE id = :id LIMIT 1")
    suspend fun getLeaveById(id: Long): LeaveRequest?

    @Update
    suspend fun updateLeave(leave: LeaveRequest)
}

@Dao
interface EventDutyDao {
    @Query("SELECT * FROM event_duties ORDER BY eventDate ASC")
    fun getAllEvents(): Flow<List<EventDuty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventDuty)

    @Delete
    suspend fun deleteEvent(event: EventDuty)
}

@Dao
interface ChatMessageDao {
    // Group messages are those where recipientId is null (broadcast to group)
    @Query("SELECT * FROM chat_messages WHERE recipientId IS NULL ORDER BY timestamp ASC")
    fun getGroupMessages(): Flow<List<ChatMessage>>

    // Private messages between two users (represented by senderId/recipientId pairs)
    @Query("SELECT * FROM chat_messages WHERE (senderId = :user1 AND recipientId = :user2) OR (senderId = :user2 AND recipientId = :user1) ORDER BY timestamp ASC")
    fun getPrivateMessages(user1: String, user2: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Database(
    entities = [
        Department::class,
        Employee::class,
        CouponUsage::class,
        LeaveRequest::class,
        EventDuty::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun departmentDao(): DepartmentDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun couponUsageDao(): CouponUsageDao
    abstract fun leaveRequestDao(): LeaveRequestDao
    abstract fun eventDutyDao(): EventDutyDao
    abstract fun chatMessageDao(): ChatMessageDao
}
