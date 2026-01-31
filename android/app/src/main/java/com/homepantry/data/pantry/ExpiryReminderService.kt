package com.homepantry.data.pantry

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.homepantry.R
import com.homepantry.data.dao.PantryItemDao
import com.homepantry.data.dao.UserProfileDao
import com.homepantry.data.entity.PantryItem
import com.homepantry.data.entity.StorageLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * 食材到期提醒服务
 */
class ExpiryReminderService(
    private val context: Context,
    private val pantryItemDao: PantryItemDao,
    private val userProfileDao: UserProfileDao
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val CHANNEL_ID = "pantry_expiry"
        private const val NOTIFICATION_ID_BASE = 1000

        // 预警颜色
        const val DAYS_CRITICAL = 1      // 红色：1天内到期
        const val DAYS_WARNING = 3       // 橙色：3天内到期
        const val DAYS_INFO = 7          // 黄色：7天内到期
    }

    init {
        createNotificationChannel()
    }

    /**
     * 启动定期检查
     */
    fun startPeriodicCheck() {
        serviceScope.launch {
            while (true) {
                checkAndNotify()
                // 每天检查一次
                delay(TimeUnit.DAYS.toMillis(1))
            }
        }
    }

    /**
     * 检查并通知
     */
    suspend fun checkAndNotify() {
        // 获取用户设置
        val profile = userProfileDao.getProfileById("default")
        val reminderDays = profile?.expiryReminderDays ?: 3
        val isEnabled = profile?.expiryReminderEnabled ?: true

        if (!isEnabled) return

        // 获取所有食材
        val pantryItems = pantryItemDao.getAllPantryItems().first()

        // 分类即将到期的食材
        val (critical, warning, info) = categorizeByExpiry(pantryItems, reminderDays)

        // 发送通知
        if (critical.isNotEmpty()) {
            sendExpiryNotification(critical, "critical", reminderDays)
        }
        if (warning.isNotEmpty() && reminderDays >= DAYS_WARNING) {
            sendExpiryNotification(warning, "warning", reminderDays)
        }
        if (info.isNotEmpty() && reminderDays >= DAYS_INFO) {
            sendExpiryNotification(info, "info", reminderDays)
        }
    }

    /**
     * 按到期时间分类
     */
    private fun categorizeByExpiry(
        items: List<PantryItem>,
        reminderDays: Int
    ): Triple<List<ExpiringItem>, List<ExpiringItem>, List<ExpiringItem>> {
        val now = System.currentTimeMillis()
        val dayInMillis = TimeUnit.DAYS.toMillis(1)

        val critical = mutableListOf<ExpiringItem>()
        val warning = mutableListOf<ExpiringItem>()
        val info = mutableListOf<ExpiringItem>()

        items.forEach { item ->
            val expiryDate = item.expiryDate ?: return@forEach
            val daysUntilExpiry = ((expiryDate - now) / dayInMillis).toInt()

            if (daysUntilExpiry <= reminderDays) {
                val expiringItem = ExpiringItem(
                    pantryItem = item,
                    daysUntilExpiry = daysUntilExpiry
                )

                when {
                    daysUntilExpiry <= DAYS_CRITICAL -> critical.add(expiringItem)
                    daysUntilExpiry <= DAYS_WARNING -> warning.add(expiringItem)
                    else -> info.add(expiringItem)
                }
            }
        }

        return Triple(critical, warning, info)
    }

    /**
     * 发送到期通知
     */
    private fun sendExpiryNotification(
        items: List<ExpiringItem>,
        priority: String,
        reminderDays: Int
    ) {
        if (items.isEmpty()) return

        val title = when (priority) {
            "critical" -> "⚠️ 食材即将到期！"
            "warning" -> "🔔 食材快过期了"
            else -> "📅 食材到期提醒"
        }

        val content = buildNotificationContent(items)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(
                when (priority) {
                    "critical" -> NotificationCompat.PRIORITY_HIGH
                    "warning" -> NotificationCompat.PRIORITY_DEFAULT
                    else -> NotificationCompat.PRIORITY_LOW
                }
            )
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_BASE + priority.hashCode(),
            notification
        )
    }

    /**
     * 构建通知内容
     */
    private fun buildNotificationContent(items: List<ExpiringItem>): String {
        return buildString {
            appendLine("以下食材即将到期：")
            appendLine()

            items.take(5).forEach { item ->
                append("• ")
                append(item.pantryItem.name)
                append(" - ")

                when {
                    item.daysUntilExpiry == 0 -> append("今天到期")
                    item.daysUntilExpiry == 1 -> append("明天到期")
                    item.daysUntilExpiry < 0 -> append("已过期 ${-item.daysUntilExpiry} 天")
                    else -> append("${item.daysUntilExpiry}天后到期")
                }

                // 显示存储位置
                if (item.pantryItem.storageLocation != StorageLocation.PANTRY) {
                    append(" (")
                    append(
                        when (item.pantryItem.storageLocation) {
                            StorageLocation.FRIDGE -> "冷藏"
                            StorageLocation.FREEZER -> "冷冻"
                            else -> "储藏室"
                        }
                    )
                    append(")")
                }

                appendLine()
            }

            if (items.size > 5) {
                appendLine("... 还有 ${items.size - 5} 项")
            }

            appendLine()
            append("💡 建议尽快使用或处理")
        }
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "食材到期提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "提醒您食材即将到期，减少浪费"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建点击意图
     */
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, ExpiryReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * 即将到期的食材
     */
    data class ExpiringItem(
        val pantryItem: PantryItem,
        val daysUntilExpiry: Int
    )
}

/**
 * 到期提醒详情 Activity（占位）
 */
class ExpiryReminderActivity {
    // 将在 UI 层实现
}
