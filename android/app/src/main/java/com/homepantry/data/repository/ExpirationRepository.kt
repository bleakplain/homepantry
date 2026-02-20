package com.homepantry.data.repository

import android.util.Log
import androidx.room.Transaction
import com.homepantry.data.dao.ExpirationReminderDao
import com.homepantry.data.dao.ExpirationNotificationDao
import com.homepantry.data.dao.PantryItemDao
import com.homepantry.data.entity.PantryItem
import com.homepantry.data.entity.ExpirationReminder
import com.homepantry.data.entity.ExpirationNotification
import com.homepantry.data.entity.ExpirationCheckResult
import com.homepantry.data.entity.ExpirationStatus
import com.homepantry.data.entity.ReminderConfig
import com.homepantry.data.entity.ExpirationSummary
import com.homepantry.data.validation.ExpirationValidator
import com.homepantry.data.validation.ValidationResult
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 过期仓库
 */
class ExpirationRepository(
    private val pantryItemDao: PantryItemDao,
    private val expirationReminderDao: ExpirationReminderDao,
    private val expirationNotificationDao: ExpirationNotificationDao
) {

    companion object {
        private const val TAG = "ExpirationRepository"
    }

    /**
     * 创建过期提醒
     */
    @Transaction
    suspend fun createReminder(
        pantryItemId: String,
        config: ReminderConfig
    ): Result<ExpirationReminder> {
        return try {
            // 验证提醒配置
            val validationResult = ExpirationValidator.validateConfig(config)
            if (validationResult is ValidationResult.Error) {
                Log.e(TAG, "提醒配置验证失败：${validationResult.message}")
                return Result.failure(java.lang.Exception(validationResult.message))
            }

            val reminder = ExpirationReminder(
                id = "rem_${UUID.randomUUID().toString()}",
                pantryItemId = pantryItemId,
                reminderDays = config.reminderDays,
                reminderTime = config.reminderTime,
                reminderFrequency = config.reminderFrequency,
                isEnabled = config.notificationEnabled
            )
            expirationReminderDao.insert(reminder)
            Log.d(TAG, "创建过期提醒成功：${reminder.id}")
            Result.success(reminder)
        } catch (e: Exception) {
            Log.e(TAG, "创建过期提醒失败", e)
            Result.failure(e)
        }
    }

    /**
     * 更新过期提醒
     */
    @Transaction
    suspend fun updateReminder(reminder: ExpirationReminder): Result<Unit> {
        return try {
            expirationReminderDao.update(reminder.copy(updatedAt = System.currentTimeMillis()))
            Log.d(TAG, "更新过期提醒成功：${reminder.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "更新过期提醒失败：${reminder.id}", e)
            Result.failure(e)
        }
    }

    /**
     * 删除过期提醒
     */
    @Transaction
    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            expirationReminderDao.deleteById(reminderId)
            Log.d(TAG, "删除过期提醒成功：$reminderId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "删除过期提醒失败：$reminderId", e)
            Result.failure(e)
        }
    }

    /**
     * 获取所有过期提醒
     */
    fun getAllReminders(): Flow<List<ExpirationReminder>> {
        return expirationReminderDao.getAllReminders()
    }

    /**
     * 获取启用的过期提醒
     */
    fun getEnabledReminders(): Flow<List<ExpirationReminder>> {
        return expirationReminderDao.getEnabledReminders()
    }

    /**
     * 获取最新过期提醒
     */
    suspend fun getLatestReminder(): ExpirationReminder? {
        return expirationReminderDao.getLatestReminder()
    }

    /**
     * 检查过期食材
     */
    fun checkExpiringItems(reminderDays: Int): Flow<List<ExpirationCheckResult>> {
        val today = System.currentTimeMillis()
        val msPerDay = 24 * 60 * 60 * 1000L
        val expirationDate = today - (reminderDays * msPerDay)

        return pantryItemDao.getItemsExpiringBefore(expirationDate)
            .map { item ->
                val daysUntilExpiration = ((item.expiryDate - today) / msPerDay).toInt()
                val status = when {
                    item.expiryDate < today -> ExpirationStatus.EXPIRED
                    item.expiryDate == today -> ExpirationStatus.EXPIRED_TODAY
                    item.expiryDate < expirationDate -> ExpirationStatus.EXPIRING_SOON
                    else -> ExpirationStatus.FRESH
                }
                ExpirationCheckResult(
                    pantryItem = item,
                    expirationDate = item.expiryDate,
                    daysUntilExpiration = daysUntilExpiration,
                    status = status
                )
            }
    }

    /**
     * 发送过期通知
     */
    @Transaction
    suspend fun sendExpirationNotification(pantryItem: PantryItem, status: ExpirationStatus): Result<ExpirationNotification> {
        return try {
            val notification = ExpirationNotification(
                id = "notif_${UUID.randomUUID().toString()}",
                pantryItemId = pantryItem.id,
                notificationDate = System.currentTimeMillis(),
                notificationType = when (status) {
                    ExpirationStatus.EXPIRED, ExpirationStatus.EXPIRED_TODAY -> ExpirationNotification.NotificationType.EXPIRED
                    else -> ExpirationNotification.NotificationType.EXPIRING_SOON
                }
            )
            expirationNotificationDao.insert(notification)
            Log.d(TAG, "发送过期通知成功：${pantryItem.name}")
            Result.success(notification)
        } catch (e: Exception) {
            Log.e(TAG, "发送过期通知失败：${pantryItem.name}", e)
            Result.failure(e)
        }
    }

    /**
     * 发送即将过期通知
     */
    @Transaction
    suspend fun sendExpiringSoonNotification(
        pantryItem: PantryItem,
        days: Int
    ): Result<ExpirationNotification> {
        return sendExpirationNotification(pantryItem, ExpirationStatus.EXPIRING_SOON)
    }

    /**
     * 保存通知记录
     */
    @Transaction
    suspend fun saveNotification(notification: ExpirationNotification): Result<Unit> {
        return try {
            expirationNotificationDao.insert(notification)
            Log.d(TAG, "保存通知记录成功：${notification.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "保存通知记录失败：${notification.id}", e)
            Result.failure(e)
        }
    }

    /**
     * 获取通知历史
     */
    fun getNotificationHistory(): Flow<List<ExpirationNotification>> {
        return expirationNotificationDao.getAllNotifications()
    }

    /**
     * 标记通知为已读
     */
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return try {
            expirationNotificationDao.markAsRead(notificationId)
            Log.d(TAG, "标记通知为已读：$notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "标记通知为已读失败：$notificationId", e)
            Result.failure(e)
        }
    }

    /**
     * 标记通知为已处理
     */
    suspend fun markNotificationAsHandled(notificationId: String): Result<Unit> {
        return try {
            expirationNotificationDao.markAsHandled(notificationId)
            Log.d(TAG, "标记通知为已处理：$notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "标记通知为已处理失败：$notificationId", e)
            Result.failure(e)
        }
    }

    /**
     * 获取未读通知
     */
    fun getUnreadNotifications(): Flow<List<ExpirationNotification>> {
        return expirationNotificationDao.getUnreadNotifications()
    }

    /**
     * 获取未处理通知
     */
    fun getUnhandledNotifications(): Flow<List<ExpirationNotification>> {
        return expirationNotificationDao.getUnhandledNotifications()
    }

    /**
     * 清除已处理通知
     */
    @Transaction
    suspend fun clearProcessedNotifications(): Result<Int> {
        return try {
            val count = expirationNotificationDao.clearAllProcessed()
            Log.d(TAG, "清除已处理通知成功：$count 个")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "清除已处理通知失败", e)
            Result.failure(e)
        }
    }

    /**
     * 保存默认提醒配置
     */
    suspend fun saveDefaultReminderConfig(pantryItemId: String): Result<ExpirationReminder> {
        val config = ReminderConfig()
        return createReminder(pantryItemId, config)
    }
}
