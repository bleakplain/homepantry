package com.homepantry.data.repository

import androidx.room.Transaction
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import com.homepantry.data.dao.CategoryDao
import com.homepantry.data.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * 分类仓库
 */
class CategoryRepository(
    private val categoryDao: CategoryDao
) {

    companion object {
        private const val TAG = "CategoryRepository"
    }

    /**
     * 创建分类
     */
    @Transaction
    suspend fun createCategory(
        name: String,
        icon: String? = null,
        color: String? = null
    ): Result<Category> {
        return PerformanceMonitor.recordMethodPerformance("createCategory") {
            Logger.enter("createCategory", name, icon, color)

            return try {
                val maxSortOrder = categoryDao.getMaxSortOrder() ?: -1
                val category = Category(
                    id = "category_${java.util.UUID.randomUUID().toString()}",
                    name = name,
                    icon = icon,
                    color = color,
                    sortOrder = maxSortOrder + 1
                )
                categoryDao.insert(category)
                Logger.d(TAG, "创建分类成功：${category.name}")
                Logger.exit("createCategory", category)
                Result.success(category)
            } catch (e: Exception) {
                Logger.e(TAG, "创建分类失败", e)
                Logger.exit("createCategory")
                Result.failure(e)
            }
        }
    }

    /**
     * 更新分类
     */
    @Transaction
    suspend fun updateCategory(category: Category): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("updateCategory") {
            Logger.enter("updateCategory", category.id)

            return try {
                categoryDao.update(category.copy(updatedAt = System.currentTimeMillis()))
                Logger.d(TAG, "更新分类成功：${category.name}")
                Logger.exit("updateCategory")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "更新分类失败：${category.name}", e)
                Logger.exit("updateCategory")
                Result.failure(e)
            }
        }
    }

    /**
     * 删除分类
     */
    @Transaction
    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("deleteCategory") {
            Logger.enter("deleteCategory", categoryId)

            return try {
                categoryDao.deleteById(categoryId)
                Logger.d(TAG, "删除分类成功：$categoryId")
                Logger.exit("deleteCategory")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "删除分类失败：$categoryId", e)
                Logger.exit("deleteCategory")
                Result.failure(e)
            }
        }
    }

    /**
     * 重新排序分类
     */
    @Transaction
    suspend fun reorderCategories(categoryIds: List<String>): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("reorderCategories") {
            Logger.enter("reorderCategories", categoryIds.size)

            return try {
                categoryIds.forEachIndexed { index, categoryId ->
                    categoryDao.updateSortOrder(categoryId, index)
                }
                Logger.d(TAG, "重新排序分类成功：${categoryIds.size} 个")
                Logger.exit("reorderCategories")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "重新排序分类失败", e)
                Logger.exit("reorderCategories")
                Result.failure(e)
            }
        }
    }

    /**
     * 获取所有分类
     */
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }

    /**
     * 根据 ID 获取分类
     */
    fun getCategoryById(categoryId: String): Flow<Category?> {
        return categoryDao.getCategoryById(categoryId)
    }

    /**
     * 搜索分类
     */
    fun searchCategories(query: String): Flow<List<Category>> {
        return categoryDao.searchCategories("%${query}%")
    }
}
