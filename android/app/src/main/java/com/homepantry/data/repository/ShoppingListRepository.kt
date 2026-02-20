package com.homepantry.data.repository

import androidx.room.Transaction
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import com.homepantry.data.dao.ShoppingListItemDao
import com.homepantry.data.entity.ShoppingListItem
import kotlinx.coroutines.flow.Flow

/**
 * 购物列表仓库
 */
class ShoppingListRepository(
    private val shoppingListItemDao: ShoppingListItemDao
) {

    companion object {
        private const val TAG = "ShoppingListRepository"
    }

    /**
     * 添加购物列表项
     */
    @Transaction
    suspend fun addShoppingListItem(item: ShoppingListItem): Result<ShoppingListItem> {
        return PerformanceMonitor.recordMethodPerformance("addShoppingListItem") {
            Logger.enter("addShoppingListItem", item.name)

            return try {
                shoppingListItemDao.insert(item)
                Logger.d(TAG, "添加购物列表项成功：${item.name}")
                Logger.exit("addShoppingListItem", item)
                Result.success(item)
            } catch (e: Exception) {
                Logger.e(TAG, "添加购物列表项失败：${item.name}", e)
                Logger.exit("addShoppingListItem")
                Result.failure(e)
            }
        }
    }

    /**
     * 更新购物列表项
     */
    @Transaction
    suspend fun updateShoppingListItem(item: ShoppingListItem): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("updateShoppingListItem") {
            Logger.enter("updateShoppingListItem", item.id)

            return try {
                shoppingListItemDao.update(item.copy(updatedAt = System.currentTimeMillis()))
                Logger.d(TAG, "更新购物列表项成功：${item.name}")
                Logger.exit("updateShoppingListItem")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "更新购物列表项失败：${item.name}", e)
                Logger.exit("updateShoppingListItem")
                Result.failure(e)
            }
        }
    }

    /**
     * 删除购物列表项
     */
    @Transaction
    suspend fun deleteShoppingListItem(itemId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("deleteShoppingListItem") {
            Logger.enter("deleteShoppingListItem", itemId)

            return try {
                shoppingListItemDao.deleteById(itemId)
                Logger.d(TAG, "删除购物列表项成功：$itemId")
                Logger.exit("deleteShoppingListItem")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "删除购物列表项失败：$itemId", e)
                Logger.exit("deleteShoppingListItem")
                Result.failure(e)
            }
        }
    }

    /**
     * 获取所有购物列表项
     */
    fun getAllShoppingListItems(): Flow<List<ShoppingListItem>> {
        return shoppingListItemDao.getAllItems()
    }

    /**
     * 根据 ID 获取购物列表项
     */
    fun getShoppingListItemById(itemId: String): Flow<ShoppingListItem?> {
        return shoppingListItemDao.getItemById(itemId)
    }

    /**
     * 根据 recipeId 获取购物列表项
     */
    fun getShoppingListItemsByRecipe(recipeId: String): Flow<List<ShoppingListItem>> {
        return shoppingListItemDao.getItemsByRecipe(recipeId)
    }

    /**
     * 标记购物列表项为已购买
     */
    @Transaction
    suspend fun markAsPurchased(itemId: String): Result<Unit> {
        return PerformanceMonitor.recordMethodPerformance("markAsPurchased") {
            Logger.enter("markAsPurchased", itemId)

            return try {
                shoppingListItemDao.markAsPurchased(itemId, System.currentTimeMillis())
                Logger.d(TAG, "标记购物列表项为已购买成功：$itemId")
                Logger.exit("markAsPurchased")
                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(TAG, "标记购物列表项为已购买失败：$itemId", e)
                Logger.exit("markAsPurchased")
                Result.failure(e)
            }
        }
    }
}
