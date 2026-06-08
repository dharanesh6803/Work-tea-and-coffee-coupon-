package com.example.smartoffice.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val date: String, // YYYY-MM-DD
    val type: String, // Morning/Afternoon
    val isUsed: Boolean = false
)

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons WHERE employeeId = :employeeId AND date = :date")
    fun getCouponsForEmployee(employeeId: Int, date: String): Flow<List<Coupon>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: Coupon)
    
    @Update
    suspend fun updateCoupon(coupon: Coupon)
}
