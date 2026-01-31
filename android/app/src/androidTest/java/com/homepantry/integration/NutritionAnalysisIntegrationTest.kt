package com.homepantry.integration

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.entity.*
import com.homepantry.data.nutrition.NutritionAnalyzer
import com.homepantry.data.nutrition.NutritionComparator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 营养分析集成测试
 */
@RunWith(AndroidJUnit4::class)
class NutritionAnalysisIntegrationTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var analyzer: NutritionAnalyzer

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomePantryDatabase::class.java
        ).allowMainThreadQueries().build()

        analyzer = NutritionAnalyzer(
            recipeDao = database.recipeDao(),
            nutritionInfoDao = database.nutritionInfoDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `analyze daily nutrition calculates totals correctly`() = runTest {
        // 添加菜谱和营养信息
        val recipe = Recipe(id = "r1", name = "测试菜", cookingTime = 30, servings = 1, difficulty = DifficultyLevel.EASY)
        database.recipeDao().insertRecipe(recipe)

        val nutrition = com.homepantry.data.entity.NutritionInfo(
            id = "nutri1",
            recipeId = "r1",
            calories = 500,
            protein = 25.0,
            fat = 15.0,
            carbs = 60.0,
            fiber = 5.0,
            sodium = 800,
            servingSize = 1
        )
        database.nutritionInfoDao().insertNutrition(nutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 2), // 2人份
            MealPlan(System.currentTimeMillis() + 3600000, MealType.DINNER, "r1", 1) // 1人份
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)

        assertEquals(1500, report.totalNutrition.calories) // 500 * 2 + 500 * 1
        assertEquals(75.0, report.totalNutrition.protein, 0.01) // 25 * 2 + 25 * 1
        assertEquals(3, report.mealNutrition.size)
    }

    @Test
    fun `nutrition assessment provides valid score`() = runTest {
        val recipe = Recipe(id = "r1", name = "均衡菜", cookingTime = 30, servings = 1, difficulty = DifficultyLevel.EASY)
        database.recipeDao().insertRecipe(recipe)

        // 完美均衡的营养
        val nutrition = com.homepantry.data.entity.NutritionInfo(
            id = "nutri1",
            recipeId = "r1",
            calories = 600,
            protein = 60.0,
            fat = 40.0,
            carbs = 200.0,
            fiber = 25.0,
            sodium = 1800,
            servingSize = 1
        )
        database.nutritionInfoDao().insertNutrition(nutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)

        assertNotNull(report.assessment)
        assertTrue(report.assessment.score >= 60)
    }

    @Test
    fun `nutrition assessment detects low protein`() = runTest {
        val recipe = Recipe(id = "r1", name = "低蛋白", cookingTime = 30, servings = 1, difficulty = DifficultyLevel.EASY)
        database.recipeDao().insertRecipe(recipe)

        val nutrition = com.homepantry.data.entity.NutritionInfo(
            id = "nutri1",
            recipeId = "r1",
            calories = 2000,
            protein = 20.0,
            fat = 80.0,
            carbs = 300.0,
            fiber = 5.0,
            sodium = 2000,
            servingSize = 1
        )
        database.nutritionInfoDao().insertNutrition(nutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)

        assertTrue(report.assessment.issues.any {
            it.contains("蛋白质") || it.contains("不足")
        })
    }

    @Test
    fun `weekly report calculates averages`() = runTest {
        val recipe = Recipe(id = "r1", name = "测试", cookingTime = 30, servings = 1, difficulty = DifficultyLevel.EASY)
        database.recipeDao().insertRecipe(recipe)

        val nutrition = com.homepantry.data.entity.NutritionInfo(
            id = "nutri1",
            recipeId = "r1",
            calories = 500,
            protein = 30.0,
            fat = 20.0,
            carbs = 60.0,
            fiber = 8.0,
            sodium = 1000,
            servingSize = 1
        )
        database.nutritionInfoDao().insertNutrition(nutrition)

        val day1 = System.currentTimeMillis()
        val day2 = day1 + 86400000

        val weeklyPlans = mapOf(
            day1 to listOf(
                MealPlan(day1, MealType.LUNCH, "r1", 1),
                MealPlan(day1, MealType.DINNER, "r1", 1)
            ),
            day2 to listOf(
                MealPlan(day2, MealType.LUNCH, "r1", 1)
            )
        )

        val report = analyzer.analyzeWeeklyNutrition(weeklyPlans)

        assertEquals(2, report.dailyReports.size)
        assertTrue(report.averageNutrition.avgCalories > 0)
        assertTrue(report.averageNutrition.avgProtein > 0)
    }

    @Test
    fun `compare to recommendations gives valid comparison`() = runTest {
        val recipe = Recipe(id = "r1", name = "测试", cookingTime = 30, servings = 1, difficulty = DifficultyLevel.EASY)
        database.recipeDao().insertRecipe(recipe)

        val nutrition = com.homepantry.data.entity.NutritionInfo(
            id = "nutri1",
            recipeId = "r1",
            calories = 500,
            protein = 30.0,
            fat = 20.0,
            carbs = 60.0,
            fiber = 8.0,
            sodium = 1000,
            servingSize = 1
        )
        database.nutritionInfoDao().insertNutrition(nutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 2)
        )

        val comparison = analyzer.compareToRecommendations(
            mealPlans = mealPlans,
            gender = NutritionAnalyzer.Gender.FEMALE,
            age = 30,
            activityLevel = NutritionAnalyzer.ActivityLevel.MODERATE
        )

        assertNotNull(comparison.recommended)
        assertNotNull(comparison.differences)
        assertEquals(1000, comparison.actual.calories) // 500 * 2
    }

    @Test
    fun `get nutrition advice for weight loss checks calories`() = runTest {
        val recipe = Recipe(id = "r1", name = "高热量", cookingTime = 30, servings = 1, difficulty = DifficultyLevel.EASY)
        database.recipeDao().insertRecipe(recipe)

        val nutrition = com.homepantry.data.entity.NutritionInfo(
            id = "nutri1",
            recipeId = "r1",
            calories = 800,
            protein = 30.0,
            fat = 25.0,
            carbs = 80.0,
            fiber = 5.0,
            sodium = 1200,
            servingSize = 1
        )
        database.nutritionInfoDao().insertNutrition(nutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 1),
            MealPlan(System.currentTimeMillis() + 3600000, MealType.DINNER, "r1", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)
        val advice = analyzer.getNutritionAdvice(report, NutritionAnalyzer.HealthGoal.WEIGHT_LOSS)

        assertTrue(advice.isNotEmpty())
        assertTrue(advice.any { it.category == "热量" })
    }

    @Test
    fun `get nutrition advice for muscle gain checks protein`() = runTest {
        val recipe = Recipe(id = "r1", name = "低蛋白", cookingTime = 30, servings = 1, difficulty = DifficultyLevel.EASY)
        database.recipeDao().insertRecipe(recipe)

        val nutrition = com.homepantry.data.entity.NutritionInfo(
            id = "nutri1",
            recipeId = "r1",
            calories = 400,
            protein = 10.0,
            fat = 15.0,
            carbs = 50.0,
            fiber = 3.0,
            sodium = 600,
            servingSize = 1
        )
        database.nutritionInfoDao().insertNutrition(nutrition)

        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 1)
        )

        val report = analyzer.analyzeDailyNutrition(mealPlans)
        val advice = analyzer.getNutritionAdvice(report, NutritionAnalyzer.HealthGoal.MUSCLE_GAIN)

        assertTrue(advice.any { it.category == "蛋白质" })
    }

    @Test
    fun `nutrition with missing info uses defaults`() = runTest {
        val recipe = Recipe(id = "r1", name = "无营养信息", cookingTime = 30, servings = 1, difficulty = DifficultyLevel.EASY)
        database.recipeDao().insertRecipe(recipe)

        // 不添加营养信息
        val nutrition = analyzer.getNutritionForRecipe("r1", servings = 2)

        // 应该使用默认值
        assertEquals(800, nutrition.calories) // 400 * 2
        assertEquals(40.0, nutrition.protein, 0.01) // 20 * 2
    }
}
