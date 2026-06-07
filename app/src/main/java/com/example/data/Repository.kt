package com.example.data

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {

    val departments: Flow<List<Department>> = db.departmentDao().getAllDepartments()
    val employees: Flow<List<Employee>> = db.employeeDao().getAllEmployees()
    val couponUsages: Flow<List<CouponUsage>> = db.couponUsageDao().getAllUsages()
    val leaveRequests: Flow<List<LeaveRequest>> = db.leaveRequestDao().getAllLeaves()
    val eventDuties: Flow<List<EventDuty>> = db.eventDutyDao().getAllEvents()
    val groupMessages: Flow<List<ChatMessage>> = db.chatMessageDao().getGroupMessages()

    suspend fun insertDepartment(dept: Department) = db.departmentDao().insertDepartment(dept)
    suspend fun deleteDepartment(dept: Department) = db.departmentDao().deleteDepartment(dept)
    suspend fun getDepartmentById(id: String) = db.departmentDao().getDepartmentById(id)

    suspend fun getEmployeeById(id: String) = db.employeeDao().getEmployeeById(id)
    suspend fun insertEmployee(emp: Employee) = db.employeeDao().insertEmployee(emp)
    suspend fun updateEmployee(emp: Employee) = db.employeeDao().updateEmployee(emp)
    suspend fun deleteEmployee(emp: Employee) = db.employeeDao().deleteEmployee(emp)

    fun getUsagesForEmployee(empId: String): Flow<List<CouponUsage>> =
        db.couponUsageDao().getUsagesForEmployee(empId)
    suspend fun insertUsage(usage: CouponUsage) = db.couponUsageDao().insertUsage(usage)
    suspend fun deleteUsageById(id: Long) = db.couponUsageDao().deleteUsageById(id)

    fun getLeavesForEmployee(empId: String): Flow<List<LeaveRequest>> =
        db.leaveRequestDao().getLeavesForEmployee(empId)
    fun getLeavesForDepartment(deptId: String): Flow<List<LeaveRequest>> =
        db.leaveRequestDao().getLeavesForDepartment(deptId)
    suspend fun insertLeave(leave: LeaveRequest) = db.leaveRequestDao().insertLeave(leave)
    suspend fun getLeaveById(id: Long) = db.leaveRequestDao().getLeaveById(id)
    suspend fun updateLeave(leave: LeaveRequest) = db.leaveRequestDao().updateLeave(leave)

    suspend fun insertEvent(event: EventDuty) = db.eventDutyDao().insertEvent(event)
    suspend fun deleteEvent(event: EventDuty) = db.eventDutyDao().deleteEvent(event)

    fun getPrivateMessages(user1: String, user2: String): Flow<List<ChatMessage>> =
        db.chatMessageDao().getPrivateMessages(user1, user2)
    suspend fun insertMessage(msg: ChatMessage) = db.chatMessageDao().insertMessage(msg)

    // Backup & Restore utilities
    suspend fun exportBackupJson(): String {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(BackupWrapper::class.java)

        val data = BackupWrapper(
            departments = departments.first(),
            employees = employees.first(),
            couponUsages = couponUsages.first(),
            leaveRequests = leaveRequests.first(),
            eventDuties = eventDuties.first(),
            chatMessages = db.chatMessageDao().getGroupMessages().first() // Save all simple broadcast messages
        )
        return adapter.toJson(data)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(BackupWrapper::class.java)
            val data = adapter.fromJson(jsonString) ?: return false

            // Use a transaction or block to clear and rebuild
            db.clearAllTables()

            // Restore elements
            data.departments.forEach { db.departmentDao().insertDepartment(it) }
            data.employees.forEach { db.employeeDao().insertEmployee(it) }
            data.couponUsages.forEach { db.couponUsageDao().insertUsage(it) }
            data.leaveRequests.forEach { db.leaveRequestDao().insertLeave(it) }
            data.eventDuties.forEach { db.eventDutyDao().insertEvent(it) }
            data.chatMessages.forEach { db.chatMessageDao().insertMessage(it) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: Repository? = null

        fun getInstance(context: Context): Repository {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "office_smart_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                val repo = Repository(db)
                INSTANCE = repo
                repo
            }
        }
    }
}

class BackupWrapper(
    val departments: List<Department>,
    val employees: List<Employee>,
    val couponUsages: List<CouponUsage>,
    val leaveRequests: List<LeaveRequest>,
    val eventDuties: List<EventDuty>,
    val chatMessages: List<ChatMessage>
)
