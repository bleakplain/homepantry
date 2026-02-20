package com.homepantry.data.repository

import androidx.room.Transaction
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.Ingredient
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.constants.Constants
import kotlinx.coroutines.flow.Flow

/**
 * 菜谱仓库
 */
class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao
) {

    companion object {
        private const val TAG = "RecipeRepository"
    }

    /**
     * 创建菜谱
     */
    @Transaction
    suspend fun createRecipe(
        name: String,
        description: String?,
        ingredients: List<com.homepantry.data.entity.RecipeIngredient>
    ): Result<Recipe> {
        return PerformanceMonitor.recordMethodPerformance("createRecipe") {
            Logger.enter("RecipeRepository.createRecipe", name, description, ingredients.size)

            return try {
                val recipe = Recipe(
                    id = "recipe_${java.util.UUID.randomUUID().toString()}",
                    name = name,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                recipeDao.insert(recipe)
                ingredients.forEach { ingredientDao.insert(it) }

                Logger.d(TAG, "创建菜谱成功：${recipe.name}")
                Logger.exit("RecipeRepository.createRecipe", recipe)
                Result.success(recipe)
            } catch (e: Exception) {
                Logger.e(TAG, "创建菜谱失败", e)
                Logger.exit("RecipeRepository.createRecipe")
                Result.failure(e)
            }
        }
    }

    /**
     * 更新菜谱
     */
    @Transaction
    suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("updateRecipe") {
            Logger.enter("RecipeRepository.updateRecipe", recipe.id)

            return try {
                recipeDao.update(recipe.copy(updatedAt = System.currentTimeMillis()))

                Logger.d(TAG, "更新菜谱成功：${recipe.name}")
                Logger.exit("RecipeRepository.updateRecipe")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "更新菜谱失败：${recipe.name}", e)
                Logger.exit("RecipeRepository.updateRecipe")
                Result.failure(e)
            }
        }
    }

    /**
     * 删除菜谱
     */
    @Transaction
    suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("deleteRecipe") {
            Logger.enter("RecipeRepository.deleteRecipe", recipeId)

            return try {
                recipeDao.deleteById(recipeId)

                Logger.d(TAG, "删除菜谱成功：$recipeId")
                Logger.exit("RecipeRepository.deleteRecipe")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "删除菜谱失败：$recipeId", e)
                Logger.exit("RecipeRepository.deleteRecipe")
                Result.failure(e)
            }
        }
    }

    /**
     * 根据搜索词搜索菜谱
     */
    fun searchRecipes(query: String): Flow<List<Recipe>>> {
        return recipeDao.searchRecipes("%${query}%")
    }

    /**
     * 根据 ID 获取菜谱
     */
    fun getRecipeById(recipeId: String): Flow<Recipe?> {
        return recipeDao.getRecipeById(recipeId)
    }

    /**
     * 获取所有菜谱
     */
    fun getAllRecipes(): Flow<List<Recipe>>> {
        return recipeDao.getAllRecipes()
    }

    /**
     * 根据分类 ID 获取菜谱
     */
    fun getRecipesByCategory(categoryId: String): Flow<List<Recipe>>> {
        return recipeDao.getRecipesByCategory(categoryId)
    }

    /**
     * 根据食材 ID 获取菜谱
     */
    fun getRecipesByIngredient(ingredientId: String): Flow<List<Recipe>>> {
        return recipeDao.getRecipesByIngredient(ingredientId)
    }
}
