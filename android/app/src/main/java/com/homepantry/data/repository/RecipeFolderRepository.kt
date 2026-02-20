package com.homepantry.data.repository

import androidx.room.Transaction
import com.homepantry.data.dao.RecipeFolderDao
import com.homepantry.data.entity.RecipeFolder
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.Flow

/**
 * 菜谱收藏夹关联仓库
 */
class RecipeFolderRepository(private val recipeFolderDao: RecipeFolderDao) {

    companion object {
        private const val TAG = "RecipeFolderRepository"
    }

    /**
     * 添加菜谱到收藏夹
     */
    @Transaction
    suspend fun addRecipeToFolder(recipeId: String, folderId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("addRecipeToFolder") {
            Logger.enter("RecipeFolderRepository.addRecipeToFolder", recipeId, folderId)

            return try {
                val recipeFolder = RecipeFolder(
                    id = "recipe_folder_${java.util.UUID.randomUUID().toString()}",
                    recipeId = recipeId,
                    folderId = folderId
                )
                recipeFolderDao.insert(recipeFolder)
                Logger.d(TAG, "添加菜谱到收藏夹成功：$recipeId → $folderId")
                Logger.exit("RecipeFolderRepository.addRecipeToFolder")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "添加菜谱到收藏夹失败：$recipeId → $folderId", e)
                Logger.exit("RecipeFolderRepository.addRecipeToFolder")
                Result.failure(e)
            }
        }
    }

    /**
     * 从收藏夹移除菜谱
     */
    @Transaction
    suspend fun removeRecipeFromFolder(recipeId: String, folderId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("removeRecipeFromFolder") {
            Logger.enter("RecipeFolderRepository.removeRecipeFromFolder", recipeId, folderId)

            return try {
                recipeFolderDao.deleteByRecipeAndFolder(recipeId, folderId)
                Logger.d(TAG, "从收藏夹移除菜谱成功：$recipeId → $folderId")
                Logger.exit("RecipeFolderRepository.removeRecipeFromFolder")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "从收藏夹移除菜谱失败：$recipeId → $folderId", e)
                Logger.exit("RecipeFolderRepository.removeRecipeFromFolder")
                Result.failure(e)
            }
        }
    }

    /**
     * 批量添加菜谱到收藏夹
     */
    @Transaction
    suspend fun addRecipesToFolder(recipeIds: List<String>, folderId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("addRecipesToFolder") {
            Logger.enter("RecipeFolderRepository.addRecipesToFolder", recipeIds.size, folderId)

            return try {
                recipeIds.forEach { recipeId ->
                    val recipeFolder = RecipeFolder(
                        id = "recipe_folder_${java.util.UUID.randomUUID().toString()}",
                        recipeId = recipeId,
                        folderId = folderId
                    )
                    recipeFolderDao.insert(recipeFolder)
                }
                Logger.d(TAG, "批量添加菜谱到收藏夹成功：${recipeIds.size} 个 → $folderId")
                Logger.exit("RecipeFolderRepository.addRecipesToFolder")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "批量添加菜谱到收藏夹失败：${recipeIds.size} 个 → $folderId", e)
                Logger.exit("RecipeFolderRepository.addRecipesToFolder")
                Result.failure(e)
            }
        }
    }

    /**
     * 批量从收藏夹移除菜谱
     */
    @Transaction
    suspend fun removeRecipesFromFolder(recipeIds: List<String>, folderId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("removeRecipesFromFolder") {
            Logger.enter("RecipeFolderRepository.removeRecipesFromFolder", recipeIds.size, folderId)

            return try {
                recipeIds.forEach { recipeId ->
                    recipeFolderDao.deleteByRecipeAndFolder(recipeId, folderId)
                }
                Logger.d(TAG, "批量从收藏夹移除菜谱成功：${recipeIds.size} 个 ← $folderId")
                Logger.exit("RecipeFolderRepository.removeRecipesFromFolder")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "批量从收藏夹移除菜谱失败：${recipeIds.size} 个 ← $folderId", e)
                Logger.exit("RecipeFolderRepository.removeRecipesFromFolder")
                Result.failure(e)
            }
        }
    }

    /**
     * 获取收藏夹中的菜谱
     */
    fun getRecipesInFolder(folderId: String): Flow<List<String>> {
        return recipeFolderDao.getRecipeIdsByFolder(folderId)
    }

    /**
     * 获取菜谱所属的收藏夹
     */
    fun getFoldersForRecipe(recipeId: String): Flow<List<String>> {
        return recipeFolderDao.getFolderIdsByRecipe(recipeId)
    }

    /**
     * 清除所有关联
     */
    @Transaction
    suspend fun clearAll(): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("clearAll") {
            Logger.enter("RecipeFolderRepository.clearAll")

            return try {
                recipeFolderDao.deleteAll()
                Logger.d(TAG, "清除所有关联成功")
                Logger.exit("RecipeFolderRepository.clearAll")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "清除所有关联失败", e)
                Logger.exit("RecipeFolderRepository.clearAll")
                Result.failure(e)
            }
        }
    }
}
