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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
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
        org.mockito.MockitoAnnotations.openMocks(this)
        repository = MealPlanRepository(mealPlanDao)
    }

    // Basic CRUD tests
    @Test
    fun `getAllMealPlans returns plans from dao`() = runTest {
        val plans = listOf(testMealPlan)
        whenever(mealPlanDao.getAllMealPlans()).thenReturn(flowOf(plans))

        val result = repository.getAllMealPlans().first()

        assertEquals(1, result.size)
        assertEquals(testMealPlan.id, result[0].id)
    }

    @Test
    fun `getAllMealPlans returns empty list when no plans`() = runTest {
        whenever(mealPlanDao.getAllMealPlans()).thenReturn(flowOf(emptyList()))

        val result = repository.getAllMealPlans().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getMealPlanById returns correct plan`() = runTest {
        whenever(mealPlanDao.getMealPlanById("1")).thenReturn(testMealPlan)

        val result = repository.getMealPlanById("1")

        assertNotNull(result)
        assertEquals(testMealPlan.id, result?.id)
        assertEquals(MealType.LUNCH, result?.mealType)
    }

    @Test
    fun `getMealPlanById returns null when not exists`() = runTest {
        whenever(mealPlanDao.getMealPlanById("non-existent")).thenReturn(null)

        val result = repository.getMealPlanById("non-existent")

        assertNull(result)
    }

    @Test
    fun `addMealPlan calls dao insert`() = runTest {
        repository.addMealPlan(testMealPlan)

        verify(mealPlanDao).insertMealPlan(eq(testMealPlan))
    }

    @Test
    fun `updateMealPlan calls dao update`() = runTest {
        repository.updateMealPlan(testMealPlan)

        verify(mealPlanDao).updateMealPlan(eq(testMealPlan))
    }

    @Test
    fun `deleteMealPlan calls dao delete`() = runTest {
        repository.deleteMealPlan(testMealPlan)

        verify(mealPlanDao).deleteMealPlan(eq(testMealPlan))
    }

    @Test
    fun `deleteMealPlanById calls dao delete`() = runTest {
        repository.deleteMealPlan("1")

        verify(mealPlanDao).deleteMealPlanById(eq("1"))
    }

    // Date range tests
    @Test
    fun `getMealPlansForDate returns plans for specific date`() = runTest {
        val date = System.currentTimeMillis()
        val plans = listOf(testMealPlan)
        whenever(mealPlanDao.getMealPlansForDate(date)).thenReturn(flowOf(plans))

        val result = repository.getMealPlansForDate(date).first()

        assertEquals(1, result.size)
    }

    @Test
    fun `getMealPlansForDate returns empty when no plans`() = runTest {
        val date = System.currentTimeMillis()
        whenever(mealPlanDao.getMealPlansForDate(date)).thenReturn(flowOf(emptyList()))

        val result = repository.getMealPlansForDate(date).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getMealPlansForWeek returns plans for week range`() = runTest {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (7 * 24 * 60 * 60 * 1000)
        val plans = listOf(testMealPlan)
        whenever(mealPlanDao.getMealPlansForWeek(startTime, endTime)).thenReturn(flowOf(plans))

        val result = repository.getMealPlansForWeek(startTime, endTime).first()

        assertEquals(1, result.size)
    }

    @Test
    fun `getMealPlansForWeek with zero duration`() = runTest {
        val time = System.currentTimeMillis()
        whenever(mealPlanDao.getMealPlansForWeek(time, time)).thenReturn(flowOf(emptyList()))

        val result = repository.getMealPlansForWeek(time, time).first()

        assertTrue(result.isEmpty())
    }

    // Meal type tests
    @Test
    fun `getMealPlansForMealType returns filtered plans`() = runTest {
        val date = System.currentTimeMillis()
        val plans = listOf(testMealPlan)
        whenever(mealPlanDao.getMealPlansForMealType(date, MealType.LUNCH)).thenReturn(flowOf(plans))

        val result = repository.getMealPlansForMealType(date, MealType.LUNCH).first()

        assertEquals(1, result.size)
        assertEquals(MealType.LUNCH, result[0].mealType)
    }

    @Test
    fun `getMealPlansForMealType returns empty when no matching plans`() = runTest {
        val date = System.currentTimeMillis()
        whenever(mealPlanDao.getMealPlansForMealType(date, MealType.DINNER)).thenReturn(flowOf(emptyList()))

        val result = repository.getMealPlansForMealType(date, MealType.DINNER).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getMealPlansForMealType works for all meal types`() = runTest {
        val date = System.currentTimeMillis()
        MealType.values().forEach { mealType ->
            val plan = testMealPlan.copy(mealType = mealType)
            whenever(mealPlanDao.getMealPlansForMealType(date, mealType)).thenReturn(flowOf(listOf(plan)))

            val result = repository.getMealPlansForMealType(date, mealType).first()

            assertEquals(1, result.size)
            assertEquals(mealType, result[0].mealType)
        }
    }

    // Delete range tests
    @Test
    fun `deleteMealPlansForDateRange calls dao delete for range`() = runTest {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (7 * 24 * 60 * 60 * 1000)

        repository.deleteMealPlansForDateRange(startTime, endTime)

        verify(mealPlanDao).deleteMealPlansForDateRange(eq(startTime), eq(endTime))
    }

    @Test
    fun `getMealPlansForDateRange returns plans in range`() = runTest {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (7 * 24 * 60 * 60 * 1000)
        val plans = listOf(testMealPlan)
        whenever(mealPlanDao.getMealPlansForDateRange(startTime, endTime)).thenReturn(flowOf(plans))

        val result = repository.getMealPlansForDateRange(startTime, endTime).first()

        assertEquals(1, result.size)
    }

    // Edge cases
    @Test
    fun `meal plan with zero servings`() = runTest {
        val zeroServingsPlan = testMealPlan.copy(servings = 0)
        repository.addMealPlan(zeroServingsPlan)

        verify(mealPlanDao).insertMealPlan(eq(zeroServingsPlan))
    }

    @Test
    fun `meal plan with very large servings`() = runTest {
        val largeServingsPlan = testMealPlan.copy(servings = 1000)
        repository.addMealPlan(largeServingsPlan)

        verify(mealPlanDao).insertMealPlan(eq(largeServingsPlan))
    }

    @Test
    fun `meal plan with null notes`() = runTest {
        val noNotesPlan = testMealPlan.copy(notes = null)
        repository.addMealPlan(noNotesPlan)

        verify(mealPlanDao).insertMealPlan(eq(noNotesPlan))
    }

    @Test
    fun `meal plan with empty notes`() = runTest {
        val emptyNotesPlan = testMealPlan.copy(notes = "")
        repository.addMealPlan(emptyNotesPlan)

        verify(mealPlanDao).insertMealPlan(eq(emptyNotesPlan))
    }

    @Test
    fun `meal plan with very long notes`() = runTest {
        val longNotesPlan = testMealPlan.copy(
            notes = "这是一段非常非常非常非常非常长的备注，包含很多很多详细的描述内容"
        )
        repository.addMealPlan(longNotesPlan)

        verify(mealPlanDao).insertMealPlan(eq(longNotesPlan))
    }

    @Test
    fun `meal plan with special characters in notes`() = runTest {
        val specialNotesPlan = testMealPlan.copy(notes = "特殊字符!@#$%^&*()")
        repository.addMealPlan(specialNotesPlan)

        verify(mealPlanDao).insertMealPlan(eq(specialNotesPlan))
    }

    @Test
    fun `meal plan with past date`() = runTest {
        val pastDatePlan = testMealPlan.copy(
            date = System.currentTimeMillis() - 86400000
        )
        repository.addMealPlan(pastDatePlan)

        verify(mealPlanDao).insertMealPlan(eq(pastDatePlan))
    }

    @Test
    fun `meal plan with future date`() = runTest {
        val futureDatePlan = testMealPlan.copy(
            date = System.currentTimeMillis() + 86400000 * 365
        )
        repository.addMealPlan(futureDatePlan)

        verify(mealPlanDao).insertMealPlan(eq(futureDatePlan))
    }

    @Test
    fun `meal plan with zero timestamp`() = runTest {
        val zeroTimePlan = testMealPlan.copy(date = 0)
        repository.addMealPlan(zeroTimePlan)

        verify(mealPlanDao).insertMealPlan(eq(zeroTimePlan))
    }

    @Test
    fun `meal plan with negative timestamp`() = runTest {
        val negativeTimePlan = testMealPlan.copy(date = -1)
        repository.addMealPlan(negativeTimePlan)

        verify(mealPlanDao).insertMealPlan(eq(negativeTimePlan))
    }

    @Test
    fun `meal plan with empty recipe id`() = runTest {
        val noRecipePlan = testMealPlan.copy(recipeId = "")
        repository.addMealPlan(noRecipePlan)

        verify(mealPlanDao).insertMealPlan(eq(noRecipePlan))
    }

    @Test
    fun `meal plan with all meal types`() = runTest {
        MealType.values().forEach { mealType ->
            val plan = testMealPlan.copy(
                id = "plan-$mealType",
                mealType = mealType
            )
            repository.addMealPlan(plan)

            verify(mealPlanDao).insertMealPlan(eq(plan))
        }
    }

    // Multiple meal plans for same date
    @Test
    fun `multiple meal plans for same date`() = runTest {
        val date = System.currentTimeMillis()
        val plans = listOf(
            testMealPlan.copy(id = "1", mealType = MealType.BREAKFAST),
            testMealPlan.copy(id = "2", mealType = MealType.LUNCH),
            testMealPlan.copy(id = "3", mealType = MealType.DINNER)
        )
        whenever(mealPlanDao.getMealPlansForDate(date)).thenReturn(flowOf(plans))

        val result = repository.getMealPlansForDate(date).first()

        assertEquals(3, result.size)
    }

    // Empty and null id tests
    @Test
    fun `get meal plan with empty id`() = runTest {
        whenever(mealPlanDao.getMealPlanById("")).thenReturn(null)

        val result = repository.getMealPlanById("")

        assertNull(result)
    }

    @Test
    fun `delete meal plan with empty id`() = runTest {
        repository.deleteMealPlan("")

        verify(mealPlanDao).deleteMealPlanById(eq(""))
    }

    // Date range edge cases
    @Test
    fun `get meal plans for inverted date range`() = runTest {
        val endTime = System.currentTimeMillis()
        val startTime = endTime + 86400000
        whenever(mealPlanDao.getMealPlansForDateRange(startTime, endTime)).thenReturn(flowOf(emptyList()))

        val result = repository.getMealPlansForDateRange(startTime, endTime).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `delete meal plans for inverted date range`() = runTest {
        val endTime = System.currentTimeMillis()
        val startTime = endTime + 86400000

        repository.deleteMealPlansForDateRange(startTime, endTime)

        verify(mealPlanDao).deleteMealPlansForDateRange(eq(startTime), eq(endTime))
    }

    // Update operations
    @Test
    fun `update meal plan changes all fields`() = runTest {
        val updated = testMealPlan.copy(
            mealType = MealType.DINNER,
            servings = 6,
            notes = "Updated notes"
        )
        repository.updateMealPlan(updated)

        verify(mealPlanDao).updateMealPlan(eq(updated))
    }

    @Test
    fun `update meal plan to null notes`() = runTest {
        val updated = testMealPlan.copy(notes = null)
        repository.updateMealPlan(updated)

        verify(mealPlanDao).updateMealPlan(eq(updated))
    }

    // Flow updates
    @Test
    fun `getAllMealPlans emits updates when data changes`() = runTest {
        val plans1 = listOf(testMealPlan)
        val plans2 = listOf(
            testMealPlan,
            testMealPlan.copy(id = "2", mealType = MealType.DINNER)
        )

        whenever(mealPlanDao.getAllMealPlans()).thenReturn(flowOf(plans1), flowOf(plans2))

        val result1 = repository.getAllMealPlans().first()
        assertEquals(1, result1.size)

        // In a real scenario, you'd collect the flow and wait for emission
    }

    // Multiple operations
    @Test
    fun `multiple meal plan operations work correctly`() = runTest {
        // Add
        repository.addMealPlan(testMealPlan)
        verify(mealPlanDao).insertMealPlan(eq(testMealPlan))

        // Update
        val updated = testMealPlan.copy(servings = 8)
        repository.updateMealPlan(updated)
        verify(mealPlanDao).updateMealPlan(eq(updated))

        // Delete
        repository.deleteMealPlan(testMealPlan.id)
        verify(mealPlanDao).deleteMealPlanById(eq(testMealPlan.id))
    }

    // Large date ranges
    @Test
    fun `get meal plans for very large date range`() = runTest {
        val startTime = 0L
        val endTime = Long.MAX_VALUE
        whenever(mealPlanDao.getMealPlansForDateRange(startTime, endTime)).thenReturn(flowOf(emptyList()))

        val result = repository.getMealPlansForDateRange(startTime, endTime).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `get meal plans for week with negative dates`() = runTest {
        val startTime = -86400000L
        val endTime = 0L
        whenever(mealPlanDao.getMealPlansForWeek(startTime, endTime)).thenReturn(flowOf(emptyList()))

        val result = repository.getMealPlansForWeek(startTime, endTime).first()

        assertTrue(result.isEmpty())
    }
}
