package com.homepantry.data.recommendation

import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class RecipeRecommendation(
    val recipe: Recipe,
    val matchPercentage: Float,
    val missingIngredients: List<String>,
    val availableIngredients: List<String>,
    val canMake: Boolean
)

class RecipeRecommender(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao
) {

    suspend fun getRecommendations(): List<RecipeRecommendation> {
        // Get all pantry items
        val pantryItems = mutableListOf<com.homepantry.data.entity.PantryItem>()
        ingredientDao.getAllPantryItems().collect { items ->
            pantryItems.addAll(items)
        }

        // Get available ingredient IDs
        val availableIngredientIds = pantryItems.map { it.ingredientId }.toSet()

        // Get all recipes
        val recipes = mutableListOf<Recipe>()
        recipeDao.getAllRecipes().collect { recipeList ->
            recipes.addAll(recipeList)
        }

        // Calculate recommendations
        val recommendations = mutableListOf<RecipeRecommendation>()
        recipes.forEach { recipe ->
            val ingredients = recipeDao.getRecipeIngredients(recipe.id)
            val availableIngredients = mutableListOf<String>()
            val missingIngredients = mutableListOf<String>()

            ingredients.forEach { recipeIngredient ->
                val ingredient = ingredientDao.getIngredientById(recipeIngredient.ingredientId)
                if (availableIngredientIds.contains(recipeIngredient.ingredientId)) {
                    ingredient?.let { availableIngredients.add(it.name) }
                } else {
                    ingredient?.let { missingIngredients.add(it.name) }
                }
            }

            val matchPercentage = if (ingredients.isNotEmpty()) {
                (availableIngredients.size.toFloat() / ingredients.size) * 100
            } else {
                0f
            }

            recommendations.add(
                RecipeRecommendation(
                    recipe = recipe,
                    matchPercentage = matchPercentage,
                    missingIngredients = missingIngredients,
                    availableIngredients = availableIngredients,
                    canMake = missingIngredients.isEmpty()
                )
            )
        }

        // Sort by match percentage and canMake status
        return recommendations.sortedWith(
            compareByDescending<RecipeRecommendation> { it.canMake }
                .thenByDescending { it.matchPercentage }
        )
    }

    suspend fun getRecipesICanMake(): Flow<List<Recipe>> {
        val pantryItems = mutableListOf<com.homepantry.data.entity.PantryItem>()
        ingredientDao.getAllPantryItems().collect { items ->
            pantryItems.addAll(items)
        }

        val availableIngredientIds = pantryItems.map { it.ingredientId }.toSet()

        return recipeDao.getAllRecipes().map { recipes ->
            recipes.filter { recipe ->
                val ingredients = recipeDao.getRecipeIngredients(recipe.id)
                ingredients.all { availableIngredientIds.contains(it.ingredientId) }
            }
        }
    }

    suspend fun getQuickMeals(maxTime: Int = 30): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        recipeDao.getAllRecipes().collect { recipeList ->
            recipes.addAll(recipeList.filter { it.cookingTime <= maxTime })
        }
        return recipes.sortedBy { it.cookingTime }
    }

    suspend fun getEasyMeals(): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        recipeDao.getAllRecipes().collect { recipeList ->
            recipes.addAll(recipeList.filter { it.difficulty == com.homepantry.data.entity.DifficultyLevel.EASY })
        }
        return recipes.shuffled()
    }
}
