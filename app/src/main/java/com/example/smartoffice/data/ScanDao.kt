package com.example.smartoffice.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans ORDER BY timestamp DESC LIMIT 20")
    fun getRecentScans(): Flow<List<Scan>>

    @Insert
    suspend fun insert(scan: Scan)
}
