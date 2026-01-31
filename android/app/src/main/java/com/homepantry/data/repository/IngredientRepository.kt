package com.homepantry.data.repository

import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.Ingredient
import com.homepantry.data.entity.PantryItem
import kotlinx.coroutines.flow.Flow

class IngredientRepository(
    private val ingredientDao: IngredientDao,
    private val recipeDao: RecipeDao
) {
    fun getAllIngredients(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()

    suspend fun getIngredientById(ingredientId: String): Ingredient? =
        ingredientDao.getIngredientById(ingredientId)

    fun searchIngredients(query: String): Flow<List<Ingredient>> =
        ingredientDao.searchIngredients(query)

    suspend fun insertIngredient(ingredient: Ingredient) = ingredientDao.insertIngredient(ingredient)

    suspend fun updateIngredient(ingredient: Ingredient) = ingredientDao.updateIngredient(ingredient)

    suspend fun deleteIngredient(ingredient: Ingredient) = ingredientDao.deleteIngredient(ingredient)

    // Pantry items
    fun getAllPantryItems(): Flow<List<PantryItem>> = ingredientDao.getAllPantryItems()

    fun getPantryItemsWithExpiry(): Flow<List<PantryItem>> =
        ingredientDao.getPantryItemsWithExpiry()

    suspend fun getExpiringItems(expiryTime: Long): List<PantryItem> =
        ingredientDao.getExpiringItems(expiryTime)

    suspend fun addPantryItem(item: PantryItem) = ingredientDao.insertPantryItem(item)

    suspend fun updatePantryItem(item: PantryItem) = ingredientDao.updatePantryItem(item)

    suspend fun deletePantryItem(itemId: String) = ingredientDao.deletePantryItemById(itemId)

    suspend fun removePantryItem(item: PantryItem) = ingredientDao.deletePantryItem(item)

    suspend fun cleanExpiredItems() {
        val now = System.currentTimeMillis()
        ingredientDao.deleteExpiredItems(now)
    }

    suspend fun getRecipeRecommendations(): List<String> {
        // Get all pantry items and find recipes that can be made with them
        val pantryItems = ingredientDao.getAllPantryItems()
        // This is a simplified version - in production, you'd implement
        // a more sophisticated matching algorithm
        return emptyList()
    }
}
