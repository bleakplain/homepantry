package com.homepantry.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeFilter
import kotlinx.coroutines.flow.Flow

/**
 * 菜谱筛选器数据访问对象
 */
@Dao
interface RecipeFilterDao {

    /**
     * 插入筛选器
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(filter: RecipeFilter)

    /**
     * 更新筛选器
     */
    @Update
    suspend fun update(filter: RecipeFilter)

    /**
     * 删除筛选器
     */
    @Delete
    suspend fun delete(filter: RecipeFilter)

    /**
     * 根据 ID 删除筛选器
     */
    @Query("DELETE FROM recipe_filters WHERE id = :filterId")
    suspend fun deleteById(filterId: String)

    /**
     * 获取所有筛选器
     */
    @Query("SELECT * FROM recipe_filters ORDER BY updated_at DESC")
    fun getAllFilters(): Flow<List<RecipeFilter>>

    /**
     * 根据 ID 获取筛选器（一次性）
     */
    @Query("SELECT * FROM recipe_filters WHERE id = :filterId")
    suspend fun getFilterById(filterId: String): RecipeFilter?

    /**
     * 获取最新的筛选器
     */
    @Query("SELECT * FROM recipe_filters ORDER BY updated_at DESC LIMIT 1")
    suspend fun getLatestFilter(): RecipeFilter?

    /**
     * 根据筛选条件筛选菜谱（优化版本）
     */
    @Query("""
        SELECT r.*
        FROM recipes r
        WHERE (:cookingTimeMin IS NULL OR r.cooking_time >= :cookingTimeMin)
        AND (:cookingTimeMax IS NULL OR r.cooking_time <= :cookingTimeMax)
        AND (:difficultyMin IS NULL OR r.difficulty >= :difficultyMin)
        AND (:difficultyMax IS NULL OR r.difficulty <= :difficultyMax)
        AND (:categoryIds IS NULL OR r.category_id IN (:categoryIds))
        AND (
            :includedIngredients IS NULL
            OR EXISTS (
                SELECT 1
                FROM recipe_ingredients ri
                WHERE ri.recipe_id = r.id
                AND ri.ingredient_id IN (:includedIngredients)
            )
        )
        AND (
            :excludedIngredients IS NULL
            OR NOT EXISTS (
                SELECT 1
                FROM recipe_ingredients ri
                WHERE ri.recipe_id = r.id
                AND ri.ingredient_id IN (:excludedIngredients)
            )
        )
        ORDER BY r.created_at DESC
    """)
    fun filterRecipes(
        cookingTimeMin: Int?,
        cookingTimeMax: Int?,
        difficultyMin: DifficultyLevel?,
        difficultyMax: DifficultyLevel?,
        categoryIds: List<String>?,
        includedIngredients: List<String>?,
        excludedIngredients: List<String>?
    ): Flow<List<Recipe>>

    /**
     * 根据筛选条件分页筛选菜谱（优化版本）
     */
    @Query("""
        SELECT r.*
        FROM recipes r
        WHERE (:cookingTimeMin IS NULL OR r.cooking_time >= :cookingTimeMin)
        AND (:cookingTimeMax IS NULL OR r.cooking_time <= :cookingTimeMax)
        AND (:difficultyMin IS NULL OR r.difficulty >= :difficultyMin)
        AND (:difficultyMax IS NULL OR r.difficulty <= :difficultyMax)
        AND (:categoryIds IS NULL OR r.category_id IN (:categoryIds))
        AND (
            :includedIngredients IS NULL
            OR EXISTS (
                SELECT 1
                FROM recipe_ingredients ri
                WHERE ri.recipe_id = r.id
                AND ri.ingredient_id IN (:includedIngredients)
            )
        )
        AND (
            :excludedIngredients IS NULL
            OR NOT EXISTS (
                SELECT 1
                FROM recipe_ingredients ri
                WHERE ri.recipe_id = r.id
                AND ri.ingredient_id IN (:excludedIngredients)
            )
        )
        ORDER BY r.created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun filterRecipesPaged(
        cookingTimeMin: Int?,
        cookingTimeMax: Int?,
        difficultyMin: DifficultyLevel?,
        difficultyMax: DifficultyLevel?,
        categoryIds: List<String>?,
        includedIngredients: List<String>?,
        excludedIngredients: List<String>?,
        limit: Int,
        offset: Int
    ): Flow<List<Recipe>>

    /**
     * 统计筛选结果数量（优化版本）
     */
    @Query("""
        SELECT COUNT(*)
        FROM recipes r
        WHERE (:cookingTimeMin IS NULL OR r.cooking_time >= :cookingTimeMin)
        AND (:cookingTimeMax IS NULL OR r.cooking_time <= :cookingTimeMax)
        AND (:difficultyMin IS NULL OR r.difficulty >= :difficultyMin)
        AND (:difficultyMax IS NULL OR r.difficulty <= :difficultyMax)
        AND (:categoryIds IS NULL OR r.category_id IN (:categoryIds))
        AND (
            :includedIngredients IS NULL
            OR EXISTS (
                SELECT 1
                FROM recipe_ingredients ri
                WHERE ri.recipe_id = r.id
                AND ri.ingredient_id IN (:includedIngredients)
            )
        )
        AND (
            :excludedIngredients IS NULL
            OR NOT EXISTS (
                SELECT 1
                FROM recipe_ingredients ri
                WHERE ri.recipe_id = r.id
                AND ri.ingredient_id IN (:excludedIngredients)
            )
        )
    """)
    suspend fun countFilterResults(
        cookingTimeMin: Int?,
        cookingTimeMax: Int?,
        difficultyMin: DifficultyLevel?,
        difficultyMax: DifficultyLevel?,
        categoryIds: List<String>?,
        includedIngredients: List<String>?,
        excludedIngredients: List<String>?
    ): Int

    /**
     * 删除所有筛选器
     */
    @Query("DELETE FROM recipe_filters")
    suspend fun deleteAll()
}
