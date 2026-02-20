package com.homepantry.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.homepantry.data.entity.ExpirationReminder
import com.homepantry.data.entity.ExpirationNotification
import kotlinx.coroutines.flow.Flow

/**
 * 过期提醒数据访问对象
 */
@Dao
interface ExpirationReminderDao {

    /**
     * 插入过期提醒
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ExpirationReminder)

    /**
     * 更新过期提醒
     */
    @Update
    suspend fun update(reminder: ExpirationReminder)

    /**
     * 删除过期提醒
     */
    @Delete
    suspend fun delete(reminder: ExpirationReminder)

    /**
     * 根据 ID 删除过期提醒
     */
    @Query("DELETE FROM expiration_reminders WHERE id = :reminderId")
    suspend fun deleteById(reminderId: String)

    /**
     * 根据 pantryItemId 删除过期提醒
     */
    @Query("DELETE FROM expiration_reminders WHERE pantry_item_id = :pantryItemId")
    suspend fun deleteByPantryItemId(pantryItemId: String)

    /**
     * 获取所有过期提醒
     */
    @Query("SELECT * FROM expiration_reminders ORDER BY created_at DESC")
    fun getAllReminders(): Flow<List<ExpirationReminder>>

    /**
     * 获取所有过期提醒（一次性）
     */
    @Query("SELECT * FROM expiration_reminders ORDER BY created_at DESC")
    suspend fun getAllRemindersOnce(): List<ExpirationReminder>

    /**
     * 根据 ID 获取过期提醒（一次性）
     */
    @Query("SELECT * FROM expiration_reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: String): ExpirationReminder?

    /**
     * 根据 pantryItemId 获取过期提醒（Flow）
     */
    @Query("SELECT * FROM expiration_reminders WHERE pantry_item_id = :pantryItemId")
    fun getReminderByPantryItemId(pantryItemId: String): Flow<ExpirationReminder?>

    /**
     * 根据 pantryItemId 获取过期提醒（一次性）
     */
    @Query("SELECT * FROM expiration_reminders WHERE pantry_item_id = :pantryItemId")
    suspend fun getReminderByPantryItemIdOnce(pantryItemId: String): ExpirationReminder?

    /**
     * 获取启用的过期提醒
     */
    @Query("SELECT * FROM expiration_reminders WHERE is_enabled = 1 ORDER BY created_at DESC")
    fun getEnabledReminders(): Flow<List<ExpirationReminder>>

    /**
     * 获取最新的过期提醒
     */
    @Query("SELECT * FROM expiration_reminders ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestReminder(): ExpirationReminder?
}
