package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.homepantry.data.constants.Constants

/**
 * 过期提醒实体
 */
@Entity(tableName = "expiration_reminders",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = PantryItem::class,
            parentColumns = ["id"],
            childColumns = ["pantryItemId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class ExpirationReminder(
    @PrimaryKey val id: String,
    val pantryItemId: String,
    val reminderDays: Int = Constants.Days.DEFAULT_REMINDER_DAYS,
    val reminderTime: String = Constants.Times.DEFAULT_REMINDER_TIME,
    val reminderFrequency: ReminderFrequency = ReminderFrequency.DAILY,
    val isEnabled: Boolean = true,
    val lastNotifiedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 转换为 ReminderConfig
         */
        fun toConfig(reminder: ExpirationReminder): ReminderConfig {
            return ReminderConfig(
                reminderDays = reminder.reminderDays,
                reminderTime = reminder.reminderTime,
                reminderFrequency = reminder.reminderFrequency,
                notificationEnabled = reminder.isEnabled
            )
        }
    }
}

/**
 * 提醒频率
 */
enum class ReminderFrequency {
    DAILY, WEEKLY, MONTHLY
}

/**
 * 提醒配置
 */
data class ReminderConfig(
    val reminderDays: Int = Constants.Days.DEFAULT_REMINDER_DAYS,
    val reminderTime: String = Constants.Times.DEFAULT_REMINDER_TIME,
    val reminderFrequency: ReminderFrequency = ReminderFrequency.DAILY,
    val notificationEnabled: Boolean = true
)
