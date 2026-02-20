package com.homepantry.data.repository

import android.util.Log
import androidx.room.Transaction
import com.homepantry.data.dao.FolderDao
import com.homepantry.data.entity.Folder
import com.homepantry.data.validation.FolderValidator
import com.homepantry.data.validation.ValidationResult
import com.homepantry.data.constants.Constants
import kotlinx.coroutines.flow.Flow

/**
 * 收藏夹仓库
 */
class FolderRepository(private val folderDao: FolderDao) {

    companion object {
        private const val TAG = "FolderRepository"
    }

    /**
     * 创建收藏夹
     */
    @Transaction
    suspend fun createFolder(
        name: String,
        icon: String? = null,
        color: String? = null
    ): Result<Folder> {
        return try {
            // 验证名称
            val validationResult = FolderValidator.validateName(name)
            if (validationResult is ValidationResult.Error) {
                Log.e(TAG, "文件夹名称验证失败：${validationResult.message}")
                return Result.failure(java.lang.Exception(validationResult.message))
            }

            // 验证颜色
            FolderValidator.validateColor(color)
                .onSuccess {
                    val maxSortOrder = folderDao.getMaxSortOrder() ?: -1
                    val folder = Folder(
                        id = "folder_${java.util.UUID.randomUUID().toString()}",
                        name = name,
                        icon = icon,
                        color = color ?: Constants.Colors.DEFAULT_FOLDER,
                        sortOrder = maxSortOrder + 1
                    )
                    folderDao.insert(folder)
                    Log.d(TAG, "创建文件夹成功：${folder.name}")
                    Result.success(folder)
                }
                .onFailure { e ->
                    Log.e(TAG, "文件夹颜色验证失败：${e.message}")
                    Result.failure(e)
                }
        } catch (e: java.lang.IllegalArgumentException) {
            Log.e(TAG, "参数验证失败", e)
            Result.failure(java.lang.Exception("参数验证失败：${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "创建文件夹失败", e)
            Result.failure(e)
        }
    }

    /**
     * 更新收藏夹
     */
    @Transaction
    suspend fun updateFolder(folder: Folder): Result<Unit> {
        return try {
            // 验证名称
            val validationResult = FolderValidator.validateName(folder.name)
            if (validationResult is ValidationResult.Error) {
                Log.e(TAG, "文件夹名称验证失败：${validationResult.message}")
                return Result.failure(java.lang.Exception(validationResult.message))
            }

            // 验证颜色
            FolderValidator.validateColor(folder.color)
                .onSuccess {
                    folderDao.update(folder.copy(updatedAt = System.currentTimeMillis()))
                    Log.d(TAG, "更新文件夹成功：${folder.name}")
                    Result.success(Unit)
                }
                .onFailure { e ->
                    Log.e(TAG, "文件夹颜色验证失败：${e.message}")
                    Result.failure(e)
                }
        } catch (e: java.lang.IllegalArgumentException) {
            Log.e(TAG, "参数验证失败", e)
            Result.failure(java.lang.Exception("参数验证失败：${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "更新文件夹失败：${folder.name}", e)
            Result.failure(e)
        }
    }

    /**
     * 删除收藏夹
     */
    @Transaction
    suspend fun deleteFolder(folderId: String): Result<Unit> {
        return try {
            folderDao.deleteById(folderId)
            Log.d(TAG, "删除文件夹成功：$folderId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "删除文件夹失败：$folderId", e)
            Result.failure(e)
        }
    }

    /**
     * 重新排序收藏夹
     */
    @Transaction
    suspend fun reorderFolders(folderIds: List<String>): Result<Unit> {
        return try {
            folderIds.forEachIndexed { index, folderId ->
                // 验证排序号
                val sortValidation = FolderValidator.validateSortOrder(index)
                if (sortValidation is ValidationResult.Error) {
                    Log.e(TAG, "排序号验证失败：${sortValidation.message}")
                    return Result.failure(java.lang.Exception(sortValidation.message))
                }

                folderDao.updateSortOrder(folderId, index)
            }
            Log.d(TAG, "重新排序文件夹成功：${folderIds.size} 个")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "重新排序文件夹失败", e)
            Result.failure(e)
        }
    }

    /**
     * 获取所有收藏夹
     */
    fun getAllFolders(): Flow<List<FolderDao.FolderWithCount>> {
        return folderDao.getAllFoldersWithCount()
    }

    /**
     * 获取所有收藏夹（一次性）
     */
    suspend fun getAllFoldersOnce(): List<Folder> {
        return folderDao.getAllFoldersOnce()
    }

    /**
     * 根据 ID 获取收藏夹
     */
    fun getFolderById(folderId: String): Flow<Folder?> {
        return folderDao.getFolderByIdFlow(folderId)
    }

    /**
     * 搜索收藏夹
     */
    fun searchFolders(query: String): Flow<List<Folder>> {
        return folderDao.searchFolders(query)
    }
}
