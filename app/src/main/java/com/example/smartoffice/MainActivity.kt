package com.example.smartoffice

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.animation.core.*
import androidx.room.Room
import com.example.smartoffice.data.AppDatabase
import com.example.smartoffice.data.CouponDao
import com.example.smartoffice.data.DutyDao
import com.example.smartoffice.data.EmployeeDao
import com.example.smartoffice.data.MessageDao
import com.example.smartoffice.data.ScanDao
import com.example.smartoffice.data.SettingsDao
import com.example.smartoffice.data.UserSettings
import androidx.compose.ui.res.stringResource
import com.example.smartoffice.R
import com.example.smartoffice.data.*
import com.example.smartoffice.ui.*
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.OutputStream
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

enum class Screen { LOGIN, OTP, DASHBOARD }

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var settingsDao: SettingsDao
    private lateinit var messageDao: MessageDao
    private lateinit var dutyDao: DutyDao
    private lateinit var scanDao: ScanDao
    private lateinit var employeeDao: EmployeeDao
    private lateinit var couponDao: CouponDao
    private lateinit var consumptionDao: ConsumptionDao
    private lateinit var leaveRequestDao: LeaveRequestDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "smart-office-db")
            .fallbackToDestructiveMigration()
            .build()
        messageDao = database.messageDao()
        settingsDao = database.settingsDao()
        dutyDao = database.dutyDao()
        scanDao = database.scanDao()
        employeeDao = database.employeeDao()
        couponDao = database.couponDao()
        consumptionDao = database.consumptionDao()
        leaveRequestDao = database.leaveRequestDao()
        
        CoroutineScope(Dispatchers.Main).launch {
            val existing = employeeDao.getAllEmployees().first()
            if (existing.isEmpty()) {
                employeeDao.insert(Employee(name="admin", password="password", role="Admin", mobile="", email="", leaveStatus="Active", dailyConsumption=0))
            }
            if (!existing.any { it.name == "0000" }) {
                employeeDao.insert(Employee(name="0000", password="Bangalore club", role="Employee", mobile="", email="", leaveStatus="Active", dailyConsumption=0))
            }
        }
        
        setContent {
            val settings by settingsDao.getSettings().collectAsState(initial = UserSettings())
            
            // Locale handling
            val context = LocalContext.current
            LaunchedEffect(settings?.language) {
                val lang = settings?.language ?: "en"
                val locale = java.util.Locale(lang)
                java.util.Locale.setDefault(locale)
                val config = context.resources.configuration
                config.setLocale(locale)
                context.createConfigurationContext(config)
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                // Forcing activity recreation for static resources if needed, 
                // but let's try just updating config first for Compose.
            }

            val colorScheme = if (settings?.isDarkMode == true) darkColorScheme() else lightColorScheme()
            
            MaterialTheme(colorScheme = colorScheme) {
                var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        }
                    ) { screen ->
                        when (screen) {
                            Screen.LOGIN -> LoginScreen(employeeDao, settingsDao, onLoginSuccess = { currentScreen = Screen.OTP })
                            Screen.OTP -> OTPScreen(onOtpVerified = { currentScreen = Screen.DASHBOARD })
                            Screen.DASHBOARD -> DashboardScreen(messageDao, settingsDao, dutyDao, scanDao, employeeDao, couponDao, consumptionDao, leaveRequestDao)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(employeeDao: EmployeeDao, settingsDao: SettingsDao, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoaded = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background elements for "3D" feel
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-200).dp)
                .graphicsLayer {
                    rotationZ = 45f
                    alpha = 0.3f
                }
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(40.dp))
        )

        val alpha by animateFloatAsState(
            targetValue = if (isLoaded) 1f else 0f,
            animationSpec = tween(durationMillis = 1000),
            label = "alpha"
        )
        val translateY by animateDpAsState(
            targetValue = if (isLoaded) 0.dp else 100.dp,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "translateY"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .graphicsLayer {
                    this.alpha = alpha
                    this.translationY = translateY.toPx()
                    rotationX = 5f // Subtle 3D tilt
                },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sign in to your smart office",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.username)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "HIDE" else "SHOW",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val employees = employeeDao.getAllEmployees().first()
                            val employee = employees.find { it.name == email && it.password == password }
                            if (employee != null) {
                                val settings = settingsDao.getSettings().first() ?: UserSettings()
                                settingsDao.updateSettings(settings.copy(currentUserId = employee.id))
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, "Invalid login", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(messageDao: MessageDao, settingsDao: SettingsDao, dutyDao: DutyDao, scanDao: ScanDao, employeeDao: EmployeeDao, couponDao: CouponDao, consumptionDao: ConsumptionDao, leaveRequestDao: LeaveRequestDao) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Admin", "HOD", "Employee", "Canteen", "Data", "Profile")
    val scope = rememberCoroutineScope()
    
    val settings by settingsDao.getSettings().collectAsState(initial = UserSettings())
    val employees by employeeDao.getAllEmployees().collectAsState(initial = emptyList())
    val currentUser = employees.find { it.id == settings?.currentUserId }
    
    var showPasswordDialog by remember { mutableStateOf(false) }
    LaunchedEffect(currentUser) {
        if (currentUser != null && currentUser.password == "dummy123") {
            showPasswordDialog = true
        }
    }
    
    if (showPasswordDialog && currentUser != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("First Login") },
            text = { ChangePasswordScreen(currentUser, employeeDao) },
            confirmButton = { }
        )
    }

    // Backup Prompt
    var showBackupDialog by remember { mutableStateOf(false) }
    LaunchedEffect(settings) {
        if (settings != null && System.currentTimeMillis() - (settings?.lastBackupTimestamp ?: 0L) > 7 * 24 * 60 * 60 * 1000L) {
            showBackupDialog = true
        }
    }
    
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Backup Data") },
            text = { Text("It's been a week! Please perform a data backup.") },
            confirmButton = {
                TextButton(onClick = {
                    showBackupDialog = false
                }) { Text("Done") }
            }
        )
    }
    LaunchedEffect(Unit) {
        while (true) {
            // Mock refresh logic: Reset daily coupons for all active employees
            kotlinx.coroutines.delay(24 * 60 * 60 * 1000L) // Wait a day
        }
    }
    
    // Duty Notification Banner
    val currentTime = remember { System.currentTimeMillis() }
    val upcomingDuties by dutyDao.getUpcomingDuties(currentTime).collectAsState(initial = emptyList())
    
    // Connection Status
    // Simplified online/offline (mocked)
    val isOnline = true 
    
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
            
            // Floating Language Selector in Header
            var langExpanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { langExpanded = true }) {
                    Text("🌐 Lang")
                }
                DropdownMenu(expanded = langExpanded, onDismissRequest = { langExpanded = false }) {
                    listOf("en", "hi", "ta").forEach { code ->
                        DropdownMenuItem(
                            text = { Text(if (code == "en") "English" else if (code == "hi") "Hindi" else "Tamil") },
                            onClick = {
                                langExpanded = false
                                scope.launch {
                                    settingsDao.updateSettings((settings ?: UserSettings()).copy(language = code))
                                }
                            }
                        )
                    }
                }
            }
            Text(if (isOnline) "🟢 Online" else "🔴 Offline")
        }

        if (upcomingDuties.isNotEmpty()) {
            Banner(text = "Upcoming: ${upcomingDuties.first().title}")
        }
        
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            }
        ) { tab ->
            when (tab) {
                0 -> AdminDashboardScreen(employeeDao, settingsDao, couponDao, leaveRequestDao)
                1 -> HODDashboardScreen(employeeDao)
                2 -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) { 
                    ChartsScreen(consumptionDao)
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    CalendarScreen(dutyDao, employeeDao)
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    LeaveManagementScreen(leaveRequestDao, currentUser)
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    ChatScreen(messageDao, employeeDao) 
                }
                3 -> CanteenDashboardScreen(scanDao, employeeDao, consumptionDao, currentUser)
                4 -> Column { ThemeSettingsScreen(settingsDao); Divider(); DataManagementScreen(settingsDao) }
                5 -> if (currentUser != null) ProfileScreen(currentUser, employeeDao)
            }
        }
    }
}

@Composable
fun Banner(text: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Text(text = text, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
// Keep QRCodeScreen and generateAndSavePdf as they were

@Composable
fun QRCodeScreen(employeeName: String, employeeId: String) {
    val context = LocalContext.current
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val content = "ID: $employeeId, Name: $employeeName"

    LaunchedEffect(content) {
        val encoder = BarcodeEncoder()
        qrBitmap = encoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 500, 500)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Employee ID: $employeeId", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Name: $employeeName", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        qrBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Employee QR Code",
                modifier = Modifier.size(256.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                generateAndSavePdf(context, employeeName, employeeId, it)
            }) {
                Text("Download as PDF")
            }
        }
    }
}

fun generateAndSavePdf(context: android.content.Context, name: String, id: String, qrBitmap: Bitmap) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(600, 800, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.textSize = 24f
    canvas.drawText("Employee ID: $id", 50f, 50f, paint)
    canvas.drawText("Name: $name", 50f, 90f, paint)
    
    val scaledQr = Bitmap.createScaledBitmap(qrBitmap, 400, 400, false)
    canvas.drawBitmap(scaledQr, 100f, 150f, paint)
    
    document.finishPage(page)

    val filename = "ID_Card_${id}.pdf"
    
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

    uri?.let {
        try {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            document.writeTo(outputStream)
            document.close()
            outputStream?.close()
            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
