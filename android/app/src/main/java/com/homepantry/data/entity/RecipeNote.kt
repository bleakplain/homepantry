package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 菜谱笔记 - 记录每次制作的改良心得
 */
@Entity(
    tableName = "recipe_notes",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId"), Index("cookingDate")]
)
data class RecipeNote(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String,
    val cookingDate: Long = System.currentTimeMillis(),    // 制作日期
    val rating: Int,                                      // 评分 1-5
    val note: String? = null,                             // 笔记内容
    val images: String = "[]",                            // 成品图片 JSON 数组
    val success: Boolean = true,                          // 是否成功
    val modifications: String? = null,                    // 改动记录
    val createdAt: Long = System.currentTimeMillis()
)
