package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Employee
import com.example.data.Department
import com.example.data.LeaveRequest
import com.example.data.EventDuty
import com.example.data.CouponUsage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SmartOfficeMainLayout(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user = viewModel.loggedInUser

    // Custom 3D Breathing Tilt State
    val infiniteTransition = rememberInfiniteTransition(label = "3D Breathing")
    val breathingAngle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "TiltAngle"
    )

    // Current page inside dashboard panels
    var adminActiveTab by remember { mutableStateOf("Overview") } // "Overview", "Workers", "Sales", "Leaves", "Dept", "Events"
    var selectedScreen by remember { mutableStateOf("Home") } // "Home", "Chat", "AI Chat", "Settings", "ID QR"

    // Base background with Wallpaper preset & Theme Colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                if (viewModel.activeWallpaper.isGradient) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = viewModel.activeWallpaper.colors
                        )
                    )
                } else {
                    drawRect(color = viewModel.activeWallpaper.colors.firstOrNull() ?: Color.Black)
                }
            }
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Smart Custom Header with Bangalore Club Styling & Crest Icon
            SmartAppHeader(
                onLogout = { viewModel.logout() },
                user = user,
                onNavSettings = { selectedScreen = "Settings" },
                angle = breathingAngle
            )

            // Dynamic view loading depending on login session
            if (user == null) {
                // If user not signed in - display Glassmorphic Secure Login Box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoginCardView(viewModel = viewModel)
                }
            } else {
                // Nested layout inside glass container
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Quick stats pill row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Club Role: ${user.role} (${user.departmentId})",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("user_role_label")
                        )
                        Row {
                            IconButton(onClick = { selectedScreen = "Home" }) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = if (selectedScreen == "Home") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                            IconButton(onClick = { selectedScreen = "Chat" }) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = "Chat Lounge",
                                    tint = if (selectedScreen == "Chat") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                            IconButton(onClick = { selectedScreen = "AI Chat" }) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = "AI Help",
                                    tint = if (selectedScreen == "AI Chat") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                            // Only Admins see Settings
                            if (user.role == "Admin") {
                                IconButton(onClick = { selectedScreen = "Settings" }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = if (selectedScreen == "Settings") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }


                    // Main Active body
                    AnimatedContent(
                        targetState = selectedScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                        },
                        label = "Screen Transition"
                    ) { screen ->
                        when (screen) {
                            "Home" -> {
                                when (user.role) {
                                    "Admin" -> AdminMainDashboard(viewModel = viewModel, activeTab = adminActiveTab, onTabSelect = { adminActiveTab = it })
                                    "HOD" -> HodDashboardView(viewModel = viewModel)
                                    "Employee" -> EmployeeDashboardView(viewModel = viewModel)
                                    "Canteen" -> CanteenDashboardView(viewModel = viewModel)
                                }
                            }
                            "Chat" -> ChatLoungeView(viewModel = viewModel)
                            "AI Chat" -> AiRosterAssistantView(viewModel = viewModel)
                            "Settings" -> ThemeSettingsView(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// --------------------------- HEADER CREST ---------------------------
@Composable
fun SmartAppHeader(onLogout: () -> Unit, user: Employee?, onNavSettings: () -> Unit, angle: Float) {
    val isLight = MaterialTheme.colorScheme.background == Color(0xFFF3F4F9)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLight) 3.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationZ = angle * 0.1f
                cameraDistance = 8 * density
            }
            .border(
                1.dp,
                if (isLight) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Bangalore Club visual Crest Emblem: rounded-xl with indigo background (or gold in darkmode)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) Color(0xFF4F46E5) else Color(0xFFE5A93B))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BC",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLight) Color.White else Color(0xFF1B365D)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Bangalore Club",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLight) Color(0xFF1E293B) else Color(0xFFE5A93B),
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "Tea & ID Management",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isLight) Color(0xFF64748B) else Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavSettings,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLight) Color(0xFFF1F5F9) else Color.White.copy(0.1f))
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (isLight) Color(0xFF64748B) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (user != null) {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLight) Color(0xFFFEE2E2) else Color.Red.copy(0.15f))
                            .testTag("logout_button")
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// --------------------------- ID/BARCODE QR CANVAS DRAW ---------------------------
@Composable
fun BarcodeQrEmulationBox(data: String, colorAccent: Color) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .padding(10.dp)
            .border(1.dp, colorAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(modifier = Modifier.size(90.dp)) {
                // Draw dynamic security matrix code simulating secure QR
                val steps = 8
                val cellSize = size.width / steps
                val seedValue = data.hashCode().toLong()
                val oldRandom = Random(seedValue)

                for (r in 0 until steps) {
                    for (c in 0 until steps) {
                        // Keep corners dark for QR visual anchors
                        val isAnchor = (r < 2 && c < 2) || (r >= steps - 2 && c < 2) || (r < 2 && c >= steps - 2)
                        if (isAnchor || oldRandom.nextBoolean()) {
                            drawRect(
                                color = if (isAnchor) Color(0xFF1B365D) else colorAccent,
                                topLeft = androidx.compose.ui.geometry.Offset(c * cellSize, r * cellSize),
                                size = androidx.compose.ui.geometry.Size(cellSize - 1f, cellSize - 1f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "VERIFIED: ${data.take(8).uppercase()}",
                fontSize = 8.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --------------------------- SYSTEM SECURE LOGIN CARD ---------------------------
@Composable
fun LoginCardView(viewModel: MainViewModel) {
    var isOtpFlow by remember { mutableStateOf(false) }
    var credentialInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }

    val glassBg = viewModel.activeTransparencyColor.color

    Card(
        colors = CardDefaults.cardColors(containerColor = glassBg.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Account Authentication",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Authorized Bangalore Club Personnel Access Only",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            TabRow(
                selectedTabIndex = if (isOtpFlow) 1 else 0,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(0.2f))
            ) {
                Tab(
                    selected = !isOtpFlow,
                    onClick = { isOtpFlow = false },
                    text = { Text("ID Password") }
                )
                Tab(
                    selected = isOtpFlow,
                    onClick = { isOtpFlow = true },
                    text = { Text("Secure OTP Login") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = credentialInput,
                onValueChange = { credentialInput = it },
                label = { Text("Mobile or Email ID", color = Color.White) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Yellow,
                    unfocusedBorderColor = Color.White.copy(0.4f)
                ),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "", tint = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_credential_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isOtpFlow) {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password", color = Color.White) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Yellow,
                        unfocusedBorderColor = Color.White.copy(0.4f)
                    ),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "", tint = Color.White) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.loginWithPassword(credentialInput, passwordInput) { success ->
                            if (!success) {
                                Toast.makeText(viewModel.getApplication(), "Invalid Credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_login_button")
                ) {
                    Text("Unlock Dashboard", color = Color(0xFF1B365D), fontWeight = FontWeight.Bold)
                }
            } else {
                if (viewModel.isOtpSent) {
                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { enteredOtp = it },
                        label = { Text("Enter 4-Digit OTP", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Yellow,
                            unfocusedBorderColor = Color.White.copy(0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.verifyOtpLogin(credentialInput, enteredOtp) { success ->
                                if (!success) {
                                    Toast.makeText(viewModel.getApplication(), "OTP Verification Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Verify & Access", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.requestOtpLogin(credentialInput) { success ->
                                if (!success) {
                                    Toast.makeText(viewModel.getApplication(), "ID not recognized", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9A7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Request Secret OTP", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --------------------------- ADMIN DASHBOARD WORKSPACE ---------------------------
@Composable
fun AdminMainDashboard(viewModel: MainViewModel, activeTab: String, onTabSelect: (String) -> Unit) {
    val totalWorkers by viewModel.totalWorkersCount.collectAsState(initial = 0)
    val totalHods by viewModel.totalHodsCount.collectAsState(initial = 0)
    val totalEmps by viewModel.totalEmployeesCount.collectAsState(initial = 0)
    val totalCanteens by viewModel.totalCanteenStaffCount.collectAsState(initial = 0)

    val teaCount by viewModel.totalTeaCouponsCount.collectAsState(initial = 0)
    val coffeeCount by viewModel.totalCoffeeCouponsCount.collectAsState(initial = 0)
    val unknownSales by viewModel.totalUnknownSalesCount.collectAsState(initial = 0)

    val leavesCount by viewModel.totalLeavesCount.collectAsState(initial = 0)
    val approvedCount by viewModel.approvedLeavesCount.collectAsState(initial = 0)
    val pendingCount by viewModel.pendingLeavesCount.collectAsState(initial = 0)
    val rejectedCount by viewModel.rejectedLeavesCount.collectAsState(initial = 0)
    val leaveList by viewModel.leaveRequests.collectAsState(initial = emptyList())

    var showAddEmployeeModel by remember { mutableStateOf(false) }
    var showAddDeptModel by remember { mutableStateOf(false) }
    var showAddRosterRoster by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Admin Dynamic Tabs Row
        LazyRow(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            val tabs = listOf("Overview", "Workers", "Sales", "Leaves", "Department", "Rosters")
            items(tabs) { tab ->
                val isSelected = activeTab == tab
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor
                    ),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { onTabSelect(tab) }
                ) {
                    Text(
                        text = tab,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Active panel layout
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (activeTab) {
                    "Overview" -> {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val isWide = maxWidth >= 600.dp
                            if (isWide) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        AdminWorkerStatsCard(
                                            viewModel = viewModel,
                                            totalWorkers = totalWorkers,
                                            totalHods = totalHods,
                                            totalEmps = totalEmps,
                                            totalCanteens = totalCanteens
                                        )
                                        AdminSalesOverviewCard(
                                            viewModel = viewModel,
                                            teaCount = teaCount,
                                            coffeeCount = coffeeCount,
                                            unknownSales = unknownSales
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        AdminLeaveSummaryCard(
                                            viewModel = viewModel,
                                            leaveList = leaveList,
                                            leavesCount = leavesCount,
                                            approvedCount = approvedCount,
                                            pendingCount = pendingCount,
                                            rejectedCount = rejectedCount
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AdminWorkerStatsCard(
                                        viewModel = viewModel,
                                        totalWorkers = totalWorkers,
                                        totalHods = totalHods,
                                        totalEmps = totalEmps,
                                        totalCanteens = totalCanteens
                                    )
                                    AdminSalesOverviewCard(
                                        viewModel = viewModel,
                                        teaCount = teaCount,
                                        coffeeCount = coffeeCount,
                                        unknownSales = unknownSales
                                    )
                                    AdminLeaveSummaryCard(
                                        viewModel = viewModel,
                                        leaveList = leaveList,
                                        leavesCount = leavesCount,
                                        approvedCount = approvedCount,
                                        pendingCount = pendingCount,
                                        rejectedCount = rejectedCount
                                    )
                                }
                            }
                        }
                    }
                    "Workers" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Worker Directory ($totalWorkers)",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showAddEmployeeModel = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("+ Employee", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        // Stats Grid Row
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatMiniBadge(label = "HODs", value = "$totalHods", color = Color(0xFFE5A93B))
                            StatMiniBadge(label = "Staffs", value = "$totalEmps", color = Color(0xFF00C9A7))
                            StatMiniBadge(label = "Kitchen", value = "$totalCanteens", color = Color.Cyan)
                        }

                        val employeeList by viewModel.employees.collectAsState()
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(employeeList) { emp ->
                                EmployeeProfileRow(employee = emp, onRemove = { viewModel.removeEmployee(it) })
                            }
                        }
                    }
                    "Sales" -> {
                        Text("Sales Terminal & Coupons Statistics", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatBarCount(label = "☕ Tea Coupons Serviced", count = teaCount, color = Color(0xFF00C9A7))
                        StatBarCount(label = "☕ Coffee Coupons Serviced", count = coffeeCount, color = Color(0xFFE67E22))
                        StatBarCount(label = "🧑 Unknown Customer logs", count = unknownSales, color = MaterialTheme.colorScheme.primary)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Backup External storage option triggers
                        var backupDataJson by remember { mutableStateOf("") }
                        Button(
                            onClick = {
                                viewModel.initiateBackupExport { json ->
                                    if (json != null) {
                                        backupDataJson = json
                                        Toast.makeText(viewModel.getApplication(), "Data backup exported! Saving locally...", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Database Safeguard (JSON)", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        if (backupDataJson.isNotEmpty()) {
                            Text(
                                "Safeguard Content: ${backupDataJson.take(150)}...",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    "Leaves" -> {
                        Text("Leave Clearances & Approvals", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatMiniBadge(label = "Applied", value = "$leavesCount", color = MaterialTheme.colorScheme.primary)
                            StatMiniBadge(label = "Approved", value = "$approvedCount", color = Color(0xFF00C9A7))
                            StatMiniBadge(label = "Pending", value = "$pendingCount", color = Color(0xFFE67E22))
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(leaveList) { leave ->
                                LeaveAdminRequestRow(leave = leave, onApprove = { viewModel.processLeaveByAdmin(leave.id, true, null) }, onReject = { viewModel.processLeaveByAdmin(leave.id, false, "Declined by Admin") })
                            }
                        }
                    }
                    "Department" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Club Departments", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Button(onClick = { showAddDeptModel = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                Text("+ Dept", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        val deptList by viewModel.departments.collectAsState()
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(deptList) { dept ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                ) {
                                    Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(dept.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text("Code: ${dept.id}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                        }
                                        Text("HOD Assigned: ${dept.hodEmployeeId ?: "None"}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                    "Rosters" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Shift Events & Roster Duty", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Button(onClick = { showAddRosterRoster = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                Text("+ Publish", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        val eventsList by viewModel.eventDuties.collectAsState()
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(eventsList) { ev ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                ) {
                                    Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(ev.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text("Date: ${ev.eventDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(ev.assignedEmployeeName, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            if (ev.isOvertime) Text("Overtime Shifts", color = Color(0xFFE67E22), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal forms as overlay containers
    if (showAddEmployeeModel) {
        AddEmployeeModal(viewModel = viewModel, onDismiss = { showAddEmployeeModel = false })
    }
    if (showAddDeptModel) {
        AddDeptModal(viewModel = viewModel, onDismiss = { showAddDeptModel = false })
    }
    if (showAddRosterRoster) {
        AddEventModal(viewModel = viewModel, onDismiss = { showAddRosterRoster = false })
    }
}

@Composable
fun StatMiniBadge(label: String, value: String, color: Color) {
    val isLight = MaterialTheme.colorScheme.background == Color(0xFFF3F4F9)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLight) color.copy(alpha = 0.08f) else color.copy(alpha = 0.15f)
        ),
        modifier = Modifier.width(100.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun StatBarCount(label: String, count: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("$count Cups", color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (count / 100f).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun EmployeeProfileRow(employee: Employee, onRemove: (Employee) -> Unit) {
    var viewIdCard by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(employee.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    Text("ID: ${employee.employeeId} • Role: ${employee.role}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                }
                Row {
                    IconButton(
                        onClick = { viewIdCard = !viewIdCard },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(0.08f))
                    ) {
                        Icon(
                            Icons.Default.Badge,
                            contentDescription = "View ID Card",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { onRemove(employee) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Red.copy(0.08f))
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (viewIdCard) {
                Spacer(modifier = Modifier.height(12.dp))
                // Render identity card structure
                CrestIdentityCard(employee = employee)
            }
        }
    }
}

@Composable
fun CrestIdentityCard(employee: Employee) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B365D)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5A93B), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bangalore Club Gold Brand Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = "", tint = Color(0xFFE5A93B), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("BANGALORE CLUB ID", fontWeight = FontWeight.Bold, color = Color(0xFFE5A93B), fontSize = 14.sp)
            }
            Divider(color = Color(0xFFE5A93B), modifier = Modifier.padding(vertical = 8.dp))

            // Photo Placeholder & Info
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("NAME: ${employee.name}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    Text("EMP ID: ${employee.employeeId}", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    Text("DEPT: ${employee.departmentId}", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    Text("AADHAAR: ${employee.aadhaar ?: "N/A"}", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    Text("ADD: ${employee.address ?: "N/A"}", color = Color.White.copy(0.8f), fontSize = 10.sp)
                }

                // Emulated dynamic QR Code for ID Card Access
                Spacer(modifier = Modifier.width(10.dp))
                BarcodeQrEmulationBox(data = "IDCard:${employee.employeeId}", colorAccent = Color(0xFF1B365D))
            }
        }
    }
}

@Composable
fun LeaveAdminRequestRow(leave: LeaveRequest, onApprove: () -> Unit, onReject: () -> Unit) {
    val isLight = MaterialTheme.colorScheme.background == Color(0xFFF3F4F9)
    val statusColor = when {
        leave.status.contains("Approved", ignoreCase = true) -> Color(0xFF00C9A7)
        leave.status.contains("Pending", ignoreCase = true) -> Color(0xFFE67E22)
        else -> Color.Red
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(leave.employeeName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    Text("Period: ${leave.startDate} to ${leave.endDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                    Text("Reason: ${leave.reason}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.9f))
                }
                Text(
                    text = leave.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            if (leave.status.contains("Pending")) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.1f)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Decline", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Approve", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// --------------------------- DEPT & EVENT ADDITION FORMS ---------------------------
@Composable
fun AddDeptModal(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var depId by remember { mutableStateOf("") }
    var depName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Department", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = depId, onValueChange = { depId = it }, label = { Text("Department Code (e.g. HR)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = depName, onValueChange = { depName = it }, label = { Text("Department Name") })
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.createDepartment(depId, depName)
                onDismiss()
            }) { Text("Add Department") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    )
}

@Composable
fun AddEventModal(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("2026-06-15") }
    var assignedId by remember { mutableStateOf("") }
    var overtime by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Event & Duty Shift", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = eventDate, onValueChange = { eventDate = it }, label = { Text("Event Date (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = assignedId, onValueChange = { assignedId = it }, label = { Text("Assign Worker ID") })
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = overtime, onCheckedChange = { overtime = it })
                    Text("Overtime duty shift assignments")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.createEvent(title, eventDate, assignedId, overtime)
                onDismiss()
            }) { Text("Publish & Assign") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    )
}

@Composable
fun AddEmployeeModal(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var empId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var deptId by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Employee") } // "Employee", "HOD", "Canteen"
    var pass by remember { mutableStateOf("123") }
    var teaQuota by remember { mutableStateOf("2") }
    var coffeeQuota by remember { mutableStateOf("2") }
    var aadhaar by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Employee Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                LazyColumn {
                    item {
                        OutlinedTextField(value = empId, onValueChange = { empId = it }, label = { Text("Employee ID") })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Number") })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Mail ID") })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = deptId, onValueChange = { deptId = it }, label = { Text("Department Code") })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role (Employee/HOD/Canteen)") })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = teaQuota, onValueChange = { teaQuota = it }, label = { Text("Tea Quota (Daily)") })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = coffeeQuota, onValueChange = { coffeeQuota = it }, label = { Text("Coffee Quota (Daily)") })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar Card No.") })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Living Address") })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.registerEmployee(
                    id = empId, name = name, mobile = mobile, email = email,
                    deptId = deptId, role = role, teaQuota = teaQuota.toIntOrNull() ?: 2,
                    coffeeQuota = coffeeQuota.toIntOrNull() ?: 2, pass = pass,
                    aadhaar = aadhaar, address = address, customFieldsJson = null
                )
                onDismiss()
            }) { Text("Complete Register") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    )
}

// --------------------------- HOD DASHBOARD PANEL ---------------------------
@Composable
fun HodDashboardView(viewModel: MainViewModel) {
    val user = viewModel.loggedInUser ?: return
    val leaveList by viewModel.leaveRequests.collectAsState()
    val departments by viewModel.departments.collectAsState()

    var showApplyLeave by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Department Management Dashboard", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Department Code: ${user.departmentId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { showApplyLeave = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Submit direct HR Leave", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Subordinate Leave Petitions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        val filteredDeptLeaves = leaveList.filter { it.departmentId == user.departmentId && it.employeeId != user.employeeId }

        if (filteredDeptLeaves.isEmpty()) {
            Text("No pending staff leaves for your division.", color = MaterialTheme.colorScheme.onSurface.copy(0.5f), modifier = Modifier.padding(vertical = 12.dp))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredDeptLeaves) { leave ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(leave.employeeName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                            Text("Dates: ${leave.startDate} to ${leave.endDate}", color = MaterialTheme.colorScheme.onSurface.copy(0.7f), fontSize = 12.sp)
                            Text("Reason: ${leave.reason}", color = MaterialTheme.colorScheme.onSurface.copy(0.9f), fontSize = 12.sp)

                            if (leave.status == "Pending_HOD") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    Button(onClick = { viewModel.processLeaveByHOD(leave.id, false, "Declined by HOD") }, colors = ButtonDefaults.buttonColors(Color.Red.copy(0.1f))) {
                                        Text("Decline", color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = { viewModel.processLeaveByHOD(leave.id, true, null) }, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)) {
                                        Text("Approve", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Text("Status: ${leave.status}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showApplyLeave) {
        ApplyLeaveModal(viewModel = viewModel, onDismiss = { showApplyLeave = false })
    }
}

// --------------------------- EMPLOYEE DASHBOARD PANEL ---------------------------
@Composable
fun EmployeeDashboardView(viewModel: MainViewModel) {
    val user = viewModel.loggedInUser ?: return
    val usages by viewModel.couponUsages.collectAsState(initial = emptyList())
    val leaveList by viewModel.leaveRequests.collectAsState(initial = emptyList())

    val employeeUsages = usages.filter { it.employeeId == user.employeeId }
    val employeeLeaves = leaveList.filter { it.employeeId == user.employeeId }

    var showApplyLeave by remember { mutableStateOf(false) }
    var selectedRecordDetail by remember { mutableStateOf<CouponUsage?>(null) }
    var selectedCouponsTab by remember { mutableStateOf(true) } // true for tea, false for coffee

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Your Canteen Quota Overview", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            QuotaConsumptionCard(title = "Tea limit today", quota = user.dailyTeaQuota, consumed = employeeUsages.count { it.type == "Tea" }, color = Color(0xFF00C9A7))
            QuotaConsumptionCard(title = "Coffee limit today", quota = user.dailyCoffeeQuota, consumed = employeeUsages.count { it.type == "Coffee" }, color = Color(0xFFE67E22))
        }

        TabRow(
            selectedTabIndex = if (selectedCouponsTab) 0 else 1,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = selectedCouponsTab, onClick = { selectedCouponsTab = true }) {
                Text(
                    text = "Tea Usage Log",
                    modifier = Modifier.padding(10.dp),
                    fontWeight = if (selectedCouponsTab) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedCouponsTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
            Tab(selected = !selectedCouponsTab, onClick = { selectedCouponsTab = false }) {
                Text(
                    text = "Coffee Usage Log",
                    modifier = Modifier.padding(10.dp),
                    fontWeight = if (!selectedCouponsTab) FontWeight.Bold else FontWeight.Normal,
                    color = if (!selectedCouponsTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
        }

        val filteredLogs = employeeUsages.filter { if (selectedCouponsTab) it.type == "Tea" else it.type == "Coffee" }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredLogs) { log ->
                val sdf = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault())
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedRecordDetail = log },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(sdf.format(Date(log.timestamp)), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("+1 Cup Serviced", color = Color(0xFF00C9A7), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { showApplyLeave = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("Request Officer Leave", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    // Quick download QR visual mockup
                    Toast.makeText(viewModel.getApplication(), "Crest ID and Coupons QR code saved as PDF Image successfully!", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9A7)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("Get Digital ID QR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showApplyLeave) {
        ApplyLeaveModal(viewModel = viewModel, onDismiss = { showApplyLeave = false })
    }

    selectedRecordDetail?.let { log ->
        val sdfLong = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm:ss", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { selectedRecordDetail = null },
            title = { Text("Coupon Record Detail") },
            text = {
                Column {
                    Text("Type Serviced: ${log.type}", fontWeight = FontWeight.Bold)
                    Text("Exact Timestamp: ${sdfLong.format(Date(log.timestamp))}")
                    Text("Dispensed By Terminal: ${log.recordedBy}")
                }
            },
            confirmButton = {
                Button(onClick = { selectedRecordDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun QuotaConsumptionCard(title: String, quota: Int, consumed: Int, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        modifier = Modifier.width(160.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$consumed / $quota", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text("Cups Redeemed Today", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
        }
    }
}

@Composable
fun ApplyLeaveModal(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var sDate by remember { mutableStateOf("2026-06-15") }
    var eDate by remember { mutableStateOf("2026-06-17") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Leave Absence") },
        text = {
            Column {
                OutlinedTextField(value = sDate, onValueChange = { sDate = it }, label = { Text("Start Date (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = eDate, onValueChange = { eDate = it }, label = { Text("End Date (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason for Absence") })
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.applyLeave(sDate, eDate, reason)
                onDismiss()
            }) { Text("Request Work-Absence") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    )
}

// --------------------------- CANTEEN OPERATOR DASHBOARD PANEL ---------------------------
@Composable
fun CanteenDashboardView(viewModel: MainViewModel) {
    var searchWorkerId by remember { mutableStateOf("") }
    var operationResultMsg by remember { mutableStateOf("") }
    val isLight = MaterialTheme.colorScheme.background == Color(0xFFF3F4F9)

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Canteen Dispenser Terminal", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Logged in: ${viewModel.loggedInUser?.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchWorkerId,
            onValueChange = { searchWorkerId = it },
            label = { Text("Worker ID (Or scan emulated QR)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            ),
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    viewModel.redeemCoupon(searchWorkerId, "Tea") { success, msg ->
                        operationResultMsg = msg
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9A7)),
                modifier = Modifier.weight(1f).padding(end = 6.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.Coffee, contentDescription = "", tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Redeem Tea", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.redeemCoupon(searchWorkerId, "Coffee") { success, msg ->
                        operationResultMsg = msg
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22)),
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.LocalCafe, contentDescription = "", tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Redeem Coffee", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Non-Member / Unknown Guest Orders", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = { viewModel.recordUnknownCustomerSale("Tea") },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary.copy(0.1f)),
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    ) {
                        Text("Guest Tea", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.recordUnknownCustomerSale("Coffee") },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary.copy(0.1f)),
                        modifier = Modifier.weight(1f).padding(start = 6.dp)
                    ) {
                        Text("Guest Coffee", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (operationResultMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = operationResultMsg,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// --------------------------- COMMUNICATION CHAT LOUNGE ---------------------------
@Composable
fun ChatLoungeView(viewModel: MainViewModel) {
    val user = viewModel.loggedInUser ?: return
    val groupMsgs by viewModel.groupMessages.collectAsState(initial = emptyList())
    var currentTxt by remember { mutableStateOf("") }
    val isLight = MaterialTheme.colorScheme.background == Color(0xFFF3F4F9)

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Lounge Broadcast - Admin & Staff", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.12f), modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = false
        ) {
            items(groupMsgs) { msg ->
                val isOwn = msg.senderId == user.employeeId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOwn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(0.7f)
                        ),
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp, 
                            topEnd = 16.dp, 
                            bottomStart = if (isOwn) 16.dp else 4.dp, 
                            bottomEnd = if (isOwn) 4.dp else 16.dp
                        ),
                        border = BorderStroke(1.dp, if (isOwn) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!isOwn) {
                                Text(msg.senderName, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 2.dp))
                            }
                            Text(msg.messageText, color = if (isOwn) Color.White else MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentTxt,
                onValueChange = { currentTxt = it },
                placeholder = { Text("Broadcast feedback or notices...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                ),
                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (currentTxt.isNotEmpty()) {
                        viewModel.sendGroupBroadcast(currentTxt)
                        currentTxt = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// --------------------------- AI ROSTER CHATBOT terminal ---------------------------
@Composable
fun AiRosterAssistantView(viewModel: MainViewModel) {
    val msgs by viewModel.aiChatMessages.collectAsState()
    var inputTxt by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("AI Smart Office Support bot", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Real-time automated guidance connected to Gemini AI API", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.12f), modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

        // Chats body
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(msgs) { msg ->
                val isUser = msg.second
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(0.7f)
                        ),
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp, 
                            topEnd = 16.dp, 
                            bottomStart = if (isUser) 16.dp else 4.dp, 
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        border = BorderStroke(1.dp, if (isUser) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "You" else "Bangalore Club AI", 
                                fontWeight = FontWeight.ExtraBold, 
                                color = if (isUser) Color.White.copy(0.8f) else MaterialTheme.colorScheme.primary, 
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(msg.first, color = if (isUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 13.sp)
                        }
                    }
                }
            }
            if (viewModel.isAiLoading) {
                item {
                    Text("AI is processing reply...", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputTxt,
                onValueChange = { inputTxt = it },
                placeholder = { Text("Ask about leave limits, coffee coupons, rosters...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                ),
                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputTxt.isNotBlank()) {
                        viewModel.sendAiChatbotMessage(inputTxt)
                        inputTxt = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// --------------------------- THEME AND UTILITY SETTINGS ---------------------------
@Composable
fun ThemeSettingsView(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentGlassColor = viewModel.activeTransparencyColor

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Visual & Theme Configuration", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))

        // Notifications Toggle Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Toggle Office Notifications", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text("Receive real-time alerts about event duties", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
            Switch(
                checked = viewModel.notificationsEnabled,
                onCheckedChange = { viewModel.notificationsEnabled = it }
            )
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.12f), modifier = Modifier.padding(vertical = 12.dp))

        // Transparency Color pickers (12 colors)
        Text("Select Transparency Color Accent (12 Colors)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(SettingsPresets.transparencyColors) { preset ->
                val isSelected = currentGlassColor.name == preset.name
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(45.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(preset.color.copy(1f))
                        .border(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.activeTransparencyColor = preset }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Change Wallpapers presets options
        Text("Select 3D Online Live Theme Wallpapers", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow {
            items(SettingsPresets.wallpaperPresets) { wall ->
                val isSelected = viewModel.activeWallpaper == wall
                Card(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .width(110.dp)
                        .clickable { viewModel.activeWallpaper = wall },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(wall.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(wall.desc.take(40), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), lineHeight = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWorkerStatsCard(
    viewModel: MainViewModel,
    totalWorkers: Int,
    totalHods: Int,
    totalEmps: Int,
    totalCanteens: Int
) {
    val glassColor = viewModel.activeTransparencyColor.color
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = RoundedCornerShape(20.dp)
                clip = true
            }
            .border(
                width = 1.dp,
                color = glassColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(glassColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Workers Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Worker Statistics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$totalWorkers",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "Active workforce members",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // horizontal comparative segmented bar
            val total = (totalHods + totalEmps + totalCanteens).toFloat().coerceAtLeast(1f)
            val hodPct = totalHods / total
            val empPct = totalEmps / total
            val canteenPct = totalCanteens / total

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            ) {
                if (totalHods > 0) {
                    Box(modifier = Modifier.fillMaxHeight().weight(hodPct).background(Color(0xFFE5A93B)))
                }
                if (totalEmps > 0) {
                    Box(modifier = Modifier.fillMaxHeight().weight(empPct).background(Color(0xFF00C9A7)))
                }
                if (totalCanteens > 0) {
                    Box(modifier = Modifier.fillMaxHeight().weight(canteenPct).background(Color(0xFF1F8EFC)))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE5A93B)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HODs ($totalHods)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF00C9A7)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Staffs ($totalEmps)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1F8EFC)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kitchen ($totalCanteens)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                }
            }
        }
    }
}

@Composable
fun AdminSalesOverviewCard(
    viewModel: MainViewModel,
    teaCount: Int,
    coffeeCount: Int,
    unknownSales: Int
) {
    val glassColor = viewModel.activeTransparencyColor.color
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = RoundedCornerShape(20.dp)
                clip = true
            }
            .border(
                width = 1.dp,
                color = glassColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(glassColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalCafe,
                        contentDescription = "Sales Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Sales & Canteen Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tea Issued", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
                    Text("$teaCount Cups", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C9A7))
                }
                Column {
                    Text("Coffee Issued", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
                    Text("$coffeeCount Cups", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
                }
                Column {
                    Text("Guest Orders", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
                    Text("$unknownSales Cups", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val primaryColor = MaterialTheme.colorScheme.primary
            // Comparative Canvas chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(0.04f), RoundedCornerShape(8.dp))
            ) {
                val paddingLeft = 16.dp.toPx()
                val availableWidth = size.width - paddingLeft * 2
                val barWidth = 28.dp.toPx()
                val spacing = (availableWidth - (3 * barWidth)) / 2
                val maxCount = (teaCount.coerceAtLeast(coffeeCount).coerceAtLeast(unknownSales).coerceAtLeast(1)).toFloat()
                
                // Pillar 1: Tea
                val teaHeight = (teaCount.toFloat() / maxCount) * size.height * 0.8f
                drawRoundRect(
                    color = Color(0xFF00C9A7),
                    topLeft = androidx.compose.ui.geometry.Offset(paddingLeft, size.height - teaHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, teaHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
                
                // Pillar 2: Coffee
                val coffeeHeight = (coffeeCount.toFloat() / maxCount) * size.height * 0.8f
                drawRoundRect(
                    color = Color(0xFFE67E22),
                    topLeft = androidx.compose.ui.geometry.Offset(paddingLeft + barWidth + spacing, size.height - coffeeHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, coffeeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
                
                // Pillar 3: Guests
                val guestHeight = (unknownSales.toFloat() / maxCount) * size.height * 0.8f
                drawRoundRect(
                    color = primaryColor,
                    topLeft = androidx.compose.ui.geometry.Offset(paddingLeft + (barWidth + spacing) * 2, size.height - guestHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, guestHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("Tea", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.Bold)
                Text("Coffee", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.Bold)
                Text("Guests", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminLeaveSummaryCard(
    viewModel: MainViewModel,
    leaveList: List<LeaveRequest>,
    leavesCount: Int,
    approvedCount: Int,
    pendingCount: Int,
    rejectedCount: Int
) {
    val glassColor = viewModel.activeTransparencyColor.color
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = RoundedCornerShape(20.dp)
                clip = true
            }
            .border(
                width = 1.dp,
                color = glassColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(glassColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Leaves Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Leave Clearance Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    StatMiniBadge(label = "Applied", value = "$leavesCount", color = MaterialTheme.colorScheme.primary)
                }
                Box(modifier = Modifier.weight(1f)) {
                    StatMiniBadge(label = "Approved", value = "$approvedCount", color = Color(0xFF00C9A7))
                }
                Box(modifier = Modifier.weight(1f)) {
                    StatMiniBadge(label = "Pending", value = "$pendingCount", color = Color(0xFFE67E22))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Urgent Action Leaves", 
                fontWeight = FontWeight.Bold, 
                fontSize = 12.sp, 
                color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            val pendingLeaves = leaveList.filter { it.status.contains("Pending", ignoreCase = true) }.take(2)
            
            if (pendingLeaves.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "All clear! Zero pending leaves.", 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                pendingLeaves.forEach { leave ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(leave.employeeName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                    Text("Dates: ${leave.startDate} to ${leave.endDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                }
                                Text(
                                    "PENDING",
                                    color = Color(0xFFE67E22),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier
                                        .background(Color(0xFFE67E22).copy(0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            
                            Text(
                                "Reason: \"${leave.reason}\"", 
                                fontSize = 11.sp, 
                                color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.processLeaveByAdmin(leave.id, false, "Declined by Admin") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.1f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Decline", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { viewModel.processLeaveByAdmin(leave.id, true, null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Approve", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
