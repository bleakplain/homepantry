package com.homepantry.data.export

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.OutputStream

data class ExportRecipe(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val cookingTime: Int,
    val servings: Int,
    val difficulty: String,
    val categoryName: String?,
    val tags: String,
    val ingredients: List<ExportIngredient>,
    val instructions: List<String>
)

data class ExportIngredient(
    val ingredientName: String,
    val quantity: Double,
    val unit: String
)

data class RecipeExportData(
    val version: String = "1.0",
    val exportDate: Long = System.currentTimeMillis(),
    val recipes: List<ExportRecipe>
)

class RecipeExporter(
    private val repository: RecipeRepository,
    private val context: Context
) {
    private val gson = Gson()

    suspend fun exportRecipes(
        recipeIds: List<String>,
        uri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val exportRecipes = mutableListOf<ExportRecipe>()

            recipeIds.forEach { recipeId ->
                val recipeWithDetails = repository.getRecipeWithDetails(recipeId)
                recipeWithDetails?.let {
                    exportRecipes.add(it.toExportRecipe())
                }
            }

            val exportData = RecipeExportData(recipes = exportRecipes)
            val jsonData = gson.toJson(exportData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonData.toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(Exception("无法打开输出流"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importRecipes(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use(BufferedReader::readText)
            } ?: return@withContext Result.failure(Exception("无法打开输入流"))

            val exportData = gson.fromJson(jsonData, RecipeExportData::class.java)
            var importedCount = 0

            exportData.recipes.forEach { exportRecipe ->
                try {
                    val recipe = Recipe(
                        name = exportRecipe.name,
                        description = exportRecipe.description,
                        imageUrl = exportRecipe.imageUrl,
                        cookingTime = exportRecipe.cookingTime,
                        servings = exportRecipe.servings,
                        difficulty = com.homepantry.data.entity.DifficultyLevel.valueOf(exportRecipe.difficulty),
                        tags = exportRecipe.tags
                    )

                    val ingredients = exportRecipe.ingredients.map { ingredient ->
                        RecipeIngredient(
                            recipeId = recipe.id,
                            ingredientId = "", // Will be matched by name
                            quantity = ingredient.quantity
                        )
                    }

                    val instructions = exportRecipe.instructions.mapIndexed { index, instruction ->
                        RecipeInstruction(
                            recipeId = recipe.id,
                            stepNumber = index + 1,
                            instruction = instruction
                        )
                    }

                    repository.insertRecipeWithDetails(recipe, ingredients, instructions)
                    importedCount++
                } catch (e: Exception) {
                    // Continue with next recipe if one fails
                }
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun RecipeRepository.RecipeWithDetails.toExportRecipe(): ExportRecipe {
        return ExportRecipe(
            id = recipe.id,
            name = recipe.name,
            description = recipe.description,
            imageUrl = recipe.imageUrl,
            cookingTime = recipe.cookingTime,
            servings = recipe.servings,
            difficulty = recipe.difficulty.name,
            categoryName = null, // Would need to fetch category name
            tags = recipe.tags,
            ingredients = ingredients.map {
                ExportIngredient(
                    ingredientName = it.ingredientId, // Using ID as name placeholder
                    quantity = it.quantity,
                    unit = ""
                )
            },
            instructions = instructions.map { it.instruction }
        )
    }
}
