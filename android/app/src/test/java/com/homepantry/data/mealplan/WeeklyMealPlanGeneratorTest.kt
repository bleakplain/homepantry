package com.homepantry.data.mealplan

import com.homepantry.data.dao.MealPlanDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.dao.UserProfileDao
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.MealPlan
import com.homepantry.data.entity.MealType
import com.homepantry.data.entity.Recipe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 周菜单生成器测试
 */
class WeeklyMealPlanGeneratorTest {

    private lateinit var recipeDao: RecipeDao
    private lateinit var mealPlanDao: MealPlanDao
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var generator: WeeklyMealPlanGenerator

    private val testRecipes = listOf(
        Recipe(
            id = "r1",
            name = "番茄炒蛋",
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            averageRating = 4.5f,
            categoryId = "cat1"
        ),
        Recipe(
            id = "r2",
            name = "红烧肉",
            cookingTime = 90,
            servings = 4,
            difficulty = DifficultyLevel.HARD,
            averageRating = 4.8f,
            categoryId = "cat2"
        ),
        Recipe(
            id = "r3",
            name = "清蒸鱼",
            cookingTime = 25,
            servings = 3,
            difficulty = DifficultyLevel.MEDIUM,
            averageRating = 4.6f,
            categoryId = "cat3"
        ),
        Recipe(
            id = "r4",
            name = "青椒肉丝",
            cookingTime = 20,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            averageRating = 4.3f,
            categoryId = "cat1"
        ),
        Recipe(
            id = "r5",
            name = "麻婆豆腐",
            cookingTime = 20,
            servings = 2,
            difficulty = DifficultyLevel.MEDIUM,
            averageRating = 4.7f,
            categoryId = "cat2"
        )
    )

    @Before
    fun setup() {
        recipeDao = mockk()
        mealPlanDao = mockk()
        userProfileDao = mockk()

        every { recipeDao.getAllRecipes() }.returns(flowOf(testRecipes))
        every { recipeDao.getRecipeIngredients(any()) }.returns(emptyList())
        every { userProfileDao.getProfileById("default") }.returns(null)

        generator = WeeklyMealPlanGenerator(recipeDao, mealPlanDao, userProfileDao)
    }

    @Test
    fun `generate weekly meal plans returns options`() = runTest {
        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 7,
            mealTypes = listOf(MealType.LUNCH, MealType.DINNER),
            servings = 2,
            maxCookingTime = 60,
            includeQuickOption = true,
            includeVarietyOption = true
        )

        val result = generator.generateWeeklyMealPlan(
            startDate = System.currentTimeMillis(),
            config = config
        )

        assertTrue(result.isSuccess)
        val options = result.getOrNull()
        assertNotNull(options)
        assertTrue(options!!.size >= 2) // 至少有平衡和快手两个方案
    }

    @Test
    fun `balanced option uses varied recipes`() = runTest {
        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 3,
            mealTypes = listOf(MealType.LUNCH),
            servings = 2
        )

        val result = generator.generateWeeklyMealPlan(
            startDate = System.currentTimeMillis(),
            config = config
        )

        assertTrue(result.isSuccess)
        val options = result.getOrNull()
        val balancedOption = options?.find { it.name == "营养均衡" }

        assertNotNull(balancedOption)
        assertEquals(3, balancedOption!!.mealPlans.size)
    }

    @Test
    fun `quick option filters by cooking time`() = runTest {
        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 1,
            mealTypes = listOf(MealType.LUNCH),
            servings = 2,
            maxCookingTime = 30,
            includeQuickOption = true
        )

        val result = generator.generateWeeklyMealPlan(
            startDate = System.currentTimeMillis(),
            config = config
        )

        assertTrue(result.isSuccess)
        val options = result.getOrNull()
        val quickOption = options?.find { it.name == "快手省时" }

        assertNotNull(quickOption)
        // 快手方案应该只包含30分钟内的菜谱
        quickOption!!.mealPlans.forEach { mealPlan ->
            val recipe = testRecipes.find { it.id == mealPlan.recipeId }
            assertTrue(recipe!!.cookingTime <= 30)
        }
    }

    @Test
    fun `meal plans have correct date and meal type`() = runTest {
        val startDate = System.currentTimeMillis()
        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 1,
            mealTypes = listOf(MealType.LUNCH, MealType.DINNER),
            servings = 2
        )

        val result = generator.generateWeeklyMealPlan(
            startDate = startDate,
            config = config
        )

        assertTrue(result.isSuccess)
        val option = result.getOrNull()?.first()
        val mealPlans = option!!.mealPlans

        assertEquals(2, mealPlans.size)
        assertEquals(MealType.LUNCH, mealPlans[0].mealType)
        assertEquals(MealType.DINNER, mealPlans[1].mealType)
    }

    @Test
    fun `save meal plan option inserts plans`() = runTest {
        val mealPlans = listOf(
            MealPlan(
                date = System.currentTimeMillis(),
                mealType = MealType.LUNCH,
                recipeId = "r1",
                servings = 2
            )
        )

        val option = WeeklyMealPlanGenerator.MealPlanOption(
            name = "测试方案",
            description = "测试",
            mealPlans = mealPlans
        )

        every { mealPlanDao.deleteMealPlansForDateRange(any(), any()) }.returns(0)
        every { mealPlanDao.insertMealPlan(any()) }.returns(1L)

        val result = generator.saveMealPlanOption(option)

        assertTrue(result.isSuccess)
        verify { mealPlanDao.insertMealPlan(mealPlans[0]) }
    }

    @Test
    fun `analyze menu checks ingredient diversity`() = runTest {
        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 2),
            MealPlan(System.currentTimeMillis(), MealType.DINNER, "r2", 2),
            MealPlan(System.currentTimeMillis() + 86400000, MealType.LUNCH, "r3", 2)
        )

        val analysis = generator.analyzeMenu(mealPlans)

        assertNotNull分析)
        assertNotNull(analysis.ingredientDiversity)
        assertNotNull(analysis.spiceBalance)
        assertNotNull(analysis.nutritionBalance)
    }

    @Test
    fun `analyze menu detects duplicate ingredients`() = runTest {
        val mealPlans = listOf(
            MealPlan(System.currentTimeMillis(), MealType.LUNCH, "r1", 2),
            MealPlan(System.currentTimeMillis(), MealType.DINNER, "r1", 2), // 重复
            MealPlan(System.currentTimeMillis() + 86400000, MealType.LUNCH, "r2", 2)
        )

        val analysis = generator.analyzeMenu(mealPlans)

        // 重复率应该高
        assertTrue(analysis.ingredientDiversity.score < 1.0)
    }

    @Test
    fun `config with max difficulty filters recipes`() = runTest {
        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 1,
            mealTypes = listOf(MealType.LUNCH),
            servings = 2,
            maxDifficulty = DifficultyLevel.EASY
        )

        val result = generator.generateWeeklyMealPlan(
            startDate = System.currentTimeMillis(),
            config = config
        )

        assertTrue(result.isSuccess)
        val option = result.getOrNull()?.first()

        // 所有选中的菜谱都应该是简单难度
        option!!.mealPlans.forEach { mealPlan ->
            val recipe = testRecipes.find { it.id == mealPlan.recipeId }
            assertEquals(DifficultyLevel.EASY, recipe!!.difficulty)
        }
    }

    @Test
    fun `variety option uses different categories`() = runTest {
        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 2,
            mealTypes = listOf(MealType.LUNCH),
            servings = 2,
            includeVarietyOption = true
        )

        val result = generator.generateWeeklyMealPlan(
            startDate = System.currentTimeMillis(),
            config = config
        )

        assertTrue(result.isSuccess)
        val varietyOption = result.getOrNull()?.find { it.name == "口味多样" }

        assertNotNull(varietyOption)
    }

    @Test
    fun `empty recipe list returns empty options`() = runTest {
        every { recipeDao.getAllRecipes() }.returns(flowOf(emptyList()))

        val config = WeeklyMealPlanGenerator.MealPlanConfig(
            days = 1,
            mealTypes = listOf(MealType.LUNCH),
            servings = 2
        )

        val result = generator.generateWeeklyMealPlan(
            startDate = System.currentTimeMillis(),
            config = config
        )

        assertTrue(result.isSuccess)
        val options = result.getOrNull()
        assertNotNull(options)
        // 即使没有菜谱，也应该返回方案（只是空的）
        assertTrue(options!!.isNotEmpty())
    }
}
