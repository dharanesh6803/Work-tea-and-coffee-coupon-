package com.example.smartoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartoffice.data.Message
import com.example.smartoffice.data.MessageDao
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import com.example.smartoffice.data.EmployeeDao
import com.example.smartoffice.data.Employee
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(messageDao: MessageDao, employeeDao: EmployeeDao) {
    var channel by remember { mutableStateOf("group") }
    val baseMessages by messageDao.getMessagesForChannel(channel).collectAsState(initial = emptyList())
    val employees by employeeDao.getAllEmployees().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    
    // Simulate presence for demo purposes
    LaunchedEffect(Unit) {
        while(true) {
            delay(10000)
            val all = employees
            if (all.isNotEmpty()) {
                val randomEmp = all.random()
                employeeDao.update(randomEmp.copy(isOnline = !randomEmp.isOnline))
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        TabRow(
            selectedTabIndex = if (channel == "group") 0 else 1,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = channel == "group", onClick = { channel = "group" }, text = { Text("Group Chat") })
            Tab(selected = channel == "private", onClick = { channel = "private" }, text = { Text("Private") })
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true // Latest messages at bottom
        ) {
            items(baseMessages.reversed()) { message ->
                val senderEmp = employees.find { it.name == message.sender }
                ChatBubble(message = message, isOnline = senderEmp?.isOnline ?: false)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            scope.launch {
                                messageDao.insert(Message(sender = "Me", content = text, channel = channel, timestamp = System.currentTimeMillis()))
                                text = ""
                            }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, isOnline: Boolean) {
    val isMe = message.sender == "Me"
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            UserAvatar(name = message.sender, isOnline = isOnline)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = alignment) {
            if (!isMe) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Surface(
                color = bubbleColor,
                shape = shape,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = message.content,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    )
                }
            }
        }
        
        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar(name = "Me", isOnline = true) // Assume self is always online
        }
    }
}

@Composable
fun UserAvatar(name: String, isOnline: Boolean) {
    val initials = if (name.length >= 2) name.take(2).uppercase() else name.uppercase()
    val colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF009688))
    val backgroundColor = remember(name) { colors[name.hashCode().absoluteValue % colors.size] }

    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
            )
        }
        
        // Status indicator
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(1.dp)
                .clip(CircleShape)
                .background(if (isOnline) Color.Green else Color.Gray)
        )
    }
}

private val Int.absoluteValue: Int
    get() = if (this < 0) -this else this

