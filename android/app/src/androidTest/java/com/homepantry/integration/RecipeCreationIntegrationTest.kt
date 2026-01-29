package com.homepantry.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.data.repository.RecipeRepository
import com.homepantry.viewmodel.RecipeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RecipeCreationIntegrationTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var repository: RecipeRepository
    private lateinit var viewModel: RecipeViewModel

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomePantryDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RecipeRepository(
            database.recipeDao(),
            database.ingredientDao()
        )
        viewModel = RecipeViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createCompleteRecipe_workflow() = runTest {
        // Create a complete recipe with ingredients and instructions
        val recipeName = "番茄炒蛋"
        val description = "经典家常菜"
        val cookingTime = 15
        val servings = 2
        val difficulty = DifficultyLevel.EASY

        val ingredients = listOf(
            RecipeIngredient(
                recipeId = "temp-1",
                ingredientId = "tomato",
                quantity = 2.0,
                unit = "个",
                notes = "去蒂切块"
            ),
            RecipeIngredient(
                recipeId = "temp-1",
                ingredientId = "egg",
                quantity = 3.0,
                unit = "个",
                notes = "打散"
            )
        )

        val instructions = listOf(
            RecipeInstruction(
                recipeId = "temp-1",
                stepNumber = 1,
                instructionText = "番茄洗净切块"
            ),
            RecipeInstruction(
                recipeId = "temp-1",
                stepNumber = 2,
                instructionText = "鸡蛋打散"
            ),
            RecipeInstruction(
                recipeId = "temp-1",
                stepNumber = 3,
                instructionText = "热锅下油，先炒鸡蛋盛起"
            ),
            RecipeInstruction(
                recipeId = "temp-1",
                stepNumber = 4,
                instructionText = "再炒番茄，加入鸡蛋翻炒"
            )
        )

        // Add recipe through ViewModel
        viewModel.addRecipe(
            name = recipeName,
            description = description,
            cookingTime = cookingTime,
            servings = servings,
            difficulty = difficulty,
            ingredients = ingredients,
            instructions = instructions
        )

        // Wait for operation to complete
        kotlinx.coroutines.test.TestDispatcher().scheduler.advanceUntilIdle()

        // Verify recipe was created
        val recipes = repository.getAllRecipes().first()
        assertEquals(1, recipes.size)

        val createdRecipe = recipes[0]
        assertEquals(recipeName, createdRecipe.name)
        assertEquals(description, createdRecipe.description)
        assertEquals(cookingTime, createdRecipe.cookingTime)
        assertEquals(servings, createdRecipe.servings)
        assertEquals(difficulty, createdRecipe.difficulty)

        // Verify ingredients were added
        val recipeIngredients = repository.getRecipeIngredients(createdRecipe.id).first()
        assertEquals(2, recipeIngredients.size)

        // Verify instructions were added
        val recipeInstructions = repository.getRecipeInstructions(createdRecipe.id).first()
        assertEquals(4, recipeInstructions.size)

        // Verify instructions are in correct order
        recipeInstructions.forEachIndexed { index, instruction ->
            assertEquals(index + 1, instruction.stepNumber)
        }
    }

    @Test
    fun createAndEditRecipe_workflow() = runTest {
        // Create initial recipe
        viewModel.addRecipe(
            name = "原始菜名",
            description = "原始描述",
            cookingTime = 30,
            servings = 4,
            difficulty = DifficultyLevel.MEDIUM,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        kotlinx.coroutines.test.TestDispatcher().scheduler.advanceUntilIdle()

        val recipes = repository.getAllRecipes().first()
        val recipe = recipes[0]

        // Update recipe
        val updatedIngredients = listOf(
            RecipeIngredient(
                recipeId = recipe.id,
                ingredientId = "ing1",
                quantity = 1.0
            )
        )

        val updatedInstructions = listOf(
            RecipeInstruction(
                recipeId = recipe.id,
                stepNumber = 1,
                instructionText = "新步骤"
            )
        )

        viewModel.updateRecipe(
            recipe = recipe.copy(
                name = "更新后的菜名",
                description = "更新后的描述",
                cookingTime = 45,
                servings = 6,
                difficulty = DifficultyLevel.HARD
            ),
            ingredients = updatedIngredients,
            instructions = updatedInstructions
        )

        kotlinx.coroutines.test.TestDispatcher().scheduler.advanceUntilIdle()

        // Verify updates
        val updatedRecipe = repository.getRecipeById(recipe.id)
        assertNotNull(updatedRecipe)
        assertEquals("更新后的菜名", updatedRecipe?.name)
        assertEquals("更新后的描述", updatedRecipe?.description)
        assertEquals(45, updatedRecipe?.cookingTime)
        assertEquals(6, updatedRecipe?.servings)
        assertEquals(DifficultyLevel.HARD, updatedRecipe?.difficulty)

        // Verify ingredients were updated
        val ingredients = repository.getRecipeIngredients(recipe.id).first()
        assertEquals(1, ingredients.size)

        // Verify instructions were updated
        val instructions = repository.getRecipeInstructions(recipe.id).first()
        assertEquals(1, instructions.size)
    }

    @Test
    fun createAndDeleteRecipe_workflow() = runTest {
        // Create recipe
        viewModel.addRecipe(
            name = "要删除的菜谱",
            description = null,
            cookingTime = 20,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        kotlinx.coroutines.test.TestDispatcher().scheduler.advanceUntilIdle()

        val recipes = repository.getAllRecipes().first()
        assertEquals(1, recipes.size)

        // Delete recipe
        viewModel.deleteRecipe(recipes[0])

        kotlinx.coroutines.test.TestDispatcher().scheduler.advanceUntilIdle()

        // Verify deletion
        val remainingRecipes = repository.getAllRecipes().first()
        assertTrue(remainingRecipes.isEmpty())
    }

    @Test
    fun searchAndFilterRecipes_workflow() = runTest {
        // Create multiple recipes
        val recipes = listOf(
            Recipe(
                id = "r1",
                name = "番茄炒蛋",
                description = "简单",
                cookingTime = 15,
                servings = 2,
                difficulty = DifficultyLevel.EASY
            ),
            Recipe(
                id = "r2",
                name = "红烧肉",
                description = "复杂",
                cookingTime = 90,
                servings = 4,
                difficulty = DifficultyLevel.HARD
            ),
            Recipe(
                id = "r3",
                name = "清蒸鱼",
                description = "简单",
                cookingTime = 20,
                servings = 3,
                difficulty = DifficultyLevel.MEDIUM
            )
        )

        recipes.forEach { repository.insertRecipe(it) }

        // Test search
        val searchResults = repository.searchRecipes("番茄").first()
        assertEquals(1, searchResults.size)
        assertEquals("r1", searchResults[0].id)

        // Test filter by difficulty
        val easyRecipes = repository.getRecipesByDifficulty(DifficultyLevel.EASY).first()
        assertEquals(1, easyRecipes.size)

        // Test filter by cooking time
        val quickRecipes = repository.getRecipesWithMaxCookingTime(20).first()
        assertEquals(2, quickRecipes.size)

        // Test sort by cooking time
        val sortedRecipes = repository.getRecipesByCookingTimeAsc().first()
        assertEquals(15, sortedRecipes[0].cookingTime)
        assertEquals(90, sortedRecipes[2].cookingTime)
    }

    @Test
    fun favoriteRecipe_workflow() = runTest {
        // Create recipe
        val recipe = Recipe(
            id = "r1",
            name = "收藏测试",
            description = null,
            cookingTime = 30,
            servings = 2,
            difficulty = DifficultyLevel.MEDIUM,
            isFavorite = false
        )

        repository.insertRecipe(recipe)

        // Add to favorites
        viewModel.toggleFavorite(recipe.id, true)

        // Verify favorite status
        val favoritedRecipe = repository.getRecipeById(recipe.id)
        assertNotNull(favoritedRecipe)
        // Note: This would require the DAO to return updated data

        // Get favorite recipes
        val favorites = repository.getFavoriteRecipes().first()
        assertTrue(favorites.isNotEmpty())
    }

    @Test
    fun complexRecipeWithManyIngredients_workflow() = runTest {
        // Create recipe with many ingredients
        val ingredients = (1..20).map { index ->
            RecipeIngredient(
                recipeId = "temp",
                ingredientId = "ing$index",
                quantity = index.toDouble(),
                unit = "克",
                notes = "食材$index"
            )
        }

        val instructions = (1..10).map { index ->
            RecipeInstruction(
                recipeId = "temp",
                stepNumber = index,
                instructionText = "步骤$index的详细说明"
            )
        }

        viewModel.addRecipe(
            name = "复杂菜谱",
            description = "包含20种食材和10个步骤",
            cookingTime = 120,
            servings = 8,
            difficulty = DifficultyLevel.HARD,
            ingredients = ingredients,
            instructions = instructions
        )

        kotlinx.coroutines.test.TestDispatcher().scheduler.advanceUntilIdle()

        // Verify all ingredients were saved
        val recipes = repository.getAllRecipes().first()
        val savedIngredients = repository.getRecipeIngredients(recipes[0].id).first()
        assertEquals(20, savedIngredients.size)

        // Verify all instructions were saved
        val savedInstructions = repository.getRecipeInstructions(recipes[0].id).first()
        assertEquals(10, savedInstructions.size)
    }
}
