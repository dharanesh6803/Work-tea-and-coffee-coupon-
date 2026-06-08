package com.example.smartoffice.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = false,
    val accentColor: Int = 0xFF6200EE.toInt(), // Default Purple
    val profileImageUri: String? = null,
    val lastBackupTimestamp: Long = 0L,
    val inactivityTimeoutMinutes: Int = 10,
    val masterPasswordHint: String = "No hint set",
    val currentUserId: Int? = null,
    val language: String = "en"
)

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: UserSettings)
}

@Database(entities = [Message::class, UserSettings::class, Duty::class, Scan::class, Employee::class, Coupon::class, Consumption::class, LeaveRequest::class], version = 14, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun settingsDao(): SettingsDao
    abstract fun dutyDao(): DutyDao
    abstract fun scanDao(): ScanDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun couponDao(): CouponDao
    abstract fun consumptionDao(): ConsumptionDao
    abstract fun leaveRequestDao(): LeaveRequestDao
}
