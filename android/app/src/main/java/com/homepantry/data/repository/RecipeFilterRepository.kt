package com.homepantry.data.repository

import android.util.Log
import androidx.room.Transaction
import com.homepantry.data.dao.RecipeFilterDao
import com.homepantry.data.entity.RecipeFilter
import com.homepantry.data.entity.RecipeFilterCriteria
import com.homepantry.data.validation.RecipeFilterValidator
import com.homepantry.data.validation.ValidationResult
import kotlinx.coroutines.flow.Flow

/**
 * 菜谱筛选仓库
 */
class RecipeFilterRepository(private val recipeFilterDao: RecipeFilterDao) {

    companion object {
        private const val TAG = "RecipeFilterRepository"
    }

    /**
     * 创建筛选器
     */
    @Transaction
    suspend fun createFilter(criteria: RecipeFilterCriteria): Result<RecipeFilter> {
        return try {
            // 验证筛选条件
            RecipeFilterValidator.validateCriteria(criteria)
                .onSuccess {
                    val filter = RecipeFilter(
                        id = java.util.UUID.randomUUID().toString(),
                        cookingTimeMin = criteria.cookingTimeMin,
                        cookingTimeMax = criteria.cookingTimeMax,
                        difficultyMin = criteria.difficultyMin,
                        difficultyMax = criteria.difficultyMax,
                        includedIngredients = criteria.includedIngredients.toList(),
                        excludedIngredients = criteria.excludedIngredients.toList(),
                        categoryIds = criteria.categoryIds.toList()
                    )
                    recipeFilterDao.insert(filter)
                    Log.d(TAG, "创建筛选器成功：${filter.id}")
                    Result.success(filter)
                }
                .onFailure { e ->
                    Log.e(TAG, "筛选条件验证失败：${e.message}")
                    Result.failure(e)
                }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "参数验证失败", e)
            Result.failure(java.lang.Exception("参数验证失败：${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "创建筛选器失败", e)
            Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
        }
    }

    /**
     * 更新筛选器
     */
    @Transaction
    suspend fun updateFilter(filter: RecipeFilter): Result<Unit> {
        return try {
            recipeFilterDao.update(filter.copy(updatedAt = System.currentTimeMillis()))
            Log.d(TAG, "更新筛选器成功：${filter.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "更新筛选器失败：${filter.id}", e)
            Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
        }
    }

    /**
     * 删除筛选器
     */
    @Transaction
    suspend fun deleteFilter(filterId: String): Result<Unit> {
        return try {
            recipeFilterDao.deleteById(filterId)
            Log.d(TAG, "删除筛选器成功：$filterId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "删除筛选器失败：$filterId", e)
            Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
        }
    }

    /**
     * 获取所有筛选器
     */
    fun getAllFilters(): Flow<List<RecipeFilter>> {
        return recipeFilterDao.getAllFilters()
    }

    /**
     * 获取最新的筛选器
     */
    suspend fun getLatestFilter(): RecipeFilter? {
        return recipeFilterDao.getLatestFilter()
    }

    /**
     * 应用筛选条件
     */
    fun applyFilter(criteria: RecipeFilterCriteria): Flow<List<Recipe>> {
        return recipeFilterDao.filterRecipes(
            cookingTimeMin = criteria.cookingTimeMin,
            cookingTimeMax = criteria.cookingTimeMax,
            difficultyMin = criteria.difficultyMin,
            difficultyMax = criteria.difficultyMax,
            categoryIds = criteria.categoryIds.toList(),
            includedIngredients = criteria.includedIngredients.toList(),
            excludedIngredients = criteria.excludedIngredients.toList()
        )
    }

    /**
     * 清除所有筛选器
     */
    @Transaction
    suspend fun clearAllFilters(): Result<Unit> {
        return try {
            recipeFilterDao.deleteAll()
            Log.d(TAG, "清除所有筛选器成功")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "清除所有筛选器失败", e)
            Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
        }
    }
}
