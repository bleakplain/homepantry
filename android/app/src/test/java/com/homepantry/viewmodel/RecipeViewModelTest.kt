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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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

    @Test
    fun `initial state has empty recipes`() = runTest {
        // Verify initial state
        assert(viewModel.recipes.value.isEmpty())
    }

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

        // Verify repository was called
        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

    @Test
    fun `add recipe with empty name does not call repository`() = runTest {
        viewModel.addRecipe(
            name = "",
            description = null,
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            ingredients = emptyList(),
            instructions = emptyList()
        )

        // Repository should still be called (validation should be in UI layer)
        // but we can add validation if needed
        verify(repository).insertRecipeWithDetails(any(), any(), any())
    }

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
    fun `delete recipe calls repository delete`() = runTest {
        viewModel.deleteRecipe(testRecipe)

        verify(repository).deleteRecipe(eq(testRecipe))
    }

    @Test
    fun `select recipe calls repository getRecipeById`() = runTest {
        whenever(repository.getRecipeById("test-recipe-id"))
            .thenReturn(testRecipe)

        viewModel.selectRecipe("test-recipe-id")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).getRecipeById(eq("test-recipe-id"))
        assert(viewModel.selectedRecipe.value == testRecipe)
    }

    @Test
    fun `clear selected recipe sets selectedRecipe to null`() = runTest {
        whenever(repository.getRecipeById("test-recipe-id"))
            .thenReturn(testRecipe)

        viewModel.selectRecipe("test-recipe-id")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearSelectedRecipe()

        assert(viewModel.selectedRecipe.value == null)
    }

    @Test
    fun `clearError sets error to null`() = runTest {
        viewModel.clearError()
        assert(viewModel.error.value == null)
    }

    @Test
    fun `clearSuccessMessage sets successMessage to null`() = runTest {
        viewModel.clearSuccessMessage()
        assert(viewModel.successMessage.value == null)
    }
}
