package com.homepantry.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.homepantry.data.entity.ExpirationNotification
import kotlinx.coroutines.flow.Flow

/**
 * 过期通知数据访问对象
 */
@Dao
interface ExpirationNotificationDao {

    /**
     * 插入过期通知
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: ExpirationNotification)

    /**
     * 批量插入过期通知
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(notifications: List<ExpirationNotification>)

    /**
     * 更新过期通知
     */
    @Update
    suspend fun update(notification: ExpirationNotification)

    /**
     * 删除过期通知
     */
    @Delete
    suspend fun delete(notification: ExpirationNotification)

    /**
     * 根据 ID 删除过期通知
     */
    @Query("DELETE FROM expiration_notifications WHERE id = :notificationId")
    suspend fun deleteById(notificationId: String)

    /**
     * 根据 pantryItemId 删除过期通知
     */
    @Query("DELETE FROM expiration_notifications WHERE pantry_item_id = :pantryItemId")
    suspend fun deleteByPantryItemId(pantryItemId: String)

    /**
     * 获取所有过期通知（Flow）
     */
    @Query("SELECT * FROM expiration_notifications ORDER BY notification_date DESC, created_at DESC")
    fun getAllNotifications(): Flow<List<ExpirationNotification>>

    /**
     * 获取所有过期通知（一次性）
     */
    @Query("SELECT * FROM expiration_notifications ORDER BY notification_date DESC, created_at DESC")
    suspend fun getAllNotificationsOnce(): List<ExpirationNotification>

    /**
     * 根据 pantryItemId 获取过期通知（Flow）
     */
    @Query("SELECT * FROM expiration_notifications WHERE pantry_item_id = :pantryItemId ORDER BY notification_date DESC")
    fun getNotificationsByPantryItemId(pantryItemId: String): Flow<List<ExpirationNotification>>

    /**
     * 根据 pantryItemId 获取过期通知（一次性）
     */
    @Query("SELECT * FROM expiration_notifications WHERE pantry_item_id = :pantryItemId ORDER BY notification_date DESC")
    suspend fun getNotificationsByPantryItemIdOnce(pantryItemId: String): List<ExpirationNotification>

    /**
     * 获取未读的过期通知
     */
    @Query("SELECT * FROM expiration_notifications WHERE is_read = 0 ORDER BY notification_date DESC, created_at DESC")
    fun getUnreadNotifications(): Flow<List<ExpirationNotification>>

    /**
     * 获取未读的过期通知（一次性）
     */
    @Query("SELECT * FROM expiration_notifications WHERE is_read = 0 ORDER BY notification_date DESC, created_at DESC")
    suspend fun getUnreadNotificationsOnce(): List<ExpirationNotification>

    /**
     * 获取未处理的过期通知
     */
    @Query("SELECT * FROM expiration_notifications WHERE is_handled = 0 ORDER BY notification_date DESC, created_at DESC")
    fun getUnhandledNotifications(): Flow<List<ExpirationNotification>>

    /**
     * 获取未处理的过期通知（一次性）
     */
    @Query("SELECT * FROM expiration_notifications WHERE is_handled = 0 ORDER BY notification_date DESC, created_at DESC")
    suspend fun getUnhandledNotificationsOnce(): List<ExpirationNotification>

    /**
     * 标记为已读
     */
    @Query("UPDATE expiration_notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    /**
     * 批量标记为已读
     */
    @Query("UPDATE expiration_notifications SET is_read = 1 WHERE id IN (:notificationIds)")
    suspend fun markAllAsRead(notificationIds: List<String>)

    /**
     * 标记为已处理
     */
    @Query("UPDATE expiration_notifications SET is_handled = 1 WHERE id = :notificationId")
    suspend fun markAsHandled(notificationId: String)

    /**
     * 批量标记为已处理
     */
    @Query("UPDATE expiration_notifications SET is_handled = 1 WHERE id IN (:notificationIds)")
    suspend fun markAllAsHandled(notificationIds: List<String>)

    /**
     * 清除所有已读和已处理的通知
     */
    @Query("DELETE FROM expiration_notifications WHERE is_read = 1 AND is_handled = 1")
    suspend fun clearAllProcessed()

    /**
     * 统计未读通知数量
     */
    @Query("SELECT COUNT(*) FROM expiration_notifications WHERE is_read = 0")
    suspend fun getUnreadCount(): Int

    /**
     * 统计未处理通知数量
     */
    @Query("SELECT COUNT(*) FROM expiration_notifications WHERE is_handled = 0")
    suspend fun getUnhandledCount(): Int

    /**
     * 统计 pantryItemId 的未处理通知数量
     */
    @Query("SELECT COUNT(*) FROM expiration_notifications WHERE pantry_item_id = :pantryItemId AND is_handled = 0")
    suspend fun getUnhandledCountByPantryItemId(pantryItemId: String): Int

    /**
     * 获取最近的通知
     */
    @Query("SELECT * FROM expiration_notifications ORDER BY notification_date DESC, created_at DESC LIMIT :limit")
    fun getRecentNotifications(limit: Int): Flow<List<ExpirationNotification>>

    /**
     * 获取最近的通知（一次性）
     */
    @Query("SELECT * FROM expiration_notifications ORDER BY notification_date DESC, created_at DESC LIMIT :limit")
    suspend fun getRecentNotificationsOnce(limit: Int): List<ExpirationNotification>
}
