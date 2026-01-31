package com.homepantry.integration

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.data.banquet.BanquetMenuGenerator
import com.homepantry.data.banquet.BanquetConfig
import com.homepantry.data.banquet.BanquetOccasion
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
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
 * 宴请模式集成测试
 */
@RunWith(AndroidJUnit4::class)
class BanquetModeIntegrationTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var generator: BanquetMenuGenerator

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomePantryDatabase::class.java
        ).allowMainThreadQueries().build()

        generator = BanquetMenuGenerator(recipeDao = database.recipeDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `generate family dinner menu creates valid courses`() = runTest {
        val recipes = listOf(
            Recipe(
                id = "r1", name = "凉拌黄瓜",
                cookingTime = 10,
                servings = 2,
                difficulty = DifficultyLevel.EASY,
                averageRating = 4.2f
            ),
            Recipe(
                id = "r2", name = "糖醋排骨",
                cookingTime = 45,
                servings = 4,
                difficulty = DifficultyLevel.MEDIUM,
                averageRating = 4.8f
            ),
            Recipe(
                id = "r3", name = "番茄蛋汤",
                cookingTime = 15,
                servings = 4,
                difficulty = DifficultyLevel.EASY,
                averageRating = 4.5f
            )
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = BanquetConfig(
            occasion = BanquetOccasion.FAMILY_DINNER,
            guestCount = 4,
            startTime = 18 * 60, // 18:00
        )

        val result = generator.generateBanquetMenu(config)

        assertTrue(result.isSuccess)
        val menu = result.getOrNull()!!

        assertTrue(menu.courses.isNotEmpty())
        assertEquals(4, menu.config.guestCount)
    }

    @Test
    fun `friends gathering menu includes appetizers`() = runTest {
        val recipes = listOf(
            Recipe(
                id = "r1", name = "拍黄瓜",
                cookingTime = 10,
                servings = 2,
                difficulty = DifficultyLevel.EASY,
                averageRating = 4.0f
            ),
            Recipe(
                id = "r2", name = "宫保鸡丁",
                cookingTime = 30,
                servings = 2,
                difficulty = DifficultyLevel.MEDIUM,
                averageRating = 4.7f
            )
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = BanquetConfig(
            occasion = BanquetOccasion.FRIENDS_GATHERING,
            guestCount = 6,
            startTime = 19 * 60
        )

        val result = generator.generateBanquetMenu(config)

        assertTrue(result.isSuccess)
        val menu = result.getOrNull()!!

        assertTrue(menu.courses.isNotEmpty())
    }

    @Test
    fun `time plan includes prep time`() = runTest {
        val recipes = listOf(
            Recipe(
                id = "r1", name = "需要腌制的肉",
                cookingTime = 30,
                servings = 4,
                difficulty = DifficultyLevel.MEDIUM,
                averageRating = 4.5f,
                prepTime = 20
            )
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = BanquetConfig(
            occasion = BanquetOccasion.CELEBRATION,
            guestCount = 4,
            startTime = 18 * 60
        )

        val result = generator.generateBanquetMenu(config)

        assertTrue(result.isSuccess)
        val menu = result.getOrNull()!!

        // 准备时间应该早于烹饪开始时间
        assertTrue(menu.timePlan.prepTime < menu.timePlan.startTime)
    }

    @Test
    fun `analyze menu detects issues`() = runTest {
        val recipes = listOf(
            Recipe(
                id = "r1", name = "猪肉",
                cookingTime = 60,
                servings = 4,
                difficulty = DifficultyLevel.HARD
            ),
            Recipe(
                id = "r2", name = "猪肉炒",
                cookingTime = 30,
                servings = 2,
                difficulty = DifficultyLevel.EASY
            )
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = BanquetConfig(
            occasion = BanquetOccasion.FAMILY_DINNER,
            guestCount = 4,
            startTime = 18 * 60
        )

        val result = generator.generateBanquetMenu(config)
        val menu = result.getOrNull()!!

        // 模拟重复主食材（实际需要从食材表获取）
        val coursesWithSameMainIngredient = menu.courses.map { course ->
            course.copy(mainIngredient = "猪肉") // 模拟相同
        }

        val analysis = generator.analyzeMenu(coursesWithSameMainIngredient)

        assertNotNull(分析)
        // 主材重复应该触发建议
        assertTrue(
            analysis.mainIngredients.size < menu.courses.size ||
            analysis.suggestions.isNotEmpty()
        )
    }

    @Test
    fun `calculate overall difficulty correctly`() = runTest {
        val recipes = listOf(
            Recipe(id = "r1", name = "简单", cookingTime = 15, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r2", name = "中等", cookingTime = 30, servings = 2, difficulty = DifficultyLevel.MEDIUM),
            Recipe(id = "r3", name = "困难", cookingTime = 60, servings = 2, difficulty = DifficultyLevel.HARD)
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = BanquetConfig(
            occasion = BanquetOccasion.CELEBRATION,
            guestCount = 4,
            startTime = 18 * 60
        )

        val result = generator.generateBanquetMenu(config)
        val menu = result.getOrNull()!!

        // 庆祝场合应该包含复杂的菜
        val analysis = generator.analyzeMenu(menu.courses)
        assertTrue(analysis.difficultyLevel == DifficultyLevel.HARD)
    }

    @Test
    fun `time plan has valid sequence`() = runTest {
        val recipes = listOf(
            Recipe(
                id = "r1", name = "长时间炖菜",
                cookingTime = 90,
                servings = 4,
                difficulty = DifficultyLevel.HARD
            ),
            Recipe(
                id = "r2", name = "快手炒菜",
                cookingTime = 15,
                servings = 2,
                difficulty = DifficultyLevel.EASY
            )
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = BanquetConfig(
            occasion = BanquetOccasion.FAMILY_DINNER,
            guestCount = 4,
            startTime = 17 * 60 // 17:00 开始
        )

        val result = generator.generateBanquetMenu(config)
        val menu = result.getOrNull()!!

        // 长时间菜应该先做
        val longDishTask = menu.timePlan.tasks.find {
            it.recipe.recipe.cookingTime == 90
        }
        assertNotNull(longDishTask)

        // 快手菜应该晚做
        val quickDishTask = menu.timePlan.tasks.find {
            it.recipe.recipe.cookingTime == 15
        }
        assertNotNull(quickDishTask)

        // 长时间菜的开始时间应该早于快手菜
        assertTrue(longDishTask!!.time < quickDishTask!!.time)
    }

    @Test
    fun `config with max time filters recipes`() = runTest {
        val recipes = listOf(
            Recipe(
                id = "r1", name = "快手菜",
                cookingTime = 15,
                servings = 2,
                difficulty = DifficultyLevel.EASY
            ),
            Recipe(
                id = "r2", name = "慢炖菜",
                cookingTime = 120,
                servings = 4,
                difficulty = DifficultyLevel.HARD
            )
        )

        recipes.forEach { database.recipeDao().insertRecipe(it) }

        val config = BanquetConfig(
            occasion = BanquetOccasion.CASUAL,
            guestCount = 2,
            startTime = 18 * 60,
            maxTimePerDish = 30
        )

        val result = generator.generateBanquetMenu(config)

        assertTrue(result.isSuccess)
        val menu = result.getOrNull()!!

        // 所有菜谱都应该在30分钟内
        menu.courses.forEach { course ->
            assertTrue(course.recipe.cookingTime <= 30)
        }
    }

    @Test
    fun `empty recipe list returns empty menu`() = runTest {
        // 不添加任何菜谱

        val config = BanquetConfig(
            occasion = BanquetOccasion.FAMILY_DINNER,
            guestCount = 2,
            startTime = 18 * 60
        )

        val result = generator.generateBanquetMenu(config)

        assertTrue(result.isSuccess)
        val menu = result.getOrNull()!!

        // 没有菜谱时应该返回空菜单
        assertTrue(menu.courses.isEmpty())
    }
}
