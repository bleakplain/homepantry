package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.Ingredient
import com.homepantry.data.entity.PantryItem
import com.homepantry.data.entity.StorageLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    // === Ingredient 操作 ===
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :ingredientId")
    suspend fun getIngredientById(ingredientId: String): Ingredient?

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchIngredients(query: String): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    // === PantryItem 操作 ===
    @Query("SELECT * FROM pantry_items ORDER BY purchasedDate DESC")
    fun getAllPantryItems(): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    suspend fun getPantryItemById(id: String): PantryItem?

    @Query("SELECT * FROM pantry_items WHERE ingredientId = :ingredientId")
    fun getPantryItemsByIngredient(ingredientId: String): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE storageLocation = :location ORDER BY expiryDate ASC")
    fun getPantryItemsByLocation(location: StorageLocation): Flow<List<PantryItem>>

    @Query("""
        SELECT pantry_items.* FROM pantry_items
        INNER JOIN ingredients ON pantry_items.ingredientId = ingredients.id
        ORDER BY pantry_items.expiryDate ASC
    """)
    fun getPantryItemsWithExpiry(): Flow<List<PantryItem>>

    @Query("""
        SELECT * FROM pantry_items
        WHERE expiryDate IS NOT NULL
        ORDER BY expiryDate ASC
    """)
    fun getPantryItemsSortedByExpiry(): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE expiryDate < :expiryTime")
    suspend fun getExpiringItems(expiryTime: Long): List<PantryItem>

    @Query("SELECT * FROM pantry_items WHERE expiryDate BETWEEN :startTime AND :endTime")
    suspend fun getItemsExpiringBetween(startTime: Long, endTime: Long): List<PantryItem>

    // 即将到期的食材（N天内）
    @Query("""
        SELECT * FROM pantry_items
        WHERE expiryDate IS NOT NULL
        AND expiryDate > :now
        AND expiryDate <= :deadline
        ORDER BY expiryDate ASC
    """)
    fun getItemsExpiringSoon(now: Long, deadline: Long): Flow<List<PantryItem>>

    // 已过期的食材
    @Query("SELECT * FROM pantry_items WHERE expiryDate < :now")
    fun getExpiredItems(now: Long = System.currentTimeMillis()): Flow<List<PantryItem>>

    // 按存储位置统计
    @Query("SELECT storageLocation, COUNT(*) FROM pantry_items GROUP BY storageLocation")
    suspend fun getCountByStorageLocation(): Map<String, Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItems(items: List<PantryItem>)

    @Update
    suspend fun updatePantryItem(item: PantryItem)

    @Delete
    suspend fun deletePantryItem(item: PantryItem)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deletePantryItemById(id: String)

    @Query("DELETE FROM pantry_items WHERE expiryDate < :expiryTime")
    suspend fun deleteExpiredItems(expiryTime: Long)

    @Query("UPDATE pantry_items SET quantity = :quantity WHERE id = :id")
    suspend fun updatePantryItemQuantity(id: String, quantity: Double)

    @Query("SELECT COUNT(*) FROM pantry_items")
    suspend fun getPantryItemCount(): Int
}
