package com.homepantry.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.homepantry.data.dao.RecipeFolderDao
import com.homepantry.data.entity.RecipeFolder
import kotlinx.coroutines.flow.Flow

/**
 * 菜谱收藏夹关联仓库
 */
class RecipeFolderRepository(private val recipeFolderDao: RecipeFolderDao) {

    /**
     * 收藏菜谱到收藏夹
     */
    suspend fun addToFolder(
        recipeId: String,
        folderId: String
    ): Result<RecipeFolder> {
        return try {
            val recipeFolder = RecipeFolder(
                id = java.util.UUID.randomUUID().toString(),
                recipeId = recipeId,
                folderId = folderId
            )
            recipeFolderDao.insert(recipeFolder)
            Result.success(recipeFolder)
        } catch (e: SQLiteConstraintException) {
            // 已存在
            Result.failure(Exception("菜谱已在此收藏夹中"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 批量收藏菜谱到收藏夹
     */
    suspend fun batchAddToFolder(
        recipeIds: List<String>,
        folderId: String
    ): Result<Int> {
        return try {
            var count = 0
            recipeIds.forEach { recipeId ->
                val recipeFolder = RecipeFolder(
                    id = java.util.UUID.randomUUID().toString(),
                    recipeId = recipeId,
                    folderId = folderId
                )
                try {
                    recipeFolderDao.insert(recipeFolder)
                    count++
                } catch (e: SQLiteConstraintException) {
                    // 跳过已存在的
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从收藏夹移除菜谱
     */
    suspend fun removeFromFolder(
        recipeId: String,
        folderId: String
    ): Result<Unit> {
        return try {
            recipeFolderDao.delete(recipeId, folderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 检查菜谱是否在收藏夹中
     */
    suspend fun isRecipeInFolder(
        recipeId: String,
        folderId: String
    ): Boolean {
        return recipeFolderDao.exists(recipeId, folderId) > 0
    }

    /**
     * 获取收藏夹中的菜谱 ID 列表
     */
    fun getRecipeIdsByFolderId(folderId: String): Flow<List<String>> {
        return recipeFolderDao.getRecipeIdsByFolderIdFlow(folderId)
    }

    /**
     * 获取菜谱所属的收藏夹 ID 列表
     */
    fun getFolderIdsByRecipeId(recipeId: String): Flow<List<String>> {
        return recipeFolderDao.getFolderIdsByRecipeIdFlow(recipeId)
    }
}
