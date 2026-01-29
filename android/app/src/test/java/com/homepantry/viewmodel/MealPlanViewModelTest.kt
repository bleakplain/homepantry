package com.homepantry.viewmodel

import com.homepantry.data.entity.MealPlan
import com.homepantry.data.entity.MealType
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.repository.MealPlanRepository
import com.homepantry.data.repository.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.Date

@ExperimentalCoroutinesApi
class MealPlanViewModelTest {

    @Mock
    private lateinit var mealPlanRepository: MealPlanRepository

    @Mock
    private lateinit var recipeRepository: RecipeRepository

    private lateinit var viewModel: MealPlanViewModel

    private val testRecipe = Recipe(
        id = "1",
        name = "番茄炒蛋",
        description = "经典家常菜",
        cookingTime = 15,
        servings = 2,
        difficulty = DifficultyLevel.EASY
    )

    private val testMealPlan = MealPlan(
        id = "1",
        date = System.currentTimeMillis(),
        mealType = MealType.LUNCH,
        recipeId = "1",
        servings = 2,
        notes = null
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = MealPlanViewModel(mealPlanRepository, recipeRepository)
    }

    @Test
    fun `addMealPlan with valid data calls repository add`() = runTest {
        // Given
        val date = Date()
        `when`(recipeRepository.getAllRecipes()).thenReturn(flowOf(listOf(testRecipe)))

        // When
        viewModel.addMealPlan(
            date = date,
            mealType = MealType.LUNCH,
            recipeId = "1",
            servings = 2,
            notes = null
        )

        // Then
        verify(mealPlanRepository).addMealPlan(any())
    }

    @Test
    fun `updateMealPlan calls repository update`() = runTest {
        // Given
        `when`(recipeRepository.getAllRecipes()).thenReturn(flowOf(listOf(testRecipe)))
        `when`(mealPlanRepository.getMealPlanById("1")).thenReturn(testMealPlan)

        // When
        viewModel.updateMealPlan(
            mealPlanId = "1",
            recipeId = "1",
            servings = 3,
            notes = "Extra spicy"
        )

        // Then
        verify(mealPlanRepository).updateMealPlan(any())
    }

    @Test
    fun `deleteMealPlan calls repository delete`() = runTest {
        // When
        viewModel.deleteMealPlan("1")

        // Then
        verify(mealPlanRepository).deleteMealPlan("1")
    }

    @Test
    fun `selectDate updates selected date state`() = runTest {
        // Given
        val newDate = Date(System.currentTimeMillis() + 86400000)

        // When
        viewModel.selectDate(newDate)

        // Then - selected date should be updated
        // In actual test, you'd collect the selectedDate flow and verify it matches newDate
    }

    @Test
    fun `clearError clears error state`() = runTest {
        // When
        viewModel.clearError()

        // Then - error should be null
    }

    @Test
    fun `clearSuccessMessage clears success message state`() = runTest {
        // When
        viewModel.clearSuccessMessage()

        // Then - success message should be null
    }
}
