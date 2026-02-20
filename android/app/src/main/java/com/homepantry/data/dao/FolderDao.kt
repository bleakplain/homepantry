package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.Folder
import kotlinx.coroutines.flow.Flow

/**
 * 收藏夹数据访问对象
 */
@Dao
interface FolderDao {
    /**
     * 插入收藏夹
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: Folder)

    /**
     * 更新收藏夹
     */
    @Update
    suspend fun update(folder: Folder)

    /**
     * 删除收藏夹
     */
    @Delete
    suspend fun delete(folder: Folder)

    /**
     * 根据 ID 删除收藏夹
     */
    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteById(folderId: String)

    /**
     * 更新收藏夹排序
     */
    @Query("UPDATE folders SET sort_order = :sortOrder WHERE id = :folderId")
    suspend fun updateSortOrder(folderId: String, sortOrder: Int)

    /**
     * 获取最大排序值
     */
    @Query("SELECT MAX(sort_order) FROM folders")
    suspend fun getMaxSortOrder(): Int?

    /**
     * 根据 ID 获取收藏夹
     */
    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: String): Folder?

    /**
     * 根据 ID 获取收藏夹（Flow）
     */
    @Query("SELECT * FROM folders WHERE id = :folderId")
    fun getFolderByIdFlow(folderId: String): Flow<Folder?>

    /**
     * 获取所有收藏夹（按排序）
     */
    @Query("SELECT * FROM folders ORDER BY sort_order ASC")
    fun getAllFolders(): Flow<List<Folder>>

    /**
     * 获取所有收藏夹（一次性）
     */
    @Query("SELECT * FROM folders ORDER BY sort_order ASC")
    suspend fun getAllFoldersOnce(): List<Folder>

    /**
     * 搜索收藏夹（按名称）
     */
    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%' ORDER BY sort_order ASC")
    fun searchFolders(query: String): Flow<List<Folder>>

    /**
     * 获取收藏夹中的菜谱数量
     */
    @Query("""
        SELECT COUNT(*)
        FROM recipe_folders
        WHERE folder_id = :folderId
    """)
    suspend fun getRecipeCount(folderId: String): Int

    /**
     * 获取所有收藏夹及其菜谱数量
     */
    @Query("""
        SELECT
            f.*,
            COUNT(rf.id) as recipe_count
        FROM folders f
        LEFT JOIN recipe_folders rf ON f.id = rf.folder_id
        GROUP BY f.id
        ORDER BY f.sort_order ASC
    """)
    fun getAllFoldersWithCount(): Flow<List<FolderWithCount>>

    /**
     * 收藏夹及菜谱数量
     */
    data class FolderWithCount(
        @Embedded val folder: Folder,
        val recipe_count: Int
    )
}
