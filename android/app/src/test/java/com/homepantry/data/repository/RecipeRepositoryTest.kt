package com.homepantry.data.repository

import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecipeRepositoryTest {

    @Mock
    private lateinit var recipeDao: RecipeDao

    @Mock
    private lateinit var ingredientDao: IngredientDao

    private lateinit var repository: RecipeRepository

    private val testRecipe = Recipe(
        id = "test-recipe-id",
        name = "番茄炒蛋",
        description = "经典家常菜",
        cookingTime = 15,
        servings = 2,
        difficulty = DifficultyLevel.EASY
    )

    private val testIngredients = listOf(
        RecipeIngredient(
            recipeId = "test-recipe-id",
            ingredientId = "tomato",
            quantity = 2.0
        ),
        RecipeIngredient(
            recipeId = "test-recipe-id",
            ingredientId = "egg",
            quantity = 3.0
        )
    )

    private val testInstructions = listOf(
        RecipeInstruction(
            recipeId = "test-recipe-id",
            stepNumber = 1,
            instruction = "番茄洗净切块"
        ),
        RecipeInstruction(
            recipeId = "test-recipe-id",
            stepNumber = 2,
            instruction = "鸡蛋打散"
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = RecipeRepository(recipeDao, ingredientDao)
    }

    // Basic CRUD tests
    @Test
    fun `insert recipe calls dao insert`() = runTest {
        repository.insertRecipe(testRecipe)

        verify(recipeDao).insertRecipe(eq(testRecipe))
    }

    @Test
    fun `update recipe calls dao update`() = runTest {
        repository.updateRecipe(testRecipe)

        verify(recipeDao).updateRecipe(eq(testRecipe))
    }

    @Test
    fun `delete recipe calls dao delete`() = runTest {
        repository.deleteRecipe(testRecipe)

        verify(recipeDao).deleteRecipe(eq(testRecipe))
    }

    @Test
    fun `get recipe by id calls dao getRecipeById`() = runTest {
        repository.getRecipeById("test-recipe-id")

        verify(recipeDao).getRecipeById(eq("test-recipe-id"))
    }

    // Recipe with details tests
    @Test
    fun `insert recipe with details calls all dao methods`() = runTest {
        repository.insertRecipeWithDetails(testRecipe, testIngredients, testInstructions)

        verify(recipeDao).insertRecipe(eq(testRecipe))
        verify(recipeDao).insertRecipeIngredient(eq(testIngredients[0]))
        verify(recipeDao).insertRecipeIngredient(eq(testIngredients[1]))
        verify(recipeDao).insertRecipeInstruction(eq(testInstructions[0]))
        verify(recipeDao).insertRecipeInstruction(eq(testInstructions[1]))
    }

    @Test
    fun `insert recipe with empty ingredients and instructions`() = runTest {
        repository.insertRecipeWithDetails(testRecipe, emptyList(), emptyList())

        verify(recipeDao).insertRecipe(eq(testRecipe))
        verify(recipeDao, never()).insertRecipeIngredient(any())
        verify(recipeDao, never()).insertRecipeInstruction(any())
    }

    @Test
    fun `update recipe with details deletes and reinserts ingredients and instructions`() = runTest {
        repository.updateRecipeWithDetails(testRecipe, testIngredients, testInstructions)

        verify(recipeDao).updateRecipe(eq(testRecipe))
        verify(recipeDao).deleteRecipeIngredients(eq(testRecipe.id))
        verify(recipeDao).deleteRecipeInstructions(eq(testRecipe.id))
        verify(recipeDao).insertRecipeIngredient(eq(testIngredients[0]))
        verify(recipeDao).insertRecipeInstruction(eq(testInstructions[0]))
    }

    @Test
    fun `update recipe with empty ingredients list still deletes existing`() = runTest {
        repository.updateRecipeWithDetails(testRecipe, emptyList(), emptyList())

        verify(recipeDao).updateRecipe(eq(testRecipe))
        verify(recipeDao).deleteRecipeIngredients(eq(testRecipe.id))
        verify(recipeDao).deleteRecipeInstructions(eq(testRecipe.id))
        verify(recipeDao, never()).insertRecipeIngredient(any())
        verify(recipeDao, never()).insertRecipeInstruction(any())
    }

    // Search and filter tests
    @Test
    fun `search recipes calls dao searchRecipes`() = runTest {
        repository.searchRecipes("番茄")

        verify(recipeDao).searchRecipes(eq("番茄"))
    }

    @Test
    fun `search recipes with empty query returns all`() = runTest {
        val resultFlow = flowOf<List<Recipe>>(emptyList())
        whenever(recipeDao.searchRecipes("")).thenReturn(resultFlow)

        val result = repository.searchRecipes("").first()

        verify(recipeDao).searchRecipes(eq(""))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `search recipes with special characters`() = runTest {
        repository.searchRecipes("特殊字符!@#")

        verify(recipeDao).searchRecipes(eq("特殊字符!@#"))
    }

    @Test
    fun `get recipes by difficulty calls dao`() = runTest {
        repository.getRecipesByDifficulty(DifficultyLevel.EASY)

        verify(recipeDao).getRecipesByDifficulty(eq(DifficultyLevel.EASY))
    }

    @Test
    fun `get recipes with max cooking time calls dao`() = runTest {
        repository.getRecipesWithMaxCookingTime(30)

        verify(recipeDao).getRecipesWithMaxCookingTime(eq(30))
    }

    @Test
    fun `get recipes with max cooking time zero returns no recipes`() = runTest {
        repository.getRecipesWithMaxCookingTime(0)

        verify(recipeDao).getRecipesWithMaxCookingTime(eq(0))
    }

    @Test
    fun `get recipes by servings calls dao`() = runTest {
        repository.getRecipesByServings(minServings = 2)

        verify(recipeDao).getRecipesByServings(eq(2), eq(null))
    }

    @Test
    fun `get recipes by servings with range calls dao`() = runTest {
        repository.getRecipesByServings(minServings = 2, maxServings = 4)

        verify(recipeDao).getRecipesByServings(eq(2), eq(4))
    }

    // Favorites tests
    @Test
    fun `get favorite recipes calls dao`() = runTest {
        repository.getFavoriteRecipes()

        verify(recipeDao).getFavoriteRecipes()
    }

    @Test
    fun `update favorite status calls dao`() = runTest {
        repository.updateFavoriteStatus("recipe-1", true)

        verify(recipeDao).updateFavoriteStatus(eq("recipe-1"), eq(true))
    }

    @Test
    fun `toggle favorite from false to true`() = runTest {
        repository.updateFavoriteStatus("recipe-1", true)

        verify(recipeDao).updateFavoriteStatus(eq("recipe-1"), eq(true))
    }

    @Test
    fun `toggle favorite from true to false`() = runTest {
        repository.updateFavoriteStatus("recipe-1", false)

        verify(recipeDao).updateFavoriteStatus(eq("recipe-1"), eq(false))
    }

    // Sorting tests
    @Test
    fun `get recipes by name ascending calls dao`() = runTest {
        repository.getRecipesByNameAsc()

        verify(recipeDao).getRecipesByNameAsc()
    }

    @Test
    fun `get recipes by name descending calls dao`() = runTest {
        repository.getRecipesByNameDesc()

        verify(recipeDao).getRecipesByNameDesc()
    }

    @Test
    fun `get recipes by newest first calls dao`() = runTest {
        repository.getRecipesByNewestFirst()

        verify(recipeDao).getRecipesByNewestFirst()
    }

    @Test
    fun `get recipes by cooking time ascending calls dao`() = runTest {
        repository.getRecipesByCookingTimeAsc()

        verify(recipeDao).getRecipesByCookingTimeAsc()
    }

    @Test
    fun `get recipes by difficulty ascending calls dao`() = runTest {
        repository.getRecipesByDifficultyAsc()

        verify(recipeDao).getRecipesByDifficultyAsc()
    }

    // Category tests
    @Test
    fun `get recipes by category calls dao`() = runTest {
        repository.getRecipesByCategory("cat-1")

        verify(recipeDao).getRecipesByCategory(eq("cat-1"))
    }

    @Test
    fun `get recipes by empty category id`() = runTest {
        repository.getRecipesByCategory("")

        verify(recipeDao).getRecipesByCategory(eq(""))
    }

    // Recipe ingredients and instructions tests
    @Test
    fun `get recipe ingredients calls dao`() = runTest {
        val flow = flowOf<List<RecipeIngredient>>(emptyList())
        whenever(recipeDao.getRecipeIngredients("recipe-1")).thenReturn(flow)

        repository.getRecipeIngredients("recipe-1").first()

        verify(recipeDao).getRecipeIngredients(eq("recipe-1"))
    }

    @Test
    fun `get recipe instructions calls dao`() = runTest {
        val flow = flowOf<List<RecipeInstruction>>(emptyList())
        whenever(recipeDao.getRecipeInstructions("recipe-1")).thenReturn(flow)

        repository.getRecipeInstructions("recipe-1").first()

        verify(recipeDao).getRecipeInstructions(eq("recipe-1"))
    }

    @Test
    fun `delete recipe ingredients calls dao`() = runTest {
        repository.deleteRecipeIngredients("recipe-1")

        verify(recipeDao).deleteRecipeIngredients(eq("recipe-1"))
    }

    @Test
    fun `delete recipe instructions calls dao`() = runTest {
        repository.deleteRecipeInstructions("recipe-1")

        verify(recipeDao).deleteRecipeInstructions(eq("recipe-1"))
    }

    // Get all recipes tests
    @Test
    fun `get all recipes calls dao`() = runTest {
        val flow = flowOf<List<Recipe>>(emptyList())
        whenever(recipeDao.getAllRecipes()).thenReturn(flow)

        repository.getAllRecipes().first()

        verify(recipeDao).getAllRecipes()
    }

    // Edge cases
    @Test
    fun `recipe with very long name is handled`() = runTest {
        val longNameRecipe = testRecipe.copy(
            name = "这是一个非常非常非常非常非常非常非常非常非常非常长的菜谱名称"
        )
        repository.insertRecipe(longNameRecipe)

        verify(recipeDao).insertRecipe(eq(longNameRecipe))
    }

    @Test
    fun `recipe with null description is handled`() = runTest {
        val noDescRecipe = testRecipe.copy(description = null)
        repository.insertRecipe(noDescRecipe)

        verify(recipeDao).insertRecipe(eq(noDescRecipe))
    }

    @Test
    fun `recipe with null category id is handled`() = runTest {
        val noCategoryRecipe = testRecipe.copy(categoryId = null)
        repository.insertRecipe(noCategoryRecipe)

        verify(recipeDao).insertRecipe(eq(noCategoryRecipe))
    }

    @Test
    fun `recipe with zero cooking time`() = runTest {
        val zeroTimeRecipe = testRecipe.copy(cookingTime = 0)
        repository.insertRecipe(zeroTimeRecipe)

        verify(recipeDao).insertRecipe(eq(zeroTimeRecipe))
    }

    @Test
    fun `recipe with very large cooking time`() = runTest {
        val largeTimeRecipe = testRecipe.copy(cookingTime = 9999)
        repository.insertRecipe(largeTimeRecipe)

        verify(recipeDao).insertRecipe(eq(largeTimeRecipe))
    }

    @Test
    fun `recipe with single serving`() = runTest {
        val singleServingRecipe = testRecipe.copy(servings = 1)
        repository.insertRecipe(singleServingRecipe)

        verify(recipeDao).insertRecipe(eq(singleServingRecipe))
    }

    @Test
    fun `recipe with large servings count`() = runTest {
        val largeServingsRecipe = testRecipe.copy(servings = 100)
        repository.insertRecipe(largeServingsRecipe)

        verify(recipeDao).insertRecipe(eq(largeServingsRecipe))
    }

    @Test
    fun `recipe ingredient with zero quantity`() = runTest {
        val zeroQuantityIngredient = testIngredients[0].copy(quantity = 0.0)
        repository.insertRecipeWithDetails(
            testRecipe,
            listOf(zeroQuantityIngredient),
            emptyList()
        )

        verify(recipeDao).insertRecipeIngredient(eq(zeroQuantityIngredient))
    }

    @Test
    fun `recipe ingredient with very large quantity`() = runTest {
        val largeQuantityIngredient = testIngredients[0].copy(quantity = 9999.99)
        repository.insertRecipeWithDetails(
            testRecipe,
            listOf(largeQuantityIngredient),
            emptyList()
        )

        verify(recipeDao).insertRecipeIngredient(eq(largeQuantityIngredient))
    }

    @Test
    fun `recipe instruction with empty text`() = runTest {
        val emptyInstruction = testInstructions[0].copy(instructionText = "")
        repository.insertRecipeWithDetails(
            testRecipe,
            emptyList(),
            listOf(emptyInstruction)
        )

        verify(recipeDao).insertRecipeInstruction(eq(emptyInstruction))
    }

    @Test
    fun `recipe instruction with very long text`() = runTest {
        val longInstruction = testInstructions[0].copy(
            instructionText = "这是一个非常非常非常非常非常非常非常长的步骤说明，包含很多很多详细的描述内容"
        )
        repository.insertRecipeWithDetails(
            testRecipe,
            emptyList(),
            listOf(longInstruction)
        )

        verify(recipeDao).insertRecipeInstruction(eq(longInstruction))
    }

    @Test
    fun `recipe with many ingredients`() = runTest {
        val manyIngredients = (1..50).map { index ->
            RecipeIngredient(
                recipeId = testRecipe.id,
                ingredientId = "ing-$index",
                quantity = index.toDouble()
            )
        }
        repository.insertRecipeWithDetails(testRecipe, manyIngredients, emptyList())

        manyIngredients.forEach { ingredient ->
            verify(recipeDao).insertRecipeIngredient(eq(ingredient))
        }
    }

    @Test
    fun `recipe with many instructions`() = runTest {
        val manyInstructions = (1..50).map { index ->
            RecipeInstruction(
                recipeId = testRecipe.id,
                stepNumber = index,
                instructionText = "步骤$index"
            )
        }
        repository.insertRecipeWithDetails(testRecipe, emptyList(), manyInstructions)

        manyInstructions.forEach { instruction ->
            verify(recipeDao).insertRecipeInstruction(eq(instruction))
        }
    }
}
