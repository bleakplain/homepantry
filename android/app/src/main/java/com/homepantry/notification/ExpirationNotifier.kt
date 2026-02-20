package com.homepantry.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.homepantry.R
import com.homepantry.data.entity.PantryItem
import com.homepantry.data.entity.ExpirationStatus

/**
 * 过期通知发送器
 */
class ExpirationNotifier(private val context: Context) {

    companion object {
        private const val TAG = "ExpirationNotifier"

        // 通知渠道
        const val CHANNEL_ID = "expiration_reminder"
        const val CHANNEL_ID_EXPIRED = "expiration_expired"
        const val CHANNEL_ID_EXPIRING_SOON = "expiration_expiring_soon"

        // 通知名称
        const val CHANNEL_NAME = "过期提醒"
        const val CHANNEL_NAME_EXPIRED = "已过期"
        const val CHANNEL_NAME_EXPIRING_SOON = "即将过期"

        // 通知描述
        const val CHANNEL_DESCRIPTION = "食材过期提醒通知"
        const val CHANNEL_DESCRIPTION_EXPIRED = "食材已过期通知"
        const val CHANNEL_DESCRIPTION_EXPIRING_SOON = "食材即将过期通知"
    }

    init {
        createNotificationChannels()
    }

    /**
     * 创建所有通知渠道
     */
    private fun createNotificationChannels() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            createExpirationReminderChannel(),
            createExpirationExpiredChannel(),
            createExpirationExpiringSoonChannel()
        )

        channels.forEach { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建过期提醒渠道
     */
    private fun createExpirationReminderChannel(): NotificationChannel {
        return NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableLights(true)
            enableVibration(true)
            setSound(getDefaultSoundUri())
        }
    }

    /**
     * 创建已过期渠道
     */
    private fun createExpirationExpiredChannel(): NotificationChannel {
        return NotificationChannel(
            CHANNEL_ID_EXPIRED,
            CHANNEL_NAME_EXPIRED,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION_EXPIRED
            enableLights(true)
            enableVibration(true)
            setSound(getDefaultSoundUri())
        }
    }

    /**
     * 创建即将过期渠道
     */
    private fun createExpirationExpiringSoonChannel(): NotificationChannel {
        return NotificationChannel(
            CHANNEL_ID_EXPIRING_SOON,
            CHANNEL_NAME_EXPIRING_SOON,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION_EXPIRING_SOON
            enableLights(true)
            enableVibration(true)
            setSound(getDefaultSoundUri())
        }
    }

    /**
     * 发送过期通知
     */
    fun sendExpirationNotification(pantryItem: PantryItem, status: ExpirationStatus): NotificationCompat.Builder {
        val channelId = when (status) {
            ExpirationStatus.EXPIRED, ExpirationStatus.EXPIRED_TODAY -> CHANNEL_ID_EXPIRED
            else -> CHANNEL_ID_EXPIRING_SOON
        }

        return ExpirationNotificationBuilder.buildNotification(
            context = context,
            channelId = channelId,
            title = getTitle(status),
            content = getContent(pantryItem, status),
            pantryItem = pantryItem,
            isGrouped = true
        )
    }

    /**
     * 发送批量过期通知
     */
    fun sendBatchExpirationNotification(results: List<com.homepantry.data.entity.ExpirationCheckResult>): NotificationCompat.Builder {
        if (results.isEmpty()) {
            throw IllegalArgumentException("结果列表不能为空")
        }

        val expiredCount = results.count { it.status == ExpirationStatus.EXPIRED || it.status == ExpirationStatus.EXPIRED_TODAY }
        val expiringSoonCount = results.count { it.status == ExpirationStatus.EXPIRING_SOON }

        if (expiredCount == 0 && expiringSoonCount == 0) {
            throw IllegalArgumentException("没有过期或即将过期的食材")
        }

        return ExpirationNotificationBuilder.buildBatchNotification(
            context = context,
            channelId = CHANNEL_ID,
            title = "食材过期提醒",
            content = "已过期：$expiredCount 个，即将过期：$expiringSoonCount 个",
            results = results,
            isGrouped = true
        )
    }

    /**
     * 发送即将过期通知
     */
    fun sendExpiringSoonNotification(
        checkResult: com.homepantry.data.entity.ExpirationCheckResult,
        days: Int
    ): NotificationCompat.Builder {
        return sendExpirationNotification(checkResult.pantryItem, ExpirationStatus.EXPIRING_SOON)
    }

    /**
     * 清除所有通知
     */
    fun clearAllNotifications() {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
    }

    /**
     * 取消指定通知
     */
    fun cancelNotification(notificationId: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }

    /**
     * 获取通知标题
     */
    private fun getTitle(status: ExpirationStatus): String {
        return when (status) {
            ExpirationStatus.EXPIRED -> "食材已过期"
            ExpirationStatus.EXPIRED_TODAY -> "食材今天过期"
            ExpirationStatus.EXPIRING_SOON -> "食材即将过期"
            else -> "食材新鲜"
        }
    }

    /**
     * 获取通知内容
     */
    private fun getContent(pantryItem: PantryItem, status: ExpirationStatus): String {
        val statusText = when (status) {
            ExpirationStatus.EXPIRED -> "已过期"
            ExpirationStatus.EXPIRED_TODAY -> "今天过期"
            ExpirationStatus.EXPIRING_SOON -> "还有 ${getDaysUntilExpiration(pantryItem)} 天过期"
            else -> "新鲜"
        }

        return "${pantryItem.name}（${pantryItem.quantity} ${pantryItem.unit}）$statusText"
    }

    /**
     * 获取距离过期的天数
     */
    private fun getDaysUntilExpiration(pantryItem: PantryItem): Int {
        val today = System.currentTimeMillis()
        val msPerDay = 24 * 60 * 60 * 1000L
        return ((pantryItem.expiryDate - today) / msPerDay).toInt()
    }

    /**
     * 获取默认通知声音
     */
    private fun getDefaultSoundUri(): Uri? {
        return try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            soundUri
        } catch (e: Exception) {
            null
        }
    }
}
