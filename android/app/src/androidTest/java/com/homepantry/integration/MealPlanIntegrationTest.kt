package com.homepantry.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.entity.MealPlan
import com.homepantry.data.entity.MealType
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.repository.MealPlanRepository
import com.homepantry.data.repository.RecipeRepository
import com.homepantry.viewmodel.MealPlanViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MealPlanIntegrationTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var mealPlanRepository: MealPlanRepository
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var viewModel: MealPlanViewModel

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomePantryDatabase::class.java
        ).allowMainThreadQueries().build()

        mealPlanRepository = MealPlanRepository(database.mealPlanDao())
        recipeRepository = RecipeRepository(
            database.recipeDao(),
            database.ingredientDao()
        )
        viewModel = MealPlanViewModel(mealPlanRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createMealPlan_workflow() = runTest {
        // First create a recipe
        val recipe = Recipe(
            id = "recipe-1",
            name = "番茄炒蛋",
            description = "经典家常菜",
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY
        )
        recipeRepository.insertRecipe(recipe)

        // Create meal plan for today
        val today = System.currentTimeMillis()
        val mealPlan = MealPlan(
            id = "meal-1",
            date = today,
            mealType = MealType.LUNCH,
            recipeId = recipe.id,
            servings = 4,
            notes = "多放点辣"
        )

        viewModel.addMealPlan(mealPlan)

        // Verify meal plan was created
        val mealPlans = mealPlanRepository.getAllMealPlans().first()
        assertEquals(1, mealPlans.size)
        assertEquals(MealType.LUNCH, mealPlans[0].mealType)
        assertEquals(4, mealPlans[0].servings)

        // Verify we can get meal plans for the date
        val plansForDate = mealPlanRepository.getMealPlansForDate(today).first()
        assertEquals(1, plansForDate.size)
    }

    @Test
    fun planFullDay_workflow() = runTest {
        // Create recipes
        val recipes = listOf(
            Recipe(id = "r1", name = "粥", cookingTime = 10, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r2", name = "炒饭", cookingTime = 15, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r3", name = "汤", cookingTime = 30, servings = 2, difficulty = DifficultyLevel.MEDIUM)
        )

        recipes.forEach { recipeRepository.insertRecipe(it) }

        // Plan all three meals for today
        val today = System.currentTimeMillis()

        val breakfast = MealPlan(
            id = "m1",
            date = today,
            mealType = MealType.BREAKFAST,
            recipeId = "r1",
            servings = 2
        )

        val lunch = MealPlan(
            id = "m2",
            date = today,
            mealType = MealType.LUNCH,
            recipeId = "r2",
            servings = 2
        )

        val dinner = MealPlan(
            id = "m3",
            date = today,
            mealType = MealType.DINNER,
            recipeId = "r3",
            servings = 2
        )

        viewModel.addMealPlan(breakfast)
        viewModel.addMealPlan(lunch)
        viewModel.addMealPlan(dinner)

        // Verify all meal plans were created
        val mealPlans = mealPlanRepository.getMealPlansForDate(today).first()
        assertEquals(3, mealPlans.size)

        // Verify each meal type
        val breakfastPlans = mealPlanRepository.getMealPlansForMealType(today, MealType.BREAKFAST).first()
        val lunchPlans = mealPlanRepository.getMealPlansForMealType(today, MealType.LUNCH).first()
        val dinnerPlans = mealPlanRepository.getMealPlansForMealType(today, MealType.DINNER).first()

        assertEquals(1, breakfastPlans.size)
        assertEquals(1, lunchPlans.size)
        assertEquals(1, dinnerPlans.size)
    }

    @Test
    fun planWeek_workflow() = runTest {
        // Create a recipe
        val recipe = Recipe(
            id = "recipe-1",
            name = "家常菜",
            cookingTime = 30,
            servings = 4,
            difficulty = DifficultyLevel.MEDIUM
        )
        recipeRepository.insertRecipe(recipe)

        // Plan meals for a week
        val now = System.currentTimeMillis()
        val dayInMillis = 86400000

        for (day in 0..6) {
            val date = now + (day * dayInMillis)
            val mealPlan = MealPlan(
                id = "meal-$day",
                date = date,
                mealType = MealType.DINNER,
                recipeId = recipe.id,
                servings = 4
            )
            viewModel.addMealPlan(mealPlan)
        }

        // Get meal plans for the week
        val weekStart = now
        val weekEnd = now + (7 * dayInMillis)

        val weekPlans = mealPlanRepository.getMealPlansForWeek(weekStart, weekEnd).first()
        assertEquals(7, weekPlans.size)

        // Verify each day has a plan
        for (day in 0..6) {
            val date = now + (day * dayInMillis)
            val plansForDate = mealPlanRepository.getMealPlansForDate(date).first()
            assertEquals(1, plansForDate.size)
        }
    }

    @Test
    fun updateMealPlan_workflow() = runTest {
        // Create recipe and meal plan
        val recipe = Recipe(
            id = "recipe-1",
            name = "原菜谱",
            cookingTime = 30,
            servings = 4,
            difficulty = DifficultyLevel.MEDIUM
        )
        recipeRepository.insertRecipe(recipe)

        val mealPlan = MealPlan(
            id = "meal-1",
            date = System.currentTimeMillis(),
            mealType = MealType.LUNCH,
            recipeId = recipe.id,
            servings = 2,
            notes = "原备注"
        )

        viewModel.addMealPlan(mealPlan)

        // Update meal plan
        val updatedMealPlan = mealPlan.copy(
            servings = 6,
            notes = "更新后的备注"
        )

        viewModel.updateMealPlan(updatedMealPlan)

        // Verify update
        val plans = mealPlanRepository.getAllMealPlans().first()
        assertEquals(6, plans[0].servings)
        assertEquals("更新后的备注", plans[0].notes)
    }

    @Test
    fun deleteMealPlan_workflow() = runTest {
        val recipe = Recipe(
            id = "recipe-1",
            name = "测试菜谱",
            cookingTime = 30,
            servings = 4,
            difficulty = DifficultyLevel.MEDIUM
        )
        recipeRepository.insertRecipe(recipe)

        val mealPlan = MealPlan(
            id = "meal-1",
            date = System.currentTimeMillis(),
            mealType = MealType.LUNCH,
            recipeId = recipe.id,
            servings = 2
        )

        viewModel.addMealPlan(mealPlan)

        // Verify it exists
        var plans = mealPlanRepository.getAllMealPlans().first()
        assertEquals(1, plans.size)

        // Delete it
        viewModel.deleteMealPlan(mealPlan.id)

        // Verify deletion
        plans = mealPlanRepository.getAllMealPlans().first()
        assertTrue(plans.isEmpty())
    }

    @Test
    fun deleteMealPlansForDateRange_workflow() = runTest {
        val recipe = Recipe(
            id = "recipe-1",
            name = "测试菜谱",
            cookingTime = 30,
            servings = 4,
            difficulty = DifficultyLevel.MEDIUM
        )
        recipeRepository.insertRecipe(recipe)

        val now = System.currentTimeMillis()

        // Create meal plans for 5 days
        (0..4).forEach { day ->
            val date = now + (day * 86400000)
            val mealPlan = MealPlan(
                id = "meal-$day",
                date = date,
                mealType = MealType.DINNER,
                recipeId = recipe.id,
                servings = 2
            )
            viewModel.addMealPlan(mealPlan)
        }

        // Delete first 3 days
        val startDate = now
        val endDate = now + (2 * 86400000)

        viewModel.deleteMealPlansForDateRange(startDate, endDate)

        // Verify deletion
        val remainingPlans = mealPlanRepository.getAllMealPlans().first()
        assertEquals(2, remainingPlans.size)
    }

    @Test
    fun multipleMealPlansForSameDay_workflow() = runTest {
        val recipes = listOf(
            Recipe(id = "r1", name = "早餐", cookingTime = 10, servings = 1, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r2", name = "午餐", cookingTime = 20, servings = 2, difficulty = DifficultyLevel.EASY),
            Recipe(id = "r3", name = "晚餐", cookingTime = 30, servings = 3, difficulty = DifficultyLevel.MEDIUM),
            Recipe(id = "r4", name = "加餐", cookingTime = 5, servings = 1, difficulty = DifficultyLevel.EASY)
        )

        recipes.forEach { recipeRepository.insertRecipe(it) }

        val today = System.currentTimeMillis()

        // Add multiple meals for the same day
        val meal1 = MealPlan(id = "m1", date = today, mealType = MealType.BREAKFAST, recipeId = "r1", servings = 1)
        val meal2 = MealPlan(id = "m2", date = today, mealType = MealType.LUNCH, recipeId = "r2", servings = 2)
        val meal3 = MealPlan(id = "m3", date = today, mealType = MealType.DINNER, recipeId = "r3", servings = 3)

        viewModel.addMealPlan(meal1)
        viewModel.addMealPlan(meal2)
        viewModel.addMealPlan(meal3)

        // Verify all meal plans for the day
        val plansForDate = mealPlanRepository.getMealPlansForDate(today).first()
        assertEquals(3, plansForDate.size)

        // Verify meal types
        val breakfastPlans = mealPlanRepository.getMealPlansForMealType(today, MealType.BREAKFAST).first()
        val lunchPlans = mealPlanRepository.getMealPlansForMealType(today, MealType.LUNCH).first()
        val dinnerPlans = mealPlanRepository.getMealPlansForMealType(today, MealType.DINNER).first()

        assertEquals(1, breakfastPlans.size)
        assertEquals(1, lunchPlans.size)
        assertEquals(1, dinnerPlans.size)
    }

    @Test
    fun mealPlanWithNotes_workflow() = runTest {
        val recipe = Recipe(
            id = "recipe-1",
            name = "测试菜谱",
            cookingTime = 30,
            servings = 4,
            difficulty = DifficultyLevel.MEDIUM
        )
        recipeRepository.insertRecipe(recipe)

        val mealPlanWithNotes = MealPlan(
            id = "meal-1",
            date = System.currentTimeMillis(),
            mealType = MealType.LUNCH,
            recipeId = recipe.id,
            servings = 4,
            notes = "少放盐，多放葱，不要辣"
        )

        viewModel.addMealPlan(mealPlanWithNotes)

        // Verify notes are saved
        val plans = mealPlanRepository.getAllMealPlans().first()
        assertEquals("少放盐，多放葱，不要辣", plans[0].notes)
    }

    @Test
    fun getMealPlansForDateRange_workflow() = runTest {
        val recipe = Recipe(
            id = "recipe-1",
            name = "测试菜谱",
            cookingTime = 30,
            servings = 4,
            difficulty = DifficultyLevel.MEDIUM
        )
        recipeRepository.insertRecipe(recipe)

        val now = System.currentTimeMillis()

        // Create meal plans for 10 days
        (0..9).forEach { day ->
            val date = now + (day * 86400000)
            val mealPlan = MealPlan(
                id = "meal-$day",
                date = date,
                mealType = MealType.DINNER,
                recipeId = recipe.id,
                servings = 2
            )
            viewModel.addMealPlan(mealPlan)
        }

        // Get meal plans for first 5 days
        val startDate = now
        val endDate = now + (4 * 86400000)

        val rangePlans = mealPlanRepository.getMealPlansForDateRange(startDate, endDate).first()
        assertEquals(5, rangePlans.size)
    }
}
