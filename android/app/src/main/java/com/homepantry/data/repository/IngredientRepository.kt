package com.homepantry.data.repository

import androidx.room.Transaction
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.entity.Ingredient
import kotlinx.coroutines.flow.Flow

/**
 * 食材仓库
 */
class IngredientRepository(private val ingredientDao: IngredientDao) {

    companion object {
        private const val TAG = "IngredientRepository"
    }

    /**
     * 创建食材
     */
    @Transaction
    suspend fun createIngredient(
        name: String,
        unit: String
    ): Result<Ingredient> {
        return PerformanceMonitor.recordMethodPerformance("createIngredient") {
            Logger.enter("createIngredient", name, unit)

            return try {
                val ingredient = Ingredient(
                    id = "ingredient_${java.util.UUID.randomUUID().toString()}",
                    name = name,
                    unit = unit,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                ingredientDao.insert(ingredient)
                Logger.d(TAG, "创建食材成功：${ingredient.name}")
                Logger.exit("createIngredient", ingredient)
                Result.success(ingredient)
            } catch (e: Exception) {
                Logger.e(TAG, "创建食材失败", e)
                Logger.exit("createIngredient")
                Result.failure(e)
            }
        }
    }

    /**
     * 更新食材
     */
    @Transaction
    suspend fun updateIngredient(ingredient: Ingredient): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("updateIngredient") {
            Logger.enter("updateIngredient", ingredient.id)

            return try {
                ingredientDao.update(ingredient.copy(updatedAt = System.currentTimeMillis()))
                Logger.d(TAG, "更新食材成功：${ingredient.name}")
                Logger.exit("updateIngredient")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "更新食材失败：${ingredient.name}", e)
                Logger.exit("updateIngredient")
                Result.failure(e)
            }
        }
    }

    /**
     * 删除食材
     */
    @Transaction
    suspend fun deleteIngredient(ingredientId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("deleteIngredient") {
            Logger.enter("deleteIngredient", ingredientId)

            return try {
                ingredientDao.deleteById(ingredientId)
                Logger.d(TAG, "删除食材成功：$ingredientId")
                Logger.exit("deleteIngredient")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "删除食材失败：$ingredientId", e)
                Logger.exit("deleteIngredient")
                Result.failure(e)
            }
        }
    }

    /**
     * 根据名称搜索食材
     */
    fun searchIngredientsByName(query: String): Flow<List<Ingredient>>> {
        return ingredientDao.searchIngredients("%${query}%")
    }

    /**
     * 获取所有食材
     */
    fun getAllIngredients(): Flow<List<Ingredient>>> {
        return ingredientDao.getAllIngredients()
    }

    /**
     * 根据 ID 获取食材
     */
    fun getIngredientById(ingredientId: String): Flow<Ingredient?> {
        return ingredientDao.getIngredientById(ingredientId)
    }

    /**
     * 获取常用食材
     */
    fun getFrequentlyUsedIngredients(limit: Int = 10): Flow<List<Ingredient>>> {
        return ingredientDao.getFrequentlyUsedIngredients(limit)
    }
}
