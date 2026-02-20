package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.RecipeFolder
import kotlinx.coroutines.flow.Flow

/**
 * 菜谱收藏夹关联数据访问对象
 */
@Dao
interface RecipeFolderDao {
    /**
     * 插入关联
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recipeFolder: RecipeFolder)

    /**
     * 批量插入关联
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(recipeFolders: List<RecipeFolder>)

    /**
     * 删除关联
     */
    @Delete
    suspend fun delete(recipeFolder: RecipeFolder)

    /**
     * 根据 recipeId 和 folderId 删除关联
     */
    @Query("DELETE FROM recipe_folders WHERE recipe_id = :recipeId AND folder_id = :folderId")
    suspend fun delete(recipeId: String, folderId: String)

    /**
     * 根据 folderId 删除所有关联
     */
    @Query("DELETE FROM recipe_folders WHERE folder_id = :folderId")
    suspend fun deleteByFolderId(folderId: String)

    /**
     * 根据 recipeId 删除所有关联
     */
    @Query("DELETE FROM recipe_folders WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)

    /**
     * 检查关联是否存在
     */
    @Query("SELECT COUNT(*) FROM recipe_folders WHERE recipe_id = :recipeId AND folder_id = :folderId")
    suspend fun exists(recipeId: String, folderId: String): Int

    /**
     * 获取收藏夹中的菜谱 ID 列表
     */
    @Query("SELECT recipe_id FROM recipe_folders WHERE folder_id = :folderId ORDER BY added_at DESC")
    suspend fun getRecipeIdsByFolderId(folderId: String): List<String>

    /**
     * 获取收藏夹中的菜谱 ID 列表（Flow）
     */
    @Query("SELECT recipe_id FROM recipe_folders WHERE folder_id = :folderId ORDER BY added_at DESC")
    fun getRecipeIdsByFolderIdFlow(folderId: String): Flow<List<String>>

    /**
     * 获取菜谱所属的收藏夹 ID 列表
     */
    @Query("SELECT folder_id FROM recipe_folders WHERE recipe_id = :recipeId ORDER BY added_at DESC")
    suspend fun getFolderIdsByRecipeId(recipeId: String): List<String>

    /**
     * 获取菜谱所属的收藏夹 ID 列表（Flow）
     */
    @Query("SELECT folder_id FROM recipe_folders WHERE recipe_id = :recipeId ORDER BY added_at DESC")
    fun getFolderIdsByRecipeIdFlow(recipeId: String): Flow<List<String>>
}
