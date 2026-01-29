package com.homepantry.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: RecipeRepository

    private lateinit var viewModel: RecipeViewModel

    private val testRecipe = Recipe(
        id = "test-recipe-id",
        name = "番茄炒蛋",
        description = "经典家常菜",
        cookingTime = 15,
        servings = 2,
        difficulty = DifficultyLevel.EASY
    )

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = RecipeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Initial state tests
    @Test
    fun `initial state has empty recipes`() = runTest {
        assertTrue(viewModel.recipes.value.isEmpty())
    }

    @Test
    fun `initial state has null selected recipe`() = runTest {
        assertNull(viewModel.selectedRecipe.value)
    }

    @Test
    fun `initial state has null error`() = runTest {
        assertNull(viewModel.error.value)
    }

    @Test
    fun `initial state has null success message`() = runTest {
        assertNull(viewModel.successMessage.value)
    }

    @Test
    fun `initial state has false loading state`() = runTest {
        assertFalse(viewModel.isLoading.value)
    }

    // Add recipe tests
    @Test
    fun `add recipe calls repository insert`() = runTest {
        val ingredients = listOf(
            RecipeIngredient("recipe-id", "tomato", 2.0)
        )
        val instructions = listOf(
            RecipeInstruction("recipe-id", 1, "Cut tomatoes")
        )

        viewModel.addRecipe(
            name = "番茄炒蛋",
            description = "测试",
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            ingredients = ingredients,
            instructions = instructions
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with empty name calls repository`() = runTest {
        viewModel.addRecipe(
            name = "",
            description = null,
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with very long name`() = runTest {
        val longName = "这是一个非常非常非常非常非常长的菜谱名称" +
                "这是一个非常非常非常非常非常长的菜谱名称"

        viewModel.addRecipe(
            name = longName,
            description = null,
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with zero cooking time`() = runTest {
        viewModel.addRecipe(
            name = "测试菜谱",
            description = null,
            cookingTime = 0,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with zero servings`() = runTest {
        viewModel.addRecipe(
            name = "测试菜谱",
            description = null,
            cookingTime = 15,
            servings = 0,
            difficulty = DifficultyLevel.EASY,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with very large servings`() = runTest {
        viewModel.addRecipe(
            name = "测试菜谱",
            description = null,
            cookingTime = 15,
            servings = 1000,
            difficulty = DifficultyLevel.EASY,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with all difficulty levels`() = runTest {
        DifficultyLevel.values().forEach { difficulty ->
            viewModel.addRecipe(
                name = "测试菜谱 $difficulty",
                description = null,
                cookingTime = 15,
                servings = 2,
                difficulty = difficulty,
                ingredients = emptyList(),
                instructions = emptyList()
            )
        }

        DifficultyLevel.values().forEach {
            verify(repository).insertRecipeWithDetails(any(), any(), any())
        }
    }

    @Test
    fun `add recipe with many ingredients`() = runTest {
        val ingredients = (1..50).map { index ->
            RecipeIngredient("recipe-id", "ing-$index", index.toDouble())
        }

        viewModel.addRecipe(
            name = "复杂菜谱",
            description = null,
            cookingTime = 60,
            servings = 10,
            difficulty = DifficultyLevel.HARD,
            ingredients = ingredients,
            instructions = emptyList()
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with many instructions`() = runTest {
        val instructions = (1..50).map { index ->
            RecipeInstruction("recipe-id", index, "步骤$index")
        }

        viewModel.addRecipe(
            name = "复杂菜谱",
            description = null,
            cookingTime = 60,
            servings = 10,
            difficulty = DifficultyLevel.HARD,
            ingredients = emptyList(),
            instructions = instructions
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with ingredient having zero quantity`() = runTest {
        val ingredients = listOf(
            RecipeIngredient("recipe-id", "salt", 0.0)
        )

        viewModel.addRecipe(
            name = "测试菜谱",
            description = null,
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            ingredients = ingredients,
            instructions = emptyList()
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with instruction having empty text`() = runTest {
        val instructions = listOf(
            RecipeInstruction("recipe-id", 1, "")
        )

        viewModel.addRecipe(
            name = "测试菜谱",
            description = null,
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            ingredients = emptyList(),
            instructions = instructions
        )

        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    // Update recipe tests
    @Test
    fun `update recipe calls repository update`() = runTest {
        viewModel.updateRecipe(
            recipe = testRecipe,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        verify(repository).updateRecipeWithDetails(eq(testRecipe), any(), any())
    }

    @Test
    fun `update recipe with empty ingredients list still deletes existing`() = runTest {
        viewModel.updateRecipe(
            recipe = testRecipe,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        verify(repository).updateRecipeWithDetails(eq(testRecipe), eq(emptyList()), eq(emptyList()))
    }

    @Test
    fun `update recipe with empty instructions list still deletes existing`() = runTest {
        viewModel.updateRecipe(
            recipe = testRecipe,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        verify(repository).updateRecipeWithDetails(eq(testRecipe), eq(emptyList()), eq(emptyList()))
    }

    // Delete recipe tests
    @Test
    fun `delete recipe calls repository delete`() = runTest {
        viewModel.deleteRecipe(testRecipe)

        verify(repository).deleteRecipe(eq(testRecipe))
    }

    @Test
    fun `delete recipe with empty id`() = runTest {
        val emptyIdRecipe = testRecipe.copy(id = "")

        viewModel.deleteRecipe(emptyIdRecipe)

        verify(repository).deleteRecipe(eq(emptyIdRecipe))
    }

    // Select recipe tests
    @Test
    fun `select recipe calls repository getRecipeById`() = runTest {
        whenever(repository.getRecipeById("test-recipe-id"))
            .thenReturn(testRecipe)

        viewModel.selectRecipe("test-recipe-id")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).getRecipeById(eq("test-recipe-id"))
        assertEquals(testRecipe, viewModel.selectedRecipe.value)
    }

    @Test
    fun `select recipe with empty id returns null`() = runTest {
        whenever(repository.getRecipeById("")).thenReturn(null)

        viewModel.selectRecipe("")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).getRecipeById(eq(""))
        assertNull(viewModel.selectedRecipe.value)
    }

    @Test
    fun `select recipe with non-existent id returns null`() = runTest {
        whenever(repository.getRecipeById("non-existent")).thenReturn(null)

        viewModel.selectRecipe("non-existent")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).getRecipeById(eq("non-existent"))
        assertNull(viewModel.selectedRecipe.value)
    }

    @Test
    fun `select multiple recipes sequentially`() = runTest {
        val recipe1 = testRecipe.copy(id = "1", name = "菜谱1")
        val recipe2 = testRecipe.copy(id = "2", name = "菜谱2")
        val recipe3 = testRecipe.copy(id = "3", name = "菜谱3")

        whenever(repository.getRecipeById("1")).thenReturn(recipe1)
        whenever(repository.getRecipeById("2")).thenReturn(recipe2)
        whenever(repository.getRecipeById("3")).thenReturn(recipe3)

        viewModel.selectRecipe("1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(recipe1, viewModel.selectedRecipe.value)

        viewModel.selectRecipe("2")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(recipe2, viewModel.selectedRecipe.value)

        viewModel.selectRecipe("3")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(recipe3, viewModel.selectedRecipe.value)
    }

    // Clear selected recipe tests
    @Test
    fun `clear selected recipe sets selectedRecipe to null`() = runTest {
        whenever(repository.getRecipeById("test-recipe-id"))
            .thenReturn(testRecipe)

        viewModel.selectRecipe("test-recipe-id")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearSelectedRecipe()

        assertNull(viewModel.selectedRecipe.value)
    }

    @Test
    fun `clear selected recipe when already null`() = runTest {
        viewModel.clearSelectedRecipe()

        assertNull(viewModel.selectedRecipe.value)
    }

    // Clear error tests
    @Test
    fun `clearError sets error to null`() = runTest {
        viewModel.clearError()
        assertNull(viewModel.error.value)
    }

    @Test
    fun `clearError when already null`() = runTest {
        viewModel.clearError()
        viewModel.clearError()

        assertNull(viewModel.error.value)
    }

    // Clear success message tests
    @Test
    fun `clearSuccessMessage sets successMessage to null`() = runTest {
        viewModel.clearSuccessMessage()
        assertNull(viewModel.successMessage.value)
    }

    @Test
    fun `clearSuccessMessage when already null`() = runTest {
        viewModel.clearSuccessMessage()
        viewModel.clearSuccessMessage()

        assertNull(viewModel.successMessage.value)
    }

    // Loading state tests
    @Test
    fun `loading state is false after initialization`() = runTest {
        assertFalse(viewModel.isLoading.value)
    }

    // Search tests
    @Test
    fun `search recipes calls repository searchRecipes`() = runTest {
        val query = "番茄"

        viewModel.searchRecipes(query)

        verify(repository).searchRecipes(eq(query))
    }

    @Test
    fun `search recipes with empty query`() = runTest {
        viewModel.searchRecipes("")

        verify(repository).searchRecipes(eq(""))
    }

    @Test
    fun `search recipes with special characters`() = runTest {
        val query = "特殊字符!@#"

        viewModel.searchRecipes(query)

        verify(repository).searchRecipes(eq(query))
    }

    @Test
    fun `search recipes with very long query`() = runTest {
        val longQuery = "番茄" + "a".repeat(1000)

        viewModel.searchRecipes(longQuery)

        verify(repository).searchRecipes(eq(longQuery))
    }

    // Filter tests
    @Test
    fun `get recipes by difficulty calls repository`() = runTest {
        viewModel.getRecipesByDifficulty(DifficultyLevel.EASY)

        verify(repository).getRecipesByDifficulty(eq(DifficultyLevel.EASY))
    }

    @Test
    fun `get recipes by difficulty for all levels`() = runTest {
        DifficultyLevel.values().forEach { difficulty ->
            viewModel.getRecipesByDifficulty(difficulty)
        }

        DifficultyLevel.values().forEach { difficulty ->
            verify(repository).getRecipesByDifficulty(eq(difficulty))
        }
    }

    @Test
    fun `get recipes with max cooking time calls repository`() = runTest {
        viewModel.getRecipesWithMaxCookingTime(30)

        verify(repository).getRecipesWithMaxCookingTime(eq(30))
    }

    @Test
    fun `get recipes with zero max cooking time`() = runTest {
        viewModel.getRecipesWithMaxCookingTime(0)

        verify(repository).getRecipesWithMaxCookingTime(eq(0))
    }

    @Test
    fun `get recipes by servings calls repository`() = runTest {
        viewModel.getRecipesByServings(minServings = 2)

        verify(repository).getRecipesByServings(eq(2), eq(null))
    }

    @Test
    fun `get recipes by servings with range`() = runTest {
        viewModel.getRecipesByServings(minServings = 2, maxServings = 4)

        verify(repository).getRecipesByServings(eq(2), eq(4))
    }

    @Test
    fun `get recipes by servings with min greater than max`() = runTest {
        viewModel.getRecipesByServings(minServings = 10, maxServings = 2)

        verify(repository).getRecipesByServings(eq(10), eq(2))
    }

    // Favorite tests
    @Test
    fun `toggle favorite calls repository updateFavoriteStatus`() = runTest {
        viewModel.toggleFavorite("recipe-1", true)

        verify(repository).updateFavoriteStatus(eq("recipe-1"), eq(true))
    }

    @Test
    fun `toggle favorite from false to true`() = runTest {
        viewModel.toggleFavorite("recipe-1", true)

        verify(repository).updateFavoriteStatus(eq("recipe-1"), eq(true))
    }

    @Test
    fun `toggle favorite from true to false`() = runTest {
        viewModel.toggleFavorite("recipe-1", false)

        verify(repository).updateFavoriteStatus(eq("recipe-1"), eq(false))
    }

    @Test
    fun `get favorite recipes calls repository`() = runTest {
        viewModel.getFavoriteRecipes()

        verify(repository).getFavoriteRecipes()
    }

    // Sorting tests
    @Test
    fun `sort recipes by name ascending calls repository`() = runTest {
        viewModel.sortRecipesByNameAsc()

        verify(repository).getRecipesByNameAsc()
    }

    @Test
    fun `sort recipes by name descending calls repository`() = runTest {
        viewModel.sortRecipesByNameDesc()

        verify(repository).getRecipesByNameDesc()
    }

    @Test
    fun `sort recipes by cooking time ascending calls repository`() = runTest {
        viewModel.sortRecipesByCookingTimeAsc()

        verify(repository).getRecipesByCookingTimeAsc()
    }

    @Test
    fun `sort recipes by difficulty ascending calls repository`() = runTest {
        viewModel.sortRecipesByDifficultyAsc()

        verify(repository).getRecipesByDifficultyAsc()
    }

    @Test
    fun `sort recipes by newest first calls repository`() = runTest {
        viewModel.sortRecipesByNewestFirst()

        verify(repository).getRecipesByNewestFirst()
    }

    // Category tests
    @Test
    fun `get recipes by category calls repository`() = runTest {
        viewModel.getRecipesByCategory("cat-1")

        verify(repository).getRecipesByCategory(eq("cat-1"))
    }

    @Test
    fun `get recipes by empty category id`() = runTest {
        viewModel.getRecipesByCategory("")

        verify(repository).getRecipesByCategory(eq(""))
    }

    // All recipes tests
    @Test
    fun `get all recipes calls repository`() = runTest {
        whenever(repository.getAllRecipes()).thenReturn(flowOf(emptyList()))

        viewModel.loadAllRecipes()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).getAllRecipes()
    }

    @Test
    fun `get all recipes with multiple recipes`() = runTest {
        val recipes = (1..10).map { index ->
            testRecipe.copy(id = "r$index", name = "菜谱$index")
        }
        whenever(repository.getAllRecipes()).thenReturn(flowOf(recipes))

        viewModel.loadAllRecipes()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).getAllRecipes()
    }

    // Multiple operations
    @Test
    fun `add then update then delete recipe`() = runTest {
        // Add
        viewModel.addRecipe(
            name = "测试菜谱",
            description = null,
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            ingredients = emptyList(),
            instructions = emptyList()
        )
        verify(repository).insertRecipeWithDetails(any(), any(), any())

        // Update
        viewModel.updateRecipe(
            recipe = testRecipe,
            ingredients = emptyList(),
            instructions = emptyList()
        )
        verify(repository).updateRecipeWithDetails(eq(testRecipe), any(), any())

        // Delete
        viewModel.deleteRecipe(testRecipe)
        verify(repository).deleteRecipe(eq(testRecipe))
    }

    // State consistency tests
    @Test
    fun `selecting new recipe replaces previous selection`() = runTest {
        val recipe1 = testRecipe.copy(id = "1", name = "菜谱1")
        val recipe2 = testRecipe.copy(id = "2", name = "菜谱2")

        whenever(repository.getRecipeById("1")).thenReturn(recipe1)
        whenever(repository.getRecipeById("2")).thenReturn(recipe2)

        viewModel.selectRecipe("1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("菜谱1", viewModel.selectedRecipe.value?.name)

        viewModel.selectRecipe("2")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("菜谱2", viewModel.selectedRecipe.value?.name)
    }

    @Test
    fun `clearing error does not affect selected recipe`() = runTest {
        whenever(repository.getRecipeById("test-recipe-id")).thenReturn(testRecipe)

        viewModel.selectRecipe("test-recipe-id")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearError()

        assertEquals(testRecipe, viewModel.selectedRecipe.value)
    }

    @Test
    fun `clearing success message does not affect selected recipe`() = runTest {
        whenever(repository.getRecipeById("test-recipe-id")).thenReturn(testRecipe)

        viewModel.selectRecipe("test-recipe-id")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearSuccessMessage()

        assertEquals(testRecipe, viewModel.selectedRecipe.value)
    }
}
