package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.homepantry.data.constants.Constants

/**
 * 菜谱筛选器实体
 */
@Entity(tableName = "recipe_filters")
data class RecipeFilter(
    @PrimaryKey val id: String,
    val cookingTimeMin: Int? = null,
    val cookingTimeMax: Int? = null,
    val difficultyMin: DifficultyLevel? = null,
    val difficultyMax: DifficultyLevel? = null,
    val includedIngredients: List<String>? = null,
    val excludedIngredients: List<String>? = null,
    val categoryIds: List<String>? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 转换为 RecipeFilterCriteria
         */
        fun toCriteria(filter: RecipeFilter): RecipeFilterCriteria {
            return RecipeFilterCriteria(
                cookingTimeMin = filter.cookingTimeMin,
                cookingTimeMax = filter.cookingTimeMax,
                difficultyMin = filter.difficultyMin,
                difficultyMax = filter.difficultyMax,
                includedIngredients = filter.includedIngredients?.toSet() ?: emptySet(),
                excludedIngredients = filter.excludedIngredients?.toSet() ?: emptySet(),
                categoryIds = filter.categoryIds?.toSet() ?: emptySet()
            )
        }
    }

    /**
     * 默认筛选条件（快速菜）
     */
    val QUICK_MEAL = RecipeFilter(
        id = "quick_meal",
        cookingTimeMax = Constants.CookingTimes.QUICK_MEAL,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    /**
     * 默认筛选条件（简单）
     */
    val SIMPLE = RecipeFilter(
        id = "simple",
        difficultyMax = DifficultyLevel.MEDIUM,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    /**
     * 默认筛选条件（健康饮食）
     */
    val HEALTHY = RecipeFilter(
        id = "healthy",
        cookingTimeMax = Constants.CookingTimes.MEDIUM,
        difficultyMax = DifficultyLevel.MEDIUM,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * 菜谱筛选条件（运行时）
 */
data class RecipeFilterCriteria(
    val cookingTimeMin: Int? = null,
    val cookingTimeMax: Int? = null,
    val difficultyMin: DifficultyLevel? = null,
    val difficultyMax: DifficultyLevel? = null,
    val includedIngredients: Set<String> = emptySet(),
    val excludedIngredients: Set<String> = emptySet(),
    val categoryIds: Set<String> = emptySet()
) {
    fun isEmpty(): Boolean {
        return cookingTimeMin == null &&
               cookingTimeMax == null &&
               difficultyMin == null &&
               difficultyMax == null &&
               includedIngredients.isEmpty() &&
               excludedIngredients.isEmpty() &&
               categoryIds.isEmpty()
    }
}

/**
 * 菜谱筛选结果
 */
data class RecipeFilterResult(
    val recipes: List<Recipe>,
    val totalCount: Int,
    val appliedCriteria: RecipeFilterCriteria,
    val elapsedTime: Long
)
