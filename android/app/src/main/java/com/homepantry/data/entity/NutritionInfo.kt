package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 营养信息 - 菜谱的营养成分
 */
@Entity(
    tableName = "nutrition_info",
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
data class NutritionInfo(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String,
    val calories: Int? = null,          // 卡路里 (每份)
    val protein: Double? = null,        // 蛋白质 (g)
    val fat: Double? = null,            // 脂肪 (g)
    val carbs: Double? = null,          // 碳水化合物 (g)
    val fiber: Double? = null,          // 纤维 (g)
    val sugar: Double? = null,          // 糖 (g)
    val sodium: Int? = null,            // 钠 (mg)
    val servingSize: Int = 1            // 份量基数
)
