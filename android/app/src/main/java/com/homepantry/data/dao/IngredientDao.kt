package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.Ingredient
import com.homepantry.data.entity.PantryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
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

    // Pantry items
    @Query("SELECT * FROM pantry_items ORDER BY purchasedDate DESC")
    fun getAllPantryItems(): Flow<List<PantryItem>>

    @Query("""
        SELECT pantry_items.* FROM pantry_items
        INNER JOIN ingredients ON pantry_items.ingredientId = ingredients.id
        ORDER BY pantry_items.expiryDate ASC
    """)
    fun getPantryItemsWithExpiry(): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE expiryDate < :expiryTime")
    suspend fun getExpiringItems(expiryTime: Long): List<PantryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryItem)

    @Update
    suspend fun updatePantryItem(item: PantryItem)

    @Delete
    suspend fun deletePantryItem(item: PantryItem)

    @Query("DELETE FROM pantry_items WHERE expiryDate < :expiryTime")
    suspend fun deleteExpiredItems(expiryTime: Long)
}
