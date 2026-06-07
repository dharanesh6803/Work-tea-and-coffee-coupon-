package com.example.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository.getInstance(application)

    // Reactive database states
    val departments = repository.departments.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val employees = repository.employees.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val couponUsages = repository.couponUsages.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val leaveRequests = repository.leaveRequests.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val eventDuties = repository.eventDuties.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val groupMessages = repository.groupMessages.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Currently logged-in session
    var loggedInUser by mutableStateOf<Employee?>(null)
        private set

    // Selected wallpaper theme
    var activeWallpaper by mutableStateOf(SettingsPresets.wallpaperPresets[0])
    var activeTransparencyColor by mutableStateOf(SettingsPresets.transparencyColors[0])
    var isDarkMode by mutableStateOf(false)
    var notificationsEnabled by mutableStateOf(true)

    // OTP Simulated values
    var generatedOtp by mutableStateOf("")
        private set
    var isOtpSent by mutableStateOf(false)

    // Live state tracking for custom ID fields being built
    var customFields = mutableListOf<Pair<String, String>>()

    // Chat states
    private val _privateChats = MutableStateFlow<List<ChatMessage>>(emptyList())
    val privateChats: StateFlow<List<ChatMessage>> = _privateChats.asStateFlow()

    // AI Chatbot messages
    private val _aiChatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf("Hello! I am your Bangalore Club Smart Office Assistant. Ask me anything about coupons, leaves, or ID creation." to false)
    )
    val aiChatMessages = _aiChatMessages.asStateFlow()
    var isAiLoading by mutableStateOf(false)

    init {
        // Create initial default Department and Admin if empty in database to facilitate initial run
        viewModelScope.launch {
            departments.first { it.isNotEmpty() || true }
            val currentDepts = departments.value
            if (currentDepts.isEmpty()) {
                repository.insertDepartment(Department("HR", "Human Resources", "ADMIN1"))
                repository.insertDepartment(Department("CS", "Canteen Service", "CANT1"))
                repository.insertDepartment(Department("ENT", "Entertainment & Cultural", "HOD1"))
            }

            val currentEmps = employees.first()
            if (currentEmps.isEmpty()) {
                // Prepopulate with seed data so the user has immediate visual structures to explore
                repository.insertEmployee(
                    Employee(
                        employeeId = "ADMIN1",
                        name = "Sarah Jenkins",
                        mobile = "9876543210",
                        email = "sarah.admin@bangaloreclub.com",
                        departmentId = "HR",
                        role = "Admin",
                        dummyPassword = "123",
                        aadhaar = "1234 5678 9012",
                        address = "Bangalore Residency Road, Gate 1"
                    )
                )
                repository.insertEmployee(
                    Employee(
                        employeeId = "HOD1",
                        name = "Arthur Pendragon",
                        mobile = "9876543211",
                        email = "arthur.hod@bangaloreclub.com",
                        departmentId = "ENT",
                        role = "HOD",
                        dummyPassword = "123",
                        aadhaar = "4567 8901 2345",
                        address = "Cunningham Road, Bangalore"
                    )
                )
                repository.insertEmployee(
                    Employee(
                        employeeId = "EMP1",
                        name = "Ramesh Kumar",
                        mobile = "9876543212",
                        email = "ramesh@bangaloreclub.com",
                        departmentId = "ENT",
                        role = "Employee",
                        dummyPassword = "123",
                        dailyTeaQuota = 3,
                        dailyCoffeeQuota = 2,
                        aadhaar = "9012 3456 7890",
                        address = "Indiranagar 12th Cross, Bangalore"
                    )
                )
                repository.insertEmployee(
                    Employee(
                        employeeId = "CANT1",
                        name = "David Chef",
                        mobile = "9876543213",
                        email = "david@bangaloreclub.com",
                        departmentId = "CS",
                        role = "Canteen",
                        dummyPassword = "123",
                        aadhaar = "2345 6789 0123",
                        address = "MG Road, Bangalore"
                    )
                )

                // Add a sample Event
                repository.insertEvent(
                    EventDuty(
                        title = "Bangalore Club Annual Tea Fest",
                        eventDate = "2026-06-15",
                        assignedEmployeeId = "EMP1",
                        assignedEmployeeName = "Ramesh Kumar",
                        isOvertime = true
                    )
                )

                // Add sample Leave requests
                repository.insertLeave(
                    LeaveRequest(
                        employeeId = "EMP1",
                        employeeName = "Ramesh Kumar",
                        employeeRole = "Employee",
                        departmentId = "ENT",
                        startDate = "2026-06-20",
                        endDate = "2026-06-22",
                        reason = "Family wedding at Madras",
                        status = "Pending_HOD"
                    )
                )

                // Seed some coupon usages to draw graphs instantly
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val today = System.currentTimeMillis()
                repository.insertUsage(CouponUsage(employeeId = "EMP1", employeeName = "Ramesh Kumar", type = "Tea", timestamp = today - 86400000, recordedBy = "CANT1"))
                repository.insertUsage(CouponUsage(employeeId = "EMP1", employeeName = "Ramesh Kumar", type = "Tea", timestamp = today - 172800000, recordedBy = "CANT1"))
                repository.insertUsage(CouponUsage(employeeId = "EMP1", employeeName = "Ramesh Kumar", type = "Coffee", timestamp = today - 86400000, recordedBy = "CANT1"))
            }
        }
    }

    // Role Statistics for Admin
    val totalWorkersCount = employees.map { list -> list.size }
    val totalHodsCount = employees.map { list -> list.count { it.role == "HOD" } }
    val totalEmployeesCount = employees.map { list -> list.count { it.role == "Employee" } }
    val totalCanteenStaffCount = employees.map { list -> list.count { it.role == "Canteen" } }

    // Coupon Serviced metrics
    val totalTeaCouponsCount = couponUsages.map { list -> list.count { it.type == "Tea" } }
    val totalCoffeeCouponsCount = couponUsages.map { list -> list.count { it.type == "Coffee" } }
    val totalUnknownSalesCount = couponUsages.map { list -> list.count { it.type == "Unknown" } }

    // Leave counts
    val totalLeavesCount = leaveRequests.map { list -> list.size }
    val approvedLeavesCount = leaveRequests.map { list -> list.count { it.status == "Approved" } }
    val pendingLeavesCount = leaveRequests.map { list -> list.count { it.status == "Pending_HOD" || it.status == "Pending_Admin" } }
    val rejectedLeavesCount = leaveRequests.map { list -> list.count { it.status == "Rejected" } }

    // Private chats sync
    fun loadPrivateChats(otherUserId: String) {
        val currentUserId = loggedInUser?.employeeId ?: return
        viewModelScope.launch {
            repository.getPrivateMessages(currentUserId, otherUserId).collect {
                _privateChats.value = it
            }
        }
    }

    // Login mechanics
    fun loginWithPassword(emailOrMobile: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = employees.value.find {
                (it.email.equals(emailOrMobile, ignoreCase = true) || it.mobile == emailOrMobile) &&
                        it.dummyPassword == password
            }
            if (user != null) {
                loggedInUser = user
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun requestOtpLogin(emailOrMobile: String, onResult: (Boolean) -> Unit) {
        val user = employees.value.find {
            it.email.equals(emailOrMobile, ignoreCase = true) || it.mobile == emailOrMobile
        }
        if (user != null) {
            val otp = "${(1000..9999).random()}"
            generatedOtp = otp
            isOtpSent = true
            // Display simulated SMS/Email popup on screen toast
            Toast.makeText(getApplication(), "OTP Sent for Bangalore Club secure access: $otp", Toast.LENGTH_LONG).show()
            onResult(true)
        } else {
            onResult(false)
        }
    }

    fun verifyOtpLogin(emailOrMobile: String, enteredOtp: String, onResult: (Boolean) -> Unit) {
        if (enteredOtp == generatedOtp && isOtpSent) {
            val user = employees.value.find {
                it.email.equals(emailOrMobile, ignoreCase = true) || it.mobile == emailOrMobile
            }
            if (user != null) {
                loggedInUser = user
                isOtpSent = false
                onResult(true)
            } else {
                onResult(false)
            }
        } else {
            onResult(false)
        }
    }

    fun logout() {
        loggedInUser = null
        _privateChats.value = emptyList()
    }

    // Change Password Flow
    fun requestPasswordChangeOtp(oldPass: String, onResult: (Boolean) -> Unit) {
        val currentUser = loggedInUser ?: return
        if (currentUser.dummyPassword == oldPass) {
            val otp = "${(1000..9999).random()}"
            generatedOtp = otp
            Toast.makeText(getApplication(), "OTP Sent for changing password: $otp", Toast.LENGTH_LONG).show()
            onResult(true)
        } else {
            onResult(false)
        }
    }

    fun applyChangePassword(newPass: String, code: String, onResult: (Boolean) -> Unit) {
        val currentUser = loggedInUser ?: return
        if (code == generatedOtp) {
            viewModelScope.launch {
                val updated = currentUser.copy(dummyPassword = newPass)
                repository.updateEmployee(updated)
                loggedInUser = updated
                onResult(true)
            }
        } else {
            onResult(false)
        }
    }

    fun updateProfileInfo(name: String, mobile: String, email: String, aadhaar: String?, address: String?, photoUri: String?) {
        val currentUser = loggedInUser ?: return
        viewModelScope.launch {
            val updated = currentUser.copy(
                name = name,
                mobile = mobile,
                email = email,
                aadhaar = aadhaar,
                address = address,
                photoUri = photoUri
            )
            repository.updateEmployee(updated)
            loggedInUser = updated
            Toast.makeText(getApplication(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Admin commands
    fun createDepartment(id: String, name: String) {
        viewModelScope.launch {
            repository.insertDepartment(Department(id.uppercase(), name))
            Toast.makeText(getApplication(), "Department $id created!", Toast.LENGTH_SHORT).show()
        }
    }

    fun registerEmployee(
        id: String, name: String, mobile: String, email: String,
        deptId: String, role: String, teaQuota: Int, coffeeQuota: Int,
        pass: String, aadhaar: String?, address: String?, customFieldsJson: String?
    ) {
        viewModelScope.launch {
            val emp = Employee(
                employeeId = id,
                name = name,
                mobile = mobile,
                email = email,
                departmentId = deptId,
                role = role,
                dailyTeaQuota = teaQuota,
                dailyCoffeeQuota = coffeeQuota,
                dummyPassword = pass,
                aadhaar = aadhaar,
                address = address,
                customFieldsJson = customFieldsJson
            )
            repository.insertEmployee(emp)
            Toast.makeText(getApplication(), "Employee $name added successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun modifyEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.updateEmployee(employee)
            // If the admin is editing themselves, update session state too!
            if (loggedInUser?.employeeId == employee.employeeId) {
                loggedInUser = employee
            }
            Toast.makeText(getApplication(), "Employee details updated!", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeEmployee(emp: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(emp)
            Toast.makeText(getApplication(), "Employee accounts removed", Toast.LENGTH_SHORT).show()
        }
    }

    // Coupon consumption actions
    fun redeemCoupon(empId: String, type: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val emp = repository.getEmployeeById(empId) ?: return@launch onResult(false, "Employee not found!")

            // Calculate current consumption today
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val allowedLimit = if (type == "Tea") emp.dailyTeaQuota else emp.dailyCoffeeQuota
            val usagesToday = couponUsages.value.filter {
                it.employeeId == empId && it.type == type && it.timestamp >= todayStart
            }.sumOf { it.count }

            if (usagesToday >= allowedLimit) {
                onResult(false, "Quota reached! Daily limit is $allowedLimit cups of $type.")
            } else {
                val usage = CouponUsage(
                    employeeId = empId,
                    employeeName = emp.name,
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    recordedBy = loggedInUser?.employeeId ?: "Canteen-Staff"
                )
                repository.insertUsage(usage)
                onResult(true, "Successfully served 1 $type to ${emp.name}!")
            }
        }
    }

    fun recordUnknownCustomerSale(type: String) {
        viewModelScope.launch {
            val usage = CouponUsage(
                employeeId = "UNKNOWN",
                employeeName = "Unknown Customer",
                type = type,
                timestamp = System.currentTimeMillis(),
                recordedBy = loggedInUser?.employeeId ?: "Canteen"
            )
            repository.insertUsage(usage)
            Toast.makeText(getApplication(), "Recorded $type sale to unknown guest!", Toast.LENGTH_SHORT).show()
        }
    }

    // Leave Application Workflow
    fun applyLeave(startDate: String, endDate: String, reason: String) {
        val currentUser = loggedInUser ?: return
        viewModelScope.launch {
            val isHod = currentUser.role == "HOD"
            val initialStatus = if (isHod) "Pending_Admin" else "Pending_HOD"

            val leave = LeaveRequest(
                employeeId = currentUser.employeeId,
                employeeName = currentUser.name,
                employeeRole = currentUser.role,
                departmentId = currentUser.departmentId,
                startDate = startDate,
                endDate = endDate,
                reason = reason,
                status = initialStatus
            )
            repository.insertLeave(leave)
            Toast.makeText(getApplication(), "Leave applied successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun processLeaveByHOD(leaveId: Long, approve: Boolean, comment: String?) {
        viewModelScope.launch {
            val leave = repository.getLeaveById(leaveId) ?: return@launch
            val updated = leave.copy(
                status = if (approve) "Pending_Admin" else "Rejected",
                rejectReason = if (approve) null else (comment ?: "Declined by Department Head")
            )
            repository.updateLeave(updated)
            Toast.makeText(getApplication(), "Leave response recorded!", Toast.LENGTH_SHORT).show()
        }
    }

    fun processLeaveByAdmin(leaveId: Long, approve: Boolean, comment: String?) {
        viewModelScope.launch {
            val leave = repository.getLeaveById(leaveId) ?: return@launch
            val updated = leave.copy(
                status = if (approve) "Approved" else "Rejected",
                rejectReason = if (approve) null else (comment ?: "Declined by Admin / HR")
            )
            repository.updateLeave(updated)
            Toast.makeText(getApplication(), "Leave registration updated!", Toast.LENGTH_SHORT).show()
        }
    }

    // Event scheduling actions
    fun createEvent(title: String, date: String, assignedId: String, overtime: Boolean) {
        viewModelScope.launch {
            val emp = repository.getEmployeeById(assignedId)
            val empName = emp?.name ?: "All Team"
            val ev = EventDuty(
                title = title,
                eventDate = date,
                assignedEmployeeId = assignedId,
                assignedEmployeeName = empName,
                isOvertime = overtime
            )
            repository.insertEvent(ev)
            Toast.makeText(getApplication(), "Event duty added successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteEvent(event: EventDuty) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            Toast.makeText(getApplication(), "Event duty deleted", Toast.LENGTH_SHORT).show()
        }
    }

    // Internal Chat message sender
    fun sendGroupBroadcast(text: String) {
        val user = loggedInUser ?: return
        viewModelScope.launch {
            repository.insertMessage(
                ChatMessage(
                    senderId = user.employeeId,
                    senderName = user.name,
                    senderRole = user.role,
                    recipientId = null, // public group broadcast
                    messageText = text
                )
            )
        }
    }

    fun sendPrivateMessage(otherUserId: String, text: String) {
        val user = loggedInUser ?: return
        viewModelScope.launch {
            val message = ChatMessage(
                senderId = user.employeeId,
                senderName = user.name,
                senderRole = user.role,
                recipientId = otherUserId,
                messageText = text
            )
            repository.insertMessage(message)
            // Reload chats
            loadPrivateChats(otherUserId)
        }
    }

    // Backup import / export triggers
    fun initiateBackupExport(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportBackupJson()
                onResult(json)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun initiateRestoreImport(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackupJson(json)
            if (success) {
                // If the logged in user gets deleted or changed, reset login
                val currentUserId = loggedInUser?.employeeId
                if (currentUserId != null) {
                    val rechecked = repository.getEmployeeById(currentUserId)
                    loggedInUser = rechecked
                }
            }
            onResult(success)
        }
    }

    // AI Direct REST chatbot query flow using Option B from the gemini-api skill
    fun sendAiChatbotMessage(promptText: String) {
        if (promptText.isBlank()) return

        // Append user prompt to list of chats
        val list = _aiChatMessages.value.toMutableList()
        list.add(promptText to true)
        _aiChatMessages.value = list

        isAiLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val isKeyConfigured = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

            var aiResponseText = ""

            if (isKeyConfigured) {
                val currentUser = loggedInUser
                val userContext = if (currentUser != null) 
                    "You are the AI Assistant for the Bangalore Club. Current user context: Name=${currentUser.name}, Role=${currentUser.role}." 
                    else "You are the AI Assistant for the Bangalore Club."

                val systemPrePrompt = "$userContext Help the user answer rules on leaves (workflow: Employees apply -> HOD approval -> Admin HR finalized; HOD requests go straight to administration), canteen cup quotas of tea/coffee, and events scheduling. Keep answers concise and helpful."
                val reqBody = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "${systemPrePrompt}\nUser request: ${promptText}"))))
                )
                try {
                    val response = RetrofitClient.service.generateContent(apiKey, reqBody)
                    aiResponseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "I received an empty response. Please verify the topic."
                } catch (e: java.lang.Exception) {
                    aiResponseText = "API Error: ${e.message}. Using offline smart assistant fallback instead.\n" +
                            getOfflineFallbackResponse(promptText)
                }
            } else {
                // Fallback smart offline system immediately
                aiResponseText = getOfflineFallbackResponse(promptText)
            }

            withContext(Dispatchers.Main) {
                isAiLoading = false
                val updatedWithAi = _aiChatMessages.value.toMutableList()
                updatedWithAi.add(aiResponseText to false)
                _aiChatMessages.value = updatedWithAi
            }
        }
    }

    private fun getOfflineFallbackResponse(prompt: String): String {
        val query = prompt.lowercase(Locale.getDefault())
        return when {
            query.contains("leave") || query.contains("apply") -> {
                "📝 Bangalore Club Leave Rules:\n" +
                        "1. Employees apply choosing dates/reasons. It routes to HOD.\n" +
                        "2. HOD evaluates details. If approved, HR Admin gives final clearance.\n" +
                        "3. HODs apply directly, routing straight to administrative HR for approval."
            }
            query.contains("tea") || query.contains("coffee") || query.contains("coupon") || query.contains("quota") -> {
                "☕ Quota Rules:\n" +
                        "- Admin sets workers manual daily tea/coffee quotas on registration.\n" +
                        "- Coupons reset on the 1st of every month automatically.\n" +
                        "- Canteen staff scanned QRs record consumptions to log history."
            }
            query.contains("id") || query.contains("id card") -> {
                "🪪 Identity Card System:\n" +
                        "- Requires: Photo, ID, Name, Mobile, Email, Aadhaar, and Address.\n" +
                        "- Dynamic QR codes are generated directly for (a) Profile ID, (b) Tea coupon validation, (c) Coffee coupon validation.\n" +
                        "- Custom fields can be set dynamically by Admin."
            }
            query.contains("event") || query.contains("duty") -> {
                "📅 Event Support:\n" +
                        "- Administrators publish events & shift routines containing names and schedules.\n" +
                        "- visible to both employee rosters and canteen operators to coordinate duty days."
            }
            query.contains("otp") || query.contains("password") || query.contains("security") -> {
                "🔐 Security Details:\n" +
                        "- Login takes either Email or Mobile verification via generated virtual OTP.\n" +
                        "- Password resets require entering of Old password, verify verification OTP, and confirm details."
            }
            else -> {
                "Hello! I am your Bangalore Club smart office helper.\n" +
                        "You can ask me regarding: Leave rules, Canteen Tea & Coffee limits, ID Cards, or Events."
            }
        }
    }
}

// Retrofit interfaces used for Option B (Direct REST calling) as described in key skills!
data class GenerateContentRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerateContentResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}
