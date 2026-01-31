package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 菜谱制作步骤
 */
@Entity(
    tableName = "recipe_instructions",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class RecipeInstruction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: String,
    val stepNumber: Int,
    val instruction: String,                       // 步骤描述
    val imageUrl: String? = null,                  // 步骤配图
    val duration: Int? = null,                     // 此步耗时 (秒)
    val temperature: Int? = null,                  // 温度 (如烤箱温度)
    val isKeyStep: Boolean = false,                // 是否关键步骤
    val reminder: String? = null                   // 提醒内容 (如"注意火候")
)
