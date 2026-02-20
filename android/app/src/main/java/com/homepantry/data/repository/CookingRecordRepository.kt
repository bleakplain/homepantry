package com.homepantry.data.repository

import androidx.room.Transaction
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import com.homepantry.data.dao.CookingRecordDao
import com.homepantry.data.entity.CookingRecord
import kotlinx.coroutines.flow.Flow

/**
 * 烹饪记录仓库
 */
class CookingRecordRepository(
    private val cookingRecordDao: CookingRecordDao
) {

    companion object {
        private const val TAG = "CookingRecordRepository"
    }

    /**
     * 创建烹饪记录
     */
    @Transaction
    suspend fun createCookingRecord(record: CookingRecord): Result<CookingRecord> {
        return PerformanceMonitor.recordMethodPerformance("createCookingRecord") {
            Logger.enter("createCookingRecord", record.recipeId, record.date)

            return try {
                cookingRecordDao.insert(record)
                Logger.d(TAG, "创建烹饪记录成功：${record.recipeId}")
                Logger.exit("createCookingRecord", record)
                Result.success(record)
            } catch (e: Exception) {
                Logger.e(TAG, "创建烹饪记录失败：${record.recipeId}", e)
                Logger.exit("createCookingRecord")
                Result.failure(e)
            }
        }
    }

    /**
     * 更新烹饪记录
     */
    @Transaction
    suspend fun updateCookingRecord(record: CookingRecord): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("updateCookingRecord") {
            Logger.enter("updateCookingRecord", record.id)

            return try {
                cookingRecordDao.update(record.copy(updatedAt = System.currentTimeMillis()))
                Logger.d(TAG, "更新烹饪记录成功：${record.id}")
                Logger.exit("updateCookingRecord")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "更新烹饪记录失败：${record.id}", e)
                Logger.exit("updateCookingRecord")
                Result.failure(e)
            }
        }
    }

    /**
     * 删除烹饪记录
     */
    @Transaction
    suspend fun deleteCookingRecord(recordId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("deleteCookingRecord") {
            Logger.enter("deleteCookingRecord", recordId)

            return try {
                cookingRecordDao.deleteById(recordId)
                Logger.d(TAG, "删除烹饪记录成功：$recordId")
                Logger.exit("deleteCookingRecord")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "删除烹饪记录失败：$recordId", e)
                Logger.exit("deleteCookingRecord")
                Result.failure(e)
            }
        }
    }

    /**
     * 根据 recipeId 获取烹饪记录
     */
    fun getCookingRecordsByRecipe(recipeId: String): Flow<List<CookingRecord>> {
        return cookingRecordDao.getRecordsByRecipe(recipeId)
    }

    /**
     * 获取所有烹饪记录
     */
    fun getAllCookingRecords(): Flow<List<CookingRecord>> {
        return cookingRecordDao.getAllRecords()
    }

    /**
     * 根据日期范围获取烹饪记录
     */
    fun getCookingRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<CookingRecord>> {
        return cookingRecordDao.getRecordsByDateRange(startDate, endDate)
    }
}
