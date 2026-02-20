package com.homepantry.data.constants

/**
 * 应用常量类
 */
object Constants {

    /**
     * 颜色常量
     */
    object Colors {
        const val DEFAULT_FOLDER = "#FFD700" // 金色
        const val EXPIRED = "#F44336" // 红色
        const val EXPIRING_SOON = "#FF9800" // 橙色
        const val FRESH = "#4CAF50" // 绿色
        const val PRIMARY = "#2196F3" // 蓝色
        const val SECONDARY = "#FF9800" // 橙色
        const val TERTIARY = "#9E9E9E" // 灰色
    }

    /**
     * 时间常量
     */
    object Times {
        const val DEFAULT_REMINDER_TIME = "08:00"
        const val MORNING = "06:00"
        const val AFTERNOON = "12:00"
        const val EVENING = "18:00"
        const val MIDNIGHT = "00:00"

        // 时间格式
        const val TIME_FORMAT_24H = "HH:mm"
        const val TIME_FORMAT_12H = "hh:mm a"
    }

    /**
     * 天数常量
     */
    object Days {
        const val DEFAULT_REMINDER_DAYS = 3
        const val MIN_REMINDER_DAYS = 1
        const val MAX_REMINDER_DAYS = 30

        const val EXPIRED_TODAY = 0
        const val EXPIRED_YESTERDAY = -1
        const val EXPIRED_TOWEEK = -7

        const val ONE_DAY = 1
        const val THREE_DAYS = 3
        const val ONE_WEEK = 7
        const val TWO_WEEKS = 14
        const val ONE_MONTH = 30
    }

    /**
     * 烹饪时间常量
     */
    object CookingTimes {
        const val QUICK_MEAL = 15
        const val SHORT = 30
        const val MEDIUM = 60
        const val LONG = 120
        const val VERY_LONG = 180
    }

    /**
     * 难度常量
     */
    object Difficulties {
        const val EASY = com.homepantry.data.entity.DifficultyLevel.EASY.ordinal
        const val MEDIUM = com.homepantry.data.entity.DifficultyLevel.MEDIUM.ordinal
        const val HARD = com.homepantry.data.entity.DifficultyLevel.HARD.ordinal
    }

    /**
     * 筛选常量
     */
    object Filters {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_PAGE = 0

        const val MAX_INCLUDED_INGREDIENTS = 20
        const val MAX_EXCLUDED_INGREDIENTS = 20
    }

    /**
     * 字符串常量
     */
    object Strings {
        const val EMPTY = ""
        const val SPACE = " "
        const val COMMA = ", "
        const val DASH = " - "
        const val COLON = ": "
        const val PARENTHESES = "(${String.EMPTY})"
    }

    /**
     * 数据库常量
     */
    object Database {
        const val DATABASE_NAME = "homepantry"
        const val DATABASE_VERSION = 19

        // 表名
        const val TABLE_FOLDERS = "folders"
        const val TABLE_RECIPE_FOLDERS = "recipe_folders"
        const val TABLE_RECIPE_FILTERS = "recipe_filters"
        const val TABLE_EXPIRATION_REMINDERS = "expiration_reminders"
        const val TABLE_EXPIRATION_NOTIFICATIONS = "expiration_notifications"

        // 索引
        const val INDEX_FOLDERS_SORT_ORDER = "index_folders_sort_order"
        const val INDEX_RECIPE_FOLDERS_RECIPE_ID = "index_recipe_folders_recipe_id"
        const val INDEX_RECIPE_FOLDERS_FOLDER_ID = "index_recipe_folders_folder_id"
        const val INDEX_RECIPE_FILTERS_CREATED_AT = "index_recipe_filters_created_at"
        const val INDEX_EXPIRATION_REMINDERS_PANTRY_ITEM_ID = "index_expiration_reminders_pantry_item_id"
        const val INDEX_EXPIRATION_REMINDERS_IS_ENABLED_LAST_NOTIFIED = "index_expiration_reminders_is_enabled_last_notified"
        const val INDEX_EXPIRATION_NOTIFICATIONS_PANTRY_ITEM_NOTIFICATION_DATE = "index_expiration_notifications_pantry_item_notification_date"
        const val INDEX_EXPIRATION_NOTIFICATIONS_IS_READ_IS_HANDLED = "index_expiration_notifications_is_read_is_handled"
    }

    /**
     * UI 常量
     */
    object UI {
        const val DEFAULT_ICON_SIZE = 24
        const val DEFAULT_BADGE_SIZE = 16
        const val DEFAULT_PADDING = 16

        const val MIN_DIALOG_WIDTH = 300
        const val MIN_DIALOG_HEIGHT = 400
        const val MAX_DIALOG_WIDTH = 500
        const val MAX_DIALOG_HEIGHT = 700

        const val DEFAULT_CORNER_RADIUS = 16
        const val DEFAULT_ELEVATION = 8
    }

    /**
     * 验证常量
     */
    object Validation {
        const val MIN_NAME_LENGTH = 2
        const val MAX_NAME_LENGTH = 20
        const val MAX_DESCRIPTION_LENGTH = 200
        const val MAX_NOTE_LENGTH = 500

        const val MIN_QUANTITY = 0.0
        const val MAX_QUANTITY = 1000.0
    }
}
