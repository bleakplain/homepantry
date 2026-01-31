package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 菜谱实体
 */
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // === 基本信息 ===
    val name: String,
    val description: String? = null,
    val images: String = "[]",              // JSON: 图片 URL 列表
    val categoryId: String? = null,
    val tags: String = "[]",                // JSON: 标签列表

    // === 烹饪信息 ===
    val prepTime: Int? = null,              // 准备时间 (分钟)
    val cookingTime: Int,                   // 烹饪时间 (分钟)
    val servings: Int,                      // 份数
    val difficulty: DifficultyLevel,

    // === 统计信息 ===
    val cookingCount: Int = 0,              // 制作次数
    val averageRating: Float = 0f,          // 平均评分
    val lastCookedAt: Long? = null,         // 最后制作时间

    // === 元数据 ===
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String? = null,          // 创建者 (家庭共享)
    val isFavorite: Boolean = false,
    val favoritePosition: Int? = null,      // 收藏排序
    val isPublic: Boolean = false           // 是否公开 (家庭共享)
)

/**
 * 难度等级
 */
enum class DifficultyLevel {
    EASY,       // 简单
    MEDIUM,     // 中等
    HARD        // 困难
}
