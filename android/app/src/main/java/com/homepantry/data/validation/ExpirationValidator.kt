package com.homepantry.data.validation

/**
 * 过期验证器
 */
object ExpirationValidator {

    private const val MIN_REMINDER_DAYS = 1
    private const val MAX_REMINDER_DAYS = 30
    private const val TIME_PATTERN = Regex("^([01]?[0-9]|2[0-3]):([0-5][0-9])$")

    /**
     * 验证提前提醒天数
     */
    fun validateReminderDays(days: Int): ValidationResult {
        return when {
            days < MIN_REMINDER_DAYS -> ValidationResult.Error("提前提醒天数不能小于 $MIN_REMINDER_DAYS 天")
            days > MAX_REMINDER_DAYS -> ValidationResult.Error("提前提醒天数不能超过 $MAX_REMINDER_DAYS 天")
            else -> ValidationResult.Success
        }
    }

    /**
     * 验证提醒时间
     */
    fun validateReminderTime(time: String): ValidationResult {
        return if (time.matches(TIME_PATTERN)) {
            val (hour, minute) = time.split(":").map { it.toInt() }
            if (hour < 0 || hour > 23) {
                ValidationResult.Error("小时必须在 0-23 之间")
            } else if (minute < 0 || minute > 59) {
                ValidationResult.Error("分钟必须在 0-59 之间")
            } else {
                ValidationResult.Success
            }
        } else {
            ValidationResult.Error("时间格式不正确，应为 HH:mm")
        }
    }

    /**
     * 验证提醒频率
     */
    fun validateReminderFrequency(frequency: com.homepantry.data.entity.ExpirationReminder.ReminderFrequency): ValidationResult {
        return ValidationResult.Success
    }

    /**
     * 验证提醒配置
     */
    fun validateConfig(config: com.homepantry.data.entity.ReminderConfig): ValidationResult {
        return validateReminderDays(config.reminderDays)
            .flatMap {
                validateReminderTime(config.reminderTime)
            }
            .flatMap {
                validateReminderFrequency(config.reminderFrequency)
            }
    }
}
