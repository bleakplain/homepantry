package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户偏好设置
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: String = "default",

    // === 口味偏好 ===
    val preferredCuisines: String = "[]",           // JSON: 喜欢的菜系
    val spiceLevel: SpiceLevel = SpiceLevel.MEDIUM, // 辣度偏好
    val flavorPreferences: String = "[]",          // JSON: 口味偏好
    val dislikedIngredients: String = "[]",       // JSON: 不喜欢的食材

    // === 饮食限制 ===
    val dietaryRestrictions: String = "[]",        // JSON: 饮食限制 (素食/清真等)
    val allergies: String = "[]",                 // JSON: 过敏原
    val healthGoal: HealthGoal? = null,            // 健康目标

    // === 时间偏好 ===
    val weekdayCookingTime: Int? = null,           // 工作日做饭时间 (分钟)
    val weekendCookingTime: Int? = null,           // 周末做饭时间 (分钟)

    // === 通知设置 ===
    val dailyRecommendationEnabled: Boolean = true,   // 每日推荐提醒
    val dailyRecommendationTime: String = "20:00",    // 推荐时间
    val mealPlanReminderEnabled: Boolean = false,     // 菜单提醒
    val shoppingReminderEnabled: Boolean = false,      // 购物提醒
    val shoppingReminderDay: Int = 5,                 // 购物提醒日 (周五)
    val expiryReminderEnabled: Boolean = true,         // 食材到期提醒
    val expiryReminderDays: Int = 3,                  // 提前几天提醒

    // === 其他设置 ===
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val theme: AppTheme = AppTheme.LIGHT,
    val language: String = "zh-CN",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 辣度等级
 */
enum class SpiceLevel {
    NONE,       // 不吃辣
    MILD,       // 微辣
    MEDIUM,     // 中辣
    HOT,        // 重辣
    EXTREME     // 特辣
}

/**
 * 健康目标
 */
enum class HealthGoal {
    WEIGHT_LOSS,       // 减肥
    MUSCLE_GAIN,       // 增肌
    MAINTENANCE,       // 保持
    HEALTHY_EATING     // 健康饮食
}

/**
 * 单位制
 */
enum class UnitSystem {
    METRIC,     // 公制 (克、毫升)
    IMPERIAL    // 英制 (盎司、杯)
}

/**
 * 主题
 */
enum class AppTheme {
    LIGHT,      // 浅色
    DARK,       // 深色
    AUTO        // 跟随系统
}
