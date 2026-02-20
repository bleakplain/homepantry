package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.homepantry.data.constants.Constants

/**
 * 过期通知实体
 */
@Entity(tableName = "expiration_notifications",
    indices = [
        androidx.room.Index(value = ["pantryItemId", "notificationDate"]),
        androidx.room.Index(value = ["isRead", "isHandled"])
    ]
)
data class ExpirationNotification(
    @PrimaryKey val id: String,
    val pantryItemId: String,
    val notificationDate: Long,
    val notificationType: NotificationType = NotificationType.EXPIRED,
    val isRead: Boolean = false,
    val isHandled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class NotificationType {
        EXPIRED,       // 已过期
        EXPIRING_SOON, // 即将过期
        BULK_CHECK     // 批量检查
    }

    companion object {
        /**
         * 创建已过期通知
         */
        fun createExpired(pantryItemId: String): ExpirationNotification {
            return ExpirationNotification(
                id = "notif_expired_${java.util.UUID.randomUUID()}",
                pantryItemId = pantryItemId,
                notificationDate = System.currentTimeMillis(),
                notificationType = NotificationType.EXPIRED
            )
        }

        /**
         * 创建即将过期通知
         */
        fun createExpiringSoon(
            pantryItemId: String,
            days: Int
        ): ExpirationNotification {
            return ExpirationNotification(
                id = "notif_expiring_soon_${java.util.UUID.randomUUID()}",
                pantryItemId = pantryItemId,
                notificationDate = System.currentTimeMillis(),
                notificationType = NotificationType.EXPIRING_SOON
            )
        }

        /**
         * 创建批量检查通知
         */
        fun createBulkCheck(
            pantryItemIds: List<String>
        ): ExpirationNotification {
            return ExpirationNotification(
                id = "notif_bulk_check_${java.util.UUID.randomUUID()}",
                pantryItemId = pantryItemIds.first(),
                notificationDate = System.currentTimeMillis(),
                notificationType = NotificationType.BULK_CHECK
            )
        }
    }
}
