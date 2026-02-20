package com.homepantry.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.repository.ExpirationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * 过期检查 Worker
 */
@HiltWorker
class ExpirationCheckWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ExpirationCheckWorker"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val database = HomePantryDatabase.getDatabase(context)
                val expirationRepo = ExpirationRepository(
                    pantryItemDao = database.pantryItemDao(),
                    expirationReminderDao = database.expirationReminderDao(),
                    expirationNotificationDao = database.expirationNotificationDao()
                )

                // 获取所有启用的过期提醒
                val reminders = expirationRepo.getEnabledRemindersOnce()

                Log.d(TAG, "开始过期检查，找到 ${reminders.size} 个启用的提醒")

                // 对每个提醒进行过期检查
                reminders.forEach { reminder ->
                    if (reminder.isEnabled) {
                        checkAndNotify(reminder, expirationRepo)
                    }
                }

                Log.d(TAG, "过期检查完成")
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "过期检查失败", e)
                Result.failure(e)
            }
        }
    }

    /**
     * 检查并发送通知
     */
    private suspend fun checkAndNotify(
        reminder: com.homepantry.data.entity.ExpirationReminder,
        expirationRepo: ExpirationRepository
    ) {
        val today = System.currentTimeMillis()
        val msPerDay = 24 * 60 * 60 * 1000L
        val expirationDate = today - (reminder.reminderDays * msPerDay)

        // 检查是否需要发送通知
        val shouldNotify = shouldNotify(reminder, expirationDate, today)

        if (shouldNotify) {
            val results = expirationRepo.checkExpiringItems(reminder.reminderDays)

            // 根据过期状态发送通知
            results.forEach { result ->
                when (result.status) {
                    com.homepantry.data.entity.ExpirationStatus.EXPIRED -> {
                        expirationRepo.sendExpirationNotification(result.pantryItem)
                        Log.d(TAG, "发送过期通知：${result.pantryItem.name}")
                    }
                    com.homepantry.data.entity.ExpirationStatus.EXPIRING_SOON -> {
                        expirationRepo.sendExpiringSoonNotification(result.pantryItem, reminder.reminderDays)
                        Log.d(TAG, "发送即将过期通知：${result.pantryItem.name}")
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 判断是否需要发送通知
     */
    private fun shouldNotify(
        reminder: com.homepantry.data.entity.ExpirationReminder,
        expirationDate: Long,
        today: Long
    ): Boolean {
        // 检查是否在今天之前
        if (expirationDate > today) {
            return false
        }

        // 检查是否已经在今天通知过
        val yesterday = today - (24 * 60 * 60 * 1000L)
        if (reminder.lastNotifiedDate != null &&
            reminder.lastNotifiedDate!! >= yesterday) {
            return false
        }

        return true
    }

    /**
     * 根据频率判断是否应该执行检查
     */
    private fun shouldExecuteCheck(
        reminder: com.homepantry.data.entity.ExpirationReminder,
        today: Long
    ): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = today

        return when (reminder.reminderFrequency) {
            com.homepantry.data.entity.ExpirationReminder.ReminderFrequency.DAILY -> true
            com.homepantry.data.entity.ExpirationReminder.ReminderFrequency.WEEKLY -> {
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                dayOfWeek == Calendar.MONDAY // 每周一检查
            }
            com.homepantry.data.entity.ExpirationReminder.ReminderFrequency.MONTHLY -> {
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                dayOfMonth == 1 // 每月 1 号检查
            }
        }
    }
}
