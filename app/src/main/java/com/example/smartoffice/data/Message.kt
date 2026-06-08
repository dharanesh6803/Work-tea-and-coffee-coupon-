package com.example.smartoffice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val content: String,
    val channel: String, // 'group' or 'private'
    val timestamp: Long
)
