package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.ShoppingItem
import com.homepantry.data.entity.ShoppingList
import kotlinx.coroutines.flow.Flow

/**
 * 购物清单 DAO
 */
@Dao
interface ShoppingListDao {
    // === ShoppingList 操作 ===
    @Query("SELECT * FROM shopping_lists ORDER BY date DESC")
    fun getAllLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getListById(id: String): ShoppingList?

    @Query("SELECT * FROM shopping_lists WHERE isCompleted = 0 ORDER BY date DESC")
    fun getActiveLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedLists(): Flow<List<ShoppingList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingList): Long

    @Update
    suspend fun updateList(list: ShoppingList)

    @Delete
    suspend fun deleteList(list: ShoppingList)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteListById(id: String)

    @Query("UPDATE shopping_lists SET isCompleted = 1, completedAt = :timestamp WHERE id = :id")
    suspend fun markListCompleted(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE shopping_lists SET isCompleted = 0, completedAt = NULL WHERE id = :id")
    suspend fun markListActive(id: String)

    // === ShoppingItem 操作 ===
    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY category, sortOrder")
    fun getItemsByList(listId: String): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE listId = :listId AND category = :category ORDER BY sortOrder")
    fun getItemsByCategory(listId: String, category: String): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getItemById(id: String): ShoppingItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingItem>)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM shopping_items WHERE listId = :listId")
    suspend fun deleteItemsByList(listId: String)

    @Query("UPDATE shopping_items SET isPurchased = :purchased WHERE id = :id")
    suspend fun updateItemPurchased(id: String, purchased: Boolean)

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE id = :id")
    suspend fun updateItemChecked(id: String, checked: Boolean)

    @Query("UPDATE shopping_items SET actualPrice = :price WHERE id = :id")
    suspend fun updateItemPrice(id: String, price: Double?)

    // 统计查询
    @Query("SELECT COUNT(*) FROM shopping_items WHERE listId = :listId")
    suspend fun getItemCount(listId: String): Int

    @Query("SELECT COUNT(*) FROM shopping_items WHERE listId = :listId AND isPurchased = 1")
    suspend fun getPurchasedCount(listId: String): Int

    @Query("SELECT SUM(estimatedPrice) FROM shopping_items WHERE listId = :listId AND estimatedPrice IS NOT NULL")
    suspend fun getTotalEstimated(listId: String): Double?

    @Query("SELECT SUM(actualPrice) FROM shopping_items WHERE listId = :listId AND actualPrice IS NOT NULL")
    suspend fun getTotalActual(listId: String): Double?
}
