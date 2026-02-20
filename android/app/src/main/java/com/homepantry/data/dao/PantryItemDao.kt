package com.homepantry.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.homepantry.data.entity.PantryItem
import kotlinx.coroutines.flow.Flow

/**
 * 食材库存数据访问对象
 */
@Dao
interface PantryItemDao {

    /**
     * 获取过期日期在指定日期之前的食材
     */
    @Query("""
        SELECT * FROM pantry_items
        WHERE expiry_date IS NOT NULL
        AND expiry_date <= :expirationDate
        ORDER BY expiry_date ASC
    """)
    fun getItemsExpiringBefore(expirationDate: Long): Flow<List<PantryItem>>

    /**
     * 获取过期日期在指定日期之前的食材（一次性）
     */
    @Query("""
        SELECT * FROM pantry_items
        WHERE expiry_date IS NOT NULL
        AND expiry_date <= :expirationDate
        ORDER BY expiry_date ASC
    """)
    suspend fun getItemsExpiringBeforeOnce(expirationDate: Long): List<PantryItem>

    /**
     * 获取食材的过期日期
     */
    @Query("SELECT expiry_date FROM pantry_items WHERE id = :pantryItemId")
    suspend fun getExpirationDate(pantryItemId: String): Long?

    /**
     * 获取所有有保质期的食材
     */
    @Query("SELECT * FROM pantry_items WHERE expiry_date IS NOT NULL")
    fun getAllItemsWithExpiry(): Flow<List<PantryItem>>

    /**
     * 获取所有有保质期的食材（一次性）
     */
    @Query("SELECT * FROM pantry_items WHERE expiry_date IS NOT NULL")
    suspend fun getAllItemsWithExpiryOnce(): List<PantryItem>
}
