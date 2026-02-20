package com.homepantry.data.repository

import androidx.room.Transaction
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import com.homepantry.data.dao.PantryItemDao
import com.homepantry.data.entity.PantryItem
import com.homepantry.data.entity.ExpirationCheckResult
import com.homepantry.data.entity.ExpirationStatus
import com.homepantry.data.repository.ExpirationRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 食材仓库
 */
class PantryRepository(
    private val pantryItemDao: PantryItemDao,
    private val expirationRepository: ExpirationRepository
) {

    companion object {
        private const val TAG = "PantryRepository"
    }

    /**
     * 添加食材
     */
    @Transaction
    suspend fun addPantryItem(item: PantryItem): Result<PantryItem> {
        return PerformanceMonitor.recordMethodPerformance("addPantryItem") {
            Logger.enter("addPantryItem", item.name)

            return try {
                pantryItemDao.insert(item)
                Logger.d(TAG, "添加食材成功：${item.name}")
                Logger.exit("addPantryItem", item)
                Result.success(item)
            } catch (e: Exception) {
                Logger.e(TAG, "添加食材失败：${item.name}", e)
                Logger.exit("addPantryItem")
                Result.failure(e)
            }
        }
    }

    /**
     * 更新食材
     */
    @Transaction
    suspend fun updatePantryItem(item: PantryItem): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("updatePantryItem") {
            Logger.enter("updatePantryItem", item.id)

            return try {
                pantryItemDao.update(item.copy(updatedAt = System.currentTimeMillis()))
                Logger.d(TAG, "更新食材成功：${item.name}")
                Logger.exit("updatePantryItem")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "更新食材失败：${item.name}", e)
                Logger.exit("updatePantryItem")
                Result.failure(e)
            }
        }
    }

    /**
     * 删除食材
     */
    @Transaction
    suspend fun deletePantryItem(itemId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("deletePantryItem") {
            Logger.enter("deletePantryItem", itemId)

            return try {
                pantryItemDao.deleteById(itemId)
                Logger.d(TAG, "删除食材成功：$itemId")
                Logger.exit("deletePantryItem")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "删除食材失败：$itemId", e)
                Logger.exit("deletePantryItem")
                Result.failure(e)
            }
        }
    }

    /**
     * 检查过期食材
     */
    fun checkExpiringItems(reminderDays: Int): Flow<List<ExpirationCheckResult>> {
        return expirationRepository.checkExpiringItems(reminderDays)
    }

    /**
     * 获取所有食材
     */
    fun getAllItems(): Flow<List<PantryItem>> {
        return pantryItemDao.getAllItems()
    }

    /**
     * 根据 ID 获取食材
     */
    fun getItemById(itemId: String): Flow<PantryItem?> {
        return pantryItemDao.getItemById(itemId)
    }

    /**
     * 根据名称搜索食材
     */
    fun searchItemsByName(query: String): Flow<List<PantryItem>> {
        return pantryItemDao.searchItems("%${query}%")
    }
}
