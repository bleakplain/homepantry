package com.homepantry.data.repository

import androidx.room.Transaction
import android.util.Log
import com.homepantry.data.dao.FolderDao
import com.homepantry.data.entity.Folder
import com.homepantry.data.validation.FolderValidator
import com.homepantry.data.validation.ValidationResult
import com.homepantry.data.constants.Constants
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
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
        return PerformanceMonitor.recordMethodPerformance("createFolder") {
            Logger.enter("createFolder", name, icon, color)

            return try {
                // 验证名称
                val validationResult = FolderValidator.validateName(name)
                if (validationResult is ValidationResult.Error) {
                    Logger.e(TAG, "文件夹名称验证失败：${validationResult.message}")
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
                        Logger.d(TAG, "创建文件夹成功：${folder.name}")
                        Logger.exit("createFolder", folder)
                        Result.success(folder)
                    }
                    .onFailure { e ->
                        Logger.e(TAG, "文件夹颜色验证失败：${e.message}")
                        Result.failure(e)
                    }
            } catch (e: java.lang.IllegalArgumentException) {
                Logger.e(TAG, "参数验证失败", e)
                Result.failure(java.lang.Exception("参数验证失败：${e.message}", e))
            } catch (e: Exception) {
                Logger.e(TAG, "创建文件夹失败", e)
                Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
            }
        }
    }

    /**
     * 更新收藏夹
     */
    @Transaction
    suspend fun updateFolder(folder: Folder): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("updateFolder") {
            Logger.enter("updateFolder", folder.id)

            return try {
                // 验证名称
                val validationResult = FolderValidator.validateName(folder.name)
                if (validationResult is ValidationResult.Error) {
                    Logger.e(TAG, "文件夹名称验证失败：${validationResult.message}")
                    return Result.failure(java.lang.Exception(validationResult.message))
                }

                // 验证颜色
                FolderValidator.validateColor(folder.color)
                    .onSuccess {
                        folderDao.update(folder.copy(updatedAt = System.currentTimeMillis()))
                        Logger.d(TAG, "更新文件夹成功：${folder.name}")
                        Logger.exit("updateFolder")
                        Result.success(Unit)
                    }
                    .onFailure { e ->
                        Logger.e(TAG, "文件夹颜色验证失败：${e.message}")
                        Result.failure(e)
                    }
            } catch (e: java.lang.IllegalArgumentException) {
                Logger.e(TAG, "参数验证失败", e)
                Result.failure(java.lang.Exception("参数验证失败：${e.message}", e))
            } catch (e: Exception) {
                Logger.e(TAG, "更新文件夹失败：${folder.name}", e)
                Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
            }
        }
    }

    /**
     * 删除收藏夹
     */
    @Transaction
    suspend fun deleteFolder(folderId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("deleteFolder") {
            Logger.enter("deleteFolder", folderId)

            return try {
                folderDao.deleteById(folderId)
                Logger.d(TAG, "删除文件夹成功：$folderId")
                Logger.exit("deleteFolder")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "删除文件夹失败：$folderId", e)
                Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
            }
        }
    }

    /**
     * 重新排序收藏夹
     */
    @Transaction
    suspend fun reorderFolders(folderIds: List<String>): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("reorderFolders") {
            Logger.enter("reorderFolders", folderIds.size)

            return try {
                folderIds.forEachIndexed { index, folderId ->
                    // 验证排序号
                    val sortValidation = FolderValidator.validateSortOrder(index)
                    if (sortValidation is ValidationResult.Error) {
                        Logger.e(TAG, "排序号验证失败：${sortValidation.message}")
                        return Result.failure(java.lang.Exception(sortValidation.message))
                    }

                    folderDao.updateSortOrder(folderId, index)
                }
                Logger.d(TAG, "重新排序文件夹成功：${folderIds.size} 个")
                Logger.exit("reorderFolders")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "重新排序文件夹失败", e)
                Result.failure(java.lang.Exception("数据库操作失败：${e.message}", e))
            }
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
