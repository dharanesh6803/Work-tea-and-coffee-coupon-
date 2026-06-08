package com.example.smartoffice.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DutyDao {
    @Query("SELECT * FROM duties WHERE timestamp - :currentTime < 86400000 AND timestamp > :currentTime ORDER BY timestamp ASC")
    fun getUpcomingDuties(currentTime: Long): Flow<List<Duty>>

    @Query("SELECT * FROM duties ORDER BY date ASC")
    fun getAllDuties(): Flow<List<Duty>>

    @Insert
    suspend fun insert(duty: Duty)

    @androidx.room.Delete
    suspend fun delete(duty: Duty)
}
