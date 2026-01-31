package com.homepantry.data.nutrition

import com.homepantry.data.dao.NutritionInfoDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.MealPlan
import com.homepantry.data.entity.MealType
import com.homepantry.data.entity.NutritionInfo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 营养分析器测试
 */
class NutritionAnalyzerTest {

    private lateinit var recipeDao: RecipeDao
    private lateinit var nutritionInfoDao: NutritionInfoDao
    private lateinit var analyzer: NutritionAnalyzer

    private val testNutritionInfo = NutritionInfo(
        id = "nutri1",
        recipeId = "r1",
        calories = 400,
        protein = 25.0,
        fat = 15.0,
        carbs = 50.0,
        fiber = 5.0,
        sodium = 800,
        servingSize = 1
    )

    @Before
    fun setup() {
        recipeDao = mockk()
        nutritionInfoDao = mockk()
        analyzer = NutritionAnalyzer(recipeDao, nutritionInfoDao)

        every { nutritionInfoDao.getNutritionByRecipe("r1") }.returns(testNutritionInfo)
        every { nutritionInfoDao.getNutritionByRecipe("r2") }.returns(
            NutritionInfo("nutri2", "r2", 600, 30.0, 20.0, 60.0, 8.0, 1000, 1)
        )
        every { nutritionInfoDao.getNutritionByRecipe("r3") }.returns(
            NutritionInfo("nutri3", "r3", 300, 15.0, 10.0, 40.0, 3.0, 500, 1)
        )
    }

    @Test
    fun `analyze daily nutrition sums correctly`() = runTest {
        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.BREAKFAST, "r1", 1),
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r2", 1),
            MealPlan(System.currentTimeMillis(), MealType.DINNER, "r3", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)

        assertEquals(1300, report.totalNutrition.calories) // 400 + 600 + 300
        assertEquals(70.0, report.totalNutrition.protein, 0.01) // 25 + 30 + 15
        assertEquals(45.0, report.totalNutrition.fat, 0.01) // 15 + 20 + 10
        assertEquals(150.0, report.totalNutrition.carbs, 0.01) // 50 + 60 + 40
    }

    @Test
    fun `analyze daily nutrition includes meal breakdown`() = runTest {
        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 2) // 2人份
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)

        assertTrue(report.mealNutrition.containsKey(MealType.LUNCH))
        val lunchNutrition = report.mealNutrition[MealType.LUNCH]!!

        assertEquals(800, lunchNutrition.calories) // 400 * 2
        assertEquals(50.0, lunchNutrition.protein, 0.01) // 25 * 2
    }

    @Test
    fun `nutrition assessment provides score`() = runTest {
        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.BREAKFAST, "r1", 1),
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r2", 1),
            MealPlan(System.currentTimeMillis(), MealType.DINNER, "r3", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)

        assertNotNull(report.assessment)
        assertTrue(report.assessment.score >= 0)
        assertTrue(report.assessment.score <= 100)
    }

    @Test
    fun `assessment detects low protein`() = runTest {
        val lowProteinNutrition = NutritionInfo(
            id = "low",
            recipeId = "low",
            calories = 2000,
            protein = 20.0,
            fat = 50.0,
            carbs = 300.0,
            fiber = 20.0,
            sodium = 2000,
            servingSize = 1
        )

        every { nutritionInfoDao.getNutritionByRecipe(any()) }.returns(lowProteinNutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "low", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)

        assertTrue(report.assessment.issues.any { it.contains("蛋白质") })
    }

    @Test
    fun `assessment detects high sodium`() = runTest {
        val highSodiumNutrition = NutritionInfo(
            id = "high",
            recipeId = "high",
            calories = 1500,
            protein = 60.0,
            fat = 40.0,
            carbs = 200.0,
            fiber = 20.0,
            sodium = 3000,
            servingSize = 1
        )

        every { nutritionInfoDao.getNutritionByRecipe(any()) }.returns(highSodiumNutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "high", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)

        assertTrue(report.assessment.warnings.any { it.contains("钠") || it.contains("盐") })
    }

    @Test
    fun `weekly report calculates averages`() = runTest {
        val day1 = System.currentTimeMillis()
        val day2 = day1 + 86400000

        val weeklyPlans = mapOf(
            day1 to listOf(
                MealPlan(day1, MealType.LUNCH, "r1", 1),
                MealPlan(day1, MealType.DINNER, "r2", 1)
            ),
            day2 to listOf(
                MealPlan(day2, MealType.LUNCH, "r3", 1),
                MealPlan(day2, MealType.DINNER, "r1", 1)
            )
        )

        val report = analyzer.analyzeWeeklyNutrition(weeklyPlans)

        assertEquals(2, report.dailyReports.size)
        assertTrue(report.averageNutrition.avgCalories > 0)
        assertTrue(report.averageNutrition.avgProtein > 0)
    }

    @Test
    fun `get nutrition for recipe returns scaled values`() = runTest {
        val nutrition = analyzer.getNutritionForRecipe("r1", servings = 2)

        assertEquals(800, nutrition.calories) // 400 * 2
        assertEquals(50.0, nutrition.protein, 0.01) // 25 * 2
        assertEquals(30.0, nutrition.fat, 0.01) // 15 * 2
    }

    @Test
    fun `get nutrition for recipe without info uses defaults`() = runTest {
        every { nutritionInfoDao.getNutritionByRecipe("unknown") }.returns(null)

        val nutrition = analyzer.getNutritionForRecipe("unknown", servings = 1)

        assertEquals(400, nutrition.calories) // 默认值
        assertEquals(20.0, nutrition.protein, 0.01)
    }

    @Test
    fun `compare to recommendations calculates differences`() = runTest {
        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 1)
        )

        val comparison = analyzer.compareToRecommendations(
            mealPlans = mealPlans,
            gender = NutritionAnalyzer.Gender.FEMALE,
            age = 30,
            activityLevel = NutritionAnalyzer.ActivityLevel.MODERATE
        )

        assertNotNull(comparison.recommended)
        assertNotNull(comparison.differences)
    }

    @Test
    fun `nutrition advice for weight loss checks calories`() = runTest {
        val highCalorieNutrition = NutritionInfo(
            id = "high",
            recipeId = "high",
            calories = 800,
            protein = 40.0,
            fat = 30.0,
            carbs = 100.0,
            fiber = 10.0,
            sodium = 1200,
            servingSize = 1
        )

        every { nutritionInfoDao.getNutritionByRecipe(any()) }.returns(highCalorieNutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "high", 1),
            MealPlan(System.currentTimeMillis(), MealType.DINNER, "high", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)
        val advice = analyzer.getNutritionAdvice(report, NutritionAnalyzer.HealthGoal.WEIGHT_LOSS)

        assertTrue(advice.any { it.category == "热量" })
    }

    @Test
    fun `nutrition advice for muscle gain checks protein`() = runTest {
        val lowProteinNutrition = NutritionInfo(
            id = "low",
            recipeId = "low",
            calories = 500,
            protein = 15.0,
            fat = 20.0,
            carbs = 60.0,
            fiber = 5.0,
            sodium = 600,
            servingSize = 1
        )

        every { nutritionInfoDao.getNutritionByRecipe(any()) }.returns(lowProteinNutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "low", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)
        val advice = analyzer.getNutritionAdvice(report, NutritionAnalyzer.HealthGoal.MUSCLE_GAIN)

        assertTrue(advice.any { it.category == "蛋白质" })
    }

    @Test
    fun `nutrition advice for healthy eating checks fiber`() = runTest {
        val lowFiberNutrition = NutritionInfo(
            id = "low",
            recipeId = "low",
            calories = 500,
            protein = 25.0,
            fat = 15.0,
            carbs = 60.0,
            fiber = 3.0,
            sodium = 700,
            servingSize = 1
        )

        every { nutritionInfoDao.getNutritionByRecipe(any()) }.returns(lowFiberNutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "low", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)
        val advice = analyzer.getNutritionAdvice(report, NutritionAnalyzer.HealthGoal.HEALTHY_EATING)

        assertTrue(advice.any { it.category == "膳食纤维" })
    }

    @Test
    fun `empty meal plans returns zero nutrition`() = runTest {
        val report = analyzer.analyzeDailyNutrition(emptyList())

        assertEquals(0, report.totalNutrition.calories)
        assertEquals(0.0, report.totalNutrition.protein, 0.01)
        assertEquals(0.0, report.totalNutrition.fat, 0.01)
    }
}
