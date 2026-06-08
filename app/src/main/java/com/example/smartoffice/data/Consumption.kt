package com.example.smartoffice.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "consumptions")
data class Consumption(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val type: String, // "Tea" or "Coffee"
    val date: String, // YYYY-MM-DD
    val timestamp: Long
)

@Dao
interface ConsumptionDao {
    @Query("SELECT * FROM consumptions WHERE employeeId = :employeeId")
    fun getConsumptionsForEmployee(employeeId: Int): Flow<List<Consumption>>

    @Insert
    suspend fun insert(consumption: Consumption)

    @Query("SELECT * FROM consumptions WHERE date = :date")
    fun getConsumptionsForDate(date: String): Flow<List<Consumption>>
}
