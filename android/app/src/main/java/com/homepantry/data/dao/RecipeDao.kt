package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): Recipe?

    @Query("SELECT * FROM recipes WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getRecipesByCategory(categoryId: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchRecipes(query: String): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    // Recipe ingredients
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getRecipeIngredients(recipeId: String): List<RecipeIngredient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(ingredient: RecipeIngredient)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: String)

    // Recipe instructions
    @Query("SELECT * FROM recipe_instructions WHERE recipeId = :recipeId ORDER BY stepNumber")
    suspend fun getRecipeInstructions(recipeId: String): List<RecipeInstruction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeInstruction(instruction: RecipeInstruction)

    @Query("DELETE FROM recipe_instructions WHERE recipeId = :recipeId")
    suspend fun deleteRecipeInstructions(recipeId: String)
}
