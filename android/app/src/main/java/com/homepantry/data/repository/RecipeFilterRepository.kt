package com.homepantry.data.repository

import androidx.room.Transaction
import android.util.Log
import com.homepantry.data.dao.RecipeFilterDao
import com.homepantry.data.entity.RecipeFilter
import com.homepantry.data.entity.RecipeFilterCriteria
import com.homepantry.data.validation.RecipeFilterValidator
import com.homepantry.data.validation.ValidationResult
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
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
        return PerformanceMonitor.recordMethodPerformance("createFilter") {
            Logger.enter("RecipeFilterRepository.createFilter")

            return try {
                // 验证筛选条件
                RecipeFilterValidator.validateCriteria(criteria)
                    .onSuccess {
                        val filter = RecipeFilter(
                            id = "filter_${java.util.UUID.randomUUID().toString()}",
                            cookingTimeMin = criteria.cookingTimeMin,
                            cookingTimeMax = criteria.cookingTimeMax,
                            difficultyMin = criteria.difficultyMin,
                            difficultyMax = criteria.difficultyMax,
                            includedIngredients = criteria.includedIngredients.toList(),
                            excludedIngredients = criteria.excludedIngredients.toList(),
                            categoryIds = criteria.categoryIds.toList()
                        )
                        recipeFilterDao.insert(filter)
                        Logger.d(TAG, "创建筛选器成功：${filter.id}")
                        Logger.exit("RecipeFilterRepository.createFilter", filter)
                        Result.success(filter)
                    }
                    .onFailure { e ->
                        Logger.e(TAG, "筛选条件验证失败：${e.message}")
                        Result.failure(e)
                    }
            } catch (e: java.lang.IllegalArgumentException) {
                Logger.e(TAG, "参数验证失败", e)
                Logger.exit("RecipeFilterRepository.createFilter")
                Result.failure(java.lang.Exception("参数验证失败：${e.message}", e))
            } catch (e: Exception) {
                Logger.e(TAG, "创建筛选器失败", e)
                Logger.exit("RecipeFilterRepository.createFilter")
                Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
            }
        }
    }

    /**
     * 更新筛选器
     */
    @Transaction
    suspend fun updateFilter(filter: RecipeFilter): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("updateFilter") {
            Logger.enter("RecipeFilterRepository.updateFilter", filter.id)

            return try {
                recipeFilterDao.update(filter.copy(updatedAt = System.currentTimeMillis()))
                Logger.d(TAG, "更新筛选器成功：${filter.id}")
                Logger.exit("RecipeFilterRepository.updateFilter")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "更新筛选器失败：${filter.id}", e)
                Logger.exit("RecipeFilterRepository.updateFilter")
                Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
            }
        }
    }

    /**
     * 删除筛选器
     */
    @Transaction
    suspend fun deleteFilter(filterId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("deleteFilter") {
            Logger.enter("RecipeFilterRepository.deleteFilter", filterId)

            return try {
                recipeFilterDao.deleteById(filterId)
                Logger.d(TAG, "删除筛选器成功：$filterId")
                Logger.exit("RecipeFilterRepository.deleteFilter")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "删除筛选器失败：$filterId", e)
                Logger.exit("RecipeFilterRepository.deleteFilter")
                Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
            }
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
        return PerformanceMonitor.recordMethodPerformance("clearAllFilters") {
            Logger.enter("RecipeFilterRepository.clearAllFilters")

            return try {
                recipeFilterDao.deleteAll()
                Logger.d(TAG, "清除所有筛选器成功")
                Logger.exit("RecipeFilterRepository.clearAllFilters")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "清除所有筛选器失败", e)
                Logger.exit("RecipeFilterRepository.clearAllFilters")
                Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
            }
        }
    }
}
