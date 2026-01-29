package com.homepantry.data.repository

import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao
) {
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun getRecipeById(recipeId: String): Recipe? = recipeDao.getRecipeById(recipeId)

    fun getRecipesByCategory(categoryId: String): Flow<List<Recipe>> =
        recipeDao.getRecipesByCategory(categoryId)

    fun searchRecipes(query: String): Flow<List<Recipe>> = recipeDao.searchRecipes(query)

    suspend fun insertRecipe(recipe: Recipe) = recipeDao.insertRecipe(recipe)

    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    suspend fun insertRecipeWithDetails(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>,
        instructions: List<RecipeInstruction>
    ) {
        recipeDao.insertRecipe(recipe)
        ingredients.forEach { recipeDao.insertRecipeIngredient(it) }
        instructions.forEach { recipeDao.insertRecipeInstruction(it) }
    }

    suspend fun updateRecipeWithDetails(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>,
        instructions: List<RecipeInstruction>
    ) {
        recipeDao.updateRecipe(recipe)
        recipeDao.deleteRecipeIngredients(recipe.id)
        recipeDao.deleteRecipeInstructions(recipe.id)
        ingredients.forEach { recipeDao.insertRecipeIngredient(it) }
        instructions.forEach { recipeDao.insertRecipeInstruction(it) }
    }

    suspend fun getRecipeWithDetails(recipeId: String): RecipeWithDetails? {
        val recipe = recipeDao.getRecipeById(recipeId) ?: return null
        val ingredients = recipeDao.getRecipeIngredients(recipeId)
        val instructions = recipeDao.getRecipeInstructions(recipeId)
        return RecipeWithDetails(recipe, ingredients, instructions)
    }

    // Favorites
    fun getFavoriteRecipes(): Flow<List<Recipe>> = recipeDao.getFavoriteRecipes()

    suspend fun toggleFavorite(recipeId: String) {
        val recipe = recipeDao.getRecipeById(recipeId) ?: return
        recipeDao.updateFavoriteStatus(recipeId, !recipe.isFavorite)
    }

    suspend fun setFavoriteStatus(recipeId: String, isFavorite: Boolean) {
        recipeDao.updateFavoriteStatus(recipeId, isFavorite)
    }

    data class RecipeWithDetails(
        val recipe: Recipe,
        val ingredients: List<RecipeIngredient>,
        val instructions: List<RecipeInstruction>
    )
}
