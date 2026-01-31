package com.homepantry.integration

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.data.banquet.BanquetMenuGenerator
import com.homepantry.data.banquet.BanquetOccasion
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.MealPlan
import com.homepantry.data.entity.MealType
import com.homepantry.data.entity.Recipe
import com.homepantry.data.mealplan.WeeklyMealPlanGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 周菜单规划集成测试
 */
@RunWith(AndroidJUnit4::class)
class WeeklyMealPlanningIntegrationTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var generator: WeeklyMealPlanGenerator

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomePantryDatabase::class.java
        ).allowMainThreadQueries().build()

        generator = WeeklyMealPlanGenerator(
            recipeDao = database.recipeDao(),
            mealPlanDao = database.mealPlanDao(),
            userProfileDao = database.userProfileDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `generate weekly menu creates valid options`() = runTest {
        // 添加测试菜谱
        val recipes = listOf(
            Recipe(id = "r1", name = "番茄炒蛋", cookingTime = 15, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r2", name = "青椒肉丝", cookingTime = 20, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r3", name = "红烧肉", cookingTime = 90, servings = 4, difficulty = DifficultyLevel.HARD),
            Recipe(id = "r4", name = "清蒸鱼", cookingTime = 25, servings = 3, difficulty = DifficultyLevel.MEDIUM),
            Recipe(id = "r5", name = "麻婆豆腐", cookingTime = 20, servings = 2, difficulty = DifficultyLevel.MEDIUM)
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 7,
            mealTypes = listOf(MealType.LUNCH, MealType.DINNER),
            servings = 2,
            maxCookingTime = 60
        )

        val result = generator.generateWeeklyMealPlans(
            startDate = System.currentTimeMillis(),
            config = config
        )

        assertTrue(result.isSuccess)
        val options = result.getOrNull()!!
        assertTrue(options.size >= 2)
    }

    @Test
    fun `balanced option has diverse recipes`() = runTest {
        val recipes = listOf(
            Recipe(id = "r1", name = "番茄炒蛋", cookingTime = 15, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r2", name = "青椒肉丝", cookingTime = 20, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r3", name = "红烧肉", cookingTime = 90, servings = 4, difficulty = DifficultyLevel.HARD)
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 3,
            mealTypes = listOf(MealType.LUNCH),
            servings = 2
        )

        val result = generator.generateWeeklyMealPlans(
            startDate = System.currentTimeMillis(),
            config = config
        )

        val balancedOption = result.getOrNull()?.find { it.name == "营养均衡" }
        assertNotNull(balancedOption)

        val mealPlans = balancedOption!!.mealPlans
        val uniqueRecipeIds = mealPlans.map { it.recipeId }.distinct()
        assertTrue(uniqueRecipeIds.size >= mealPlans.size * 0.7) // 至少70%多样性
    }

    @Test
    fun `quick option only uses fast recipes`() = runTest {
        val recipes = listOf(
            Recipe(id = "r1", name = "番茄炒蛋", cookingTime = 15, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r2", name = "红烧肉", cookingTime = 90, servings = 4, difficulty = DifficultyLevel.HARD)
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 1,
            mealTypes = listOf(MealType.LUNCH),
            servings = 2,
            maxCookingTime = 30,
            includeQuickOption = true
        )

        val result = generator.generateWeeklyMealPlans(
            startDate = System.currentTimeMillis(),
            config = config
        )

        val quickOption = result.getOrNull()?.find { it.name == "快手省时" }
        assertNotNull(quickOption)

        // 所有菜谱都应该在30分钟内
        quickOption!!.mealPlans.forEach { mealPlan ->
            val recipe = recipes.find { it.id == mealPlan.recipeId }
            assertNotNull(recipe)
            assertTrue(recipe!!.cookingTime <= 30)
        }
    }

    @Test
    fun `save meal plan option persists plans`() = runTest {
        val recipes = listOf(
            Recipe(id = "r1", name = "番茄炒蛋", cookingTime = 15, servings = 2, difficulty = DifficultyLevel.EASY)
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val option = WeeklyMealPlanGenerator.MealPlanOption(
            name = "测试方案",
            description = "测试",
            mealPlans = listOf(
                MealPlan(
                    date = System.currentTimeMillis(),
                    mealType = MealType.LUNCH,
                    recipeId = "r1",
                    servings = 2
                )
            )
        )

        val result = generator.saveMealPlanOption(option)
        assertTrue(result.isSuccess)

        // 验证已保存
        val savedPlans = database.mealPlanDao().getMealPlansForWeek(0, Long.MAX_VALUE).first()
        assertTrue(savedPlans.isNotEmpty())
    }

    @Test
    fun `analyze menu detects issues`() = runTest {
        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 2),
            MealPlan(System.currentTimeMillis(), MealType.DINNER, "r1", 2) // 重复
        )

        val analysis = generator.analyzeMenu(mealPlans)

        assertNotNull分析)
        // 重复菜谱应该触发建议
        assertTrue(analysis.suggestions.any {
            it.contains("重复") || it.contains("多样性")
        })
    }

    @Test
    fun `generate menu with time constraint filters recipes`() = runTest {
        val recipes = listOf(
            Recipe(id = "r1", name = "番茄炒蛋", cookingTime = 15, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r2", name = "红烧肉", cookingTime = 120, servings = 4, difficulty = DifficultyLevel.HARD)
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 1,
            mealTypes = listOf(MealType.LUNCH),
            servings = 2,
            maxCookingTime = 30
        )

        val result = generator.generateWeeklyMealPlans(
            startDate = System.currentTimeMillis(),
            config = config
        )

        val option = result.getOrNull()?.first()
        assertNotNull(option)

        // 应该只选择快手的菜谱
        option!!.mealPlans.forEach { mealPlan ->
            val recipe = recipes.find { it.id == mealPlan.recipeId }
            assertNotNull(recipe)
            assertEquals(15, recipe!!.cookingTime) // 只有15分钟的菜
        }
    }
}
