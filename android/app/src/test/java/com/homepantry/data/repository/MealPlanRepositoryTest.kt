package com.homepantry.data.repository

import com.homepantry.data.dao.MealPlanDao
import com.homepantry.data.entity.MealPlan
import com.homepantry.data.entity.MealType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.Date

@ExperimentalCoroutinesApi
class MealPlanRepositoryTest {

    @Mock
    private lateinit var mealPlanDao: MealPlanDao

    private lateinit var repository: MealPlanRepository

    private val testMealPlan = MealPlan(
        id = "1",
        date = System.currentTimeMillis(),
        mealType = MealType.LUNCH,
        recipeId = "recipe1",
        servings = 4,
        notes = "Extra spicy"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = MealPlanRepository(mealPlanDao)
    }

    @Test
    fun `getAllMealPlans returns plans from dao`() = runTest {
        // Given
        val plans = listOf(testMealPlan)
        `when`(mealPlanDao.getAllMealPlans()).thenReturn(flowOf(plans))

        // When
        val result = repository.getAllMealPlans().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(testMealPlan.id, result[0].id)
    }

    @Test
    fun `getMealPlanById returns correct plan`() = runTest {
        // Given
        `when`(mealPlanDao.getMealPlanById("1")).thenReturn(testMealPlan)

        // When
        val result = repository.getMealPlanById("1")

        // Then
        assertNotNull(result)
        assertEquals(testMealPlan.id, result?.id)
        assertEquals(MealType.LUNCH, result?.mealType)
    }

    @Test
    fun `addMealPlan calls dao insert`() = runTest {
        // When
        repository.addMealPlan(testMealPlan)

        // Then
        verify(mealPlanDao).insertMealPlan(testMealPlan)
    }

    @Test
    fun `updateMealPlan calls dao update`() = runTest {
        // When
        repository.updateMealPlan(testMealPlan)

        // Then
        verify(mealPlanDao).updateMealPlan(testMealPlan)
    }

    @Test
    fun `deleteMealPlan calls dao delete`() = runTest {
        // When
        repository.deleteMealPlan(testMealPlan)

        // Then
        verify(mealPlanDao).deleteMealPlan(testMealPlan)
    }

    @Test
    fun `deleteMealPlanById calls dao delete`() = runTest {
        // When
        repository.deleteMealPlan("1")

        // Then
        verify(mealPlanDao).deleteMealPlanById("1")
    }

    @Test
    fun `getMealPlansForDate returns plans for specific date`() = runTest {
        // Given
        val date = System.currentTimeMillis()
        val plans = listOf(testMealPlan)
        `when`(mealPlanDao.getMealPlansForDate(date)).thenReturn(flowOf(plans))

        // When
        val result = repository.getMealPlansForDate(date).first()

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `getMealPlansForWeek returns plans for week range`() = runTest {
        // Given
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (7 * 24 * 60 * 60 * 1000)
        val plans = listOf(testMealPlan)
        `when`(mealPlanDao.getMealPlansForWeek(startTime, endTime)).thenReturn(flowOf(plans))

        // When
        val result = repository.getMealPlansForWeek(startTime, endTime).first()

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `getMealPlansForMealType returns filtered plans`() = runTest {
        // Given
        val date = System.currentTimeMillis()
        val plans = listOf(testMealPlan)
        `when`(mealPlanDao.getMealPlansForMealType(date, MealType.LUNCH)).thenReturn(flowOf(plans))

        // When
        val result = repository.getMealPlansForMealType(date, MealType.LUNCH).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(MealType.LUNCH, result[0].mealType)
    }

    @Test
    fun `deleteMealPlansForDateRange calls dao delete for range`() = runTest {
        // Given
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (7 * 24 * 60 * 60 * 1000)

        // When
        repository.deleteMealPlansForDateRange(startTime, endTime)

        // Then
        verify(mealPlanDao).deleteMealPlansForDateRange(startTime, endTime)
    }
}
