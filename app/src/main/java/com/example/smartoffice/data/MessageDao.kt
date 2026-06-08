package com.example.smartoffice.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE channel = :channel ORDER BY timestamp ASC")
    fun getMessagesForChannel(channel: String): Flow<List<Message>>

    @Insert
    suspend fun insert(message: Message)
}
