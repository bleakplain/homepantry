package com.homepantry.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.entity.*
import com.homepantry.data.repository.*
import com.homepantry.viewmodel.MealPlanViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ShoppingListIntegrationTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var mealPlanRepository: MealPlanRepository
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var ingredientRepository: IngredientRepository
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
        ingredientRepository = IngredientRepository(
            database.ingredientDao(),
            database.recipeDao()
        )
        viewModel = MealPlanViewModel(mealPlanRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun generateShoppingListFromMealPlan_workflow() = runTest {
        // Create ingredients
        val tomato = Ingredient(
            id = "ing-tomato",
            name = "番茄",
            unit = "个",
            category = IngredientCategory.VEGETABLE
        )
        val egg = Ingredient(
            id = "ing-egg",
            name = "鸡蛋",
            unit = "个",
            category = IngredientCategory.OTHER
        )

        ingredientRepository.insertIngredient(tomato)
        ingredientRepository.insertIngredient(egg)

        // Create recipe with ingredients
        val recipe = Recipe(
            id = "recipe-1",
            name = "番茄炒蛋",
            description = "经典家常菜",
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY
        )

        recipeRepository.insertRecipe(recipe)

        val recipeIngredients = listOf(
            RecipeIngredient(
                recipeId = recipe.id,
                ingredientId = tomato.id,
                quantity = 2.0,
                unit = tomato.unit
            ),
            RecipeIngredient(
                recipeId = recipe.id,
                ingredientId = egg.id,
                quantity = 3.0,
                unit = egg.unit
            )
        )

        recipeIngredients.forEach { database.recipeDao().insertRecipeIngredient(it) }

        // Create meal plan
        val today = System.currentTimeMillis()
        val mealPlan = MealPlan(
            id = "meal-1",
            date = today,
            mealType = MealType.LUNCH,
            recipeId = recipe.id,
            servings = 4  // Double the recipe's servings
        )

        viewModel.addMealPlan(mealPlan)

        // Generate shopping list
        val mealPlans = mealPlanRepository.getMealPlansForDate(today).first()
        val shoppingList = mutableListOf<ShoppingListItem>()

        mealPlans.forEach { plan ->
            val ingredients = database.recipeDao().getRecipeIngredients(plan.recipeId).first()
            val recipe = recipeRepository.getRecipeById(plan.recipeId)

            val scaleFactor = if (recipe != null) plan.servings.toFloat() / recipe.servings else 1f

            ingredients.forEach { ingredient ->
                val adjustedQuantity = ingredient.quantity * scaleFactor
                shoppingList.add(
                    ShoppingListItem(
                        ingredientId = ingredient.ingredientId,
                        name = ingredient.ingredientId,  // In real app, would join with ingredients table
                        quantity = adjustedQuantity,
                        unit = ingredient.unit ?: "",
                        forRecipe = recipe?.name ?: ""
                    )
                )
            }
        }

        // Verify shopping list
        assertEquals(2, shoppingList.size)

        // Verify quantities are scaled
        val tomatoItem = shoppingList.find { it.ingredientId == tomato.id }
        assertNotNull(tomatoItem)
        assertEquals(4.0, tomatoItem.quantity)  // 2 * (4/2) = 4

        val eggItem = shoppingList.find { it.ingredientId == egg.id }
        assertNotNull(eggItem)
        assertEquals(6.0, eggItem.quantity)  // 3 * (4/2) = 6
    }

    @Test
    fun generateShoppingListForMultipleMeals_workflow() = runTest {
        // Create ingredients
        val ingredients = listOf(
            Ingredient(id = "ing1", name = "番茄", unit = "个", category = IngredientCategory.VEGETABLE),
            Ingredient(id = "ing2", name = "鸡蛋", unit = "个", category = IngredientCategory.OTHER),
            Ingredient(id = "ing3", name = "猪肉", unit = "克", category = IngredientCategory.MEAT),
            Ingredient(id = "ing4", name = "大米", unit = "克", category = IngredientCategory.GRAIN)
        )

        ingredients.forEach { ingredientRepository.insertIngredient(it) }

        // Create recipes
        val recipe1 = Recipe(id = "r1", name = "番茄炒蛋", cookingTime = 15, servings = 2, difficulty = DifficultyLevel.EASY)
        val recipe2 = Recipe(id = "r2", name = "红烧肉", cookingTime = 60, servings = 4, difficulty = DifficultyLevel.HARD)

        listOf(recipe1, recipe2).forEach { recipeRepository.insertRecipe(it) }

        // Add ingredients to recipes
        database.recipeDao().insertRecipeIngredient(
            RecipeIngredient(recipe1.id, "ing1", 2.0, "个")
        )
        database.recipeDao().insertRecipeIngredient(
            RecipeIngredient(recipe1.id, "ing2", 3.0, "个")
        )
        database.recipeDao().insertRecipeIngredient(
            RecipeIngredient(recipe2.id, "ing3", 500.0, "克")
        )
        database.recipeDao().insertRecipeIngredient(
            RecipeIngredient(recipe2.id, "ing4", 200.0, "克")
        )

        // Plan both meals for today
        val today = System.currentTimeMillis()

        val meal1 = MealPlan(
            id = "m1",
            date = today,
            mealType = MealType.LUNCH,
            recipeId = recipe1.id,
            servings = 2
        )

        val meal2 = MealPlan(
            id = "m2",
            date = today,
            mealType = MealType.DINNER,
            recipeId = recipe2.id,
            servings = 6
        )

        viewModel.addMealPlan(meal1)
        viewModel.addMealPlan(meal2)

        // Generate shopping list for both meals
        val mealPlans = mealPlanRepository.getMealPlansForDate(today).first()
        val shoppingList = mutableListOf<ShoppingListItem>()

        mealPlans.forEach { plan ->
            val ingredients = database.recipeDao().getRecipeIngredients(plan.recipeId).first()
            val recipe = recipeRepository.getRecipeById(plan.recipeId)

            val scaleFactor = if (recipe != null) plan.servings.toFloat() / recipe.servings else 1f

            ingredients.forEach { ingredient ->
                val existing = shoppingList.find { it.ingredientId == ingredient.ingredientId }
                val adjustedQuantity = ingredient.quantity * scaleFactor

                if (existing != null) {
                    // Sum quantities for same ingredient
                    existing.quantity += adjustedQuantity
                    existing.forRecipe += ", ${recipe?.name}"
                } else {
                    shoppingList.add(
                        ShoppingListItem(
                            ingredientId = ingredient.ingredientId,
                            name = ingredient.ingredientId,
                            quantity = adjustedQuantity,
                            unit = ingredient.unit ?: "",
                            forRecipe = recipe?.name ?: ""
                        )
                    )
                }
            }
        }

        // Verify shopping list
        assertEquals(4, shoppingList.size)

        // Verify quantities
        val tomato = shoppingList.find { it.ingredientId == "ing1" }
        assertEquals(2.0, tomato?.quantity)

        val pork = shoppingList.find { it.ingredientId == "ing3" }
        assertEquals(750.0, pork?.quantity)  // 500 * (6/4) = 750
    }

    @Test
    fun generateShoppingListForWeek_workflow() = runTest {
        // Create a simple recipe
        val ingredient = Ingredient(
            id = "ing1",
            name = "鸡蛋",
            unit = "个",
            category = IngredientCategory.OTHER
        )

        ingredientRepository.insertIngredient(ingredient)

        val recipe = Recipe(
            id = "recipe-1",
            name = "炒蛋",
            cookingTime = 10,
            servings = 2,
            difficulty = DifficultyLevel.EASY
        )

        recipeRepository.insertRecipe(recipe)

        database.recipeDao().insertRecipeIngredient(
            RecipeIngredient(recipe.id, ingredient.id, 2.0, "个")
        )

        // Plan meals for a week
        val now = System.currentTimeMillis()
        val dayInMillis = 86400000

        for (day in 0..6) {
            val date = now + (day * dayInMillis)
            val mealPlan = MealPlan(
                id = "meal-$day",
                date = date,
                mealType = MealType.BREAKFAST,
                recipeId = recipe.id,
                servings = 4  // Family of 4
            )
            viewModel.addMealPlan(mealPlan)
        }

        // Generate shopping list for the week
        val weekStart = now
        val weekEnd = now + (7 * dayInMillis)

        val mealPlans = mealPlanRepository.getMealPlansForWeek(weekStart, weekEnd).first()

        val totalEggsNeeded = mealPlans.sumOf { plan ->
            val scaleFactor = plan.servings.toFloat() / 2  // Recipe serves 2
            2.0 * scaleFactor  // 2 eggs per recipe
        }

        // Should need 28 eggs for the week (7 days * 4 servings / 2 per recipe * 2 eggs)
        assertEquals(28.0, totalEggsNeeded)
    }

    @Test
    fun checkPantryBeforeShopping_workflow() = runTest {
        // Create ingredient
        val tomato = Ingredient(
            id = "ing-tomato",
            name = "番茄",
            unit = "个",
            category = IngredientCategory.VEGETABLE
        )

        ingredientRepository.insertIngredient(tomato)

        // Add some tomatoes to pantry
        val pantryItem = PantryItem(
            id = "pantry-1",
            ingredientId = tomato.id,
            quantity = 5.0,
            unit = tomato.unit
        )

        ingredientRepository.addPantryItem(pantryItem)

        // Create recipe requiring tomatoes
        val recipe = Recipe(
            id = "recipe-1",
            name = "番茄炒蛋",
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY
        )

        recipeRepository.insertRecipe(recipe)

        database.recipeDao().insertRecipeIngredient(
            RecipeIngredient(recipe.id, tomato.id, 10.0, "个")
        )

        // Plan the meal
        val mealPlan = MealPlan(
            id = "meal-1",
            date = System.currentTimeMillis(),
            mealType = MealType.LUNCH,
            recipeId = recipe.id,
            servings = 2
        )

        viewModel.addMealPlan(mealPlan)

        // Check what's needed from shopping
        val pantryItems = ingredientRepository.getAllPantryItems().first()
        val recipeIngredients = database.recipeDao().getRecipeIngredients(recipe.id).first()

        val shoppingList = mutableListOf<ShoppingListItem>()

        recipeIngredients.forEach { ingredient ->
            val inPantry = pantryItems.filter { it.ingredientId == ingredient.ingredientId }
            val pantryQuantity = inPantry.sumOf { it.quantity }
            val needed = ingredient.quantity - pantryQuantity

            if (needed > 0) {
                shoppingList.add(
                    ShoppingListItem(
                        ingredientId = ingredient.ingredientId,
                        name = ingredient.ingredientId,
                        quantity = needed,
                        unit = ingredient.unit ?: "",
                        haveInPantry = pantryQuantity,
                        forRecipe = recipe.name
                    )
                )
            }
        }

        // Should only need 5 more tomatoes (10 - 5 in pantry)
        assertEquals(1, shoppingList.size)
        assertEquals(5.0, shoppingList[0].quantity)
        assertEquals(5.0, shoppingList[0].haveInPantry)
    }

    @Test
    fun generateShoppingListWithNotes_workflow() = runTest {
        // Create ingredient
        val ingredient = Ingredient(
            id = "ing1",
            name = "番茄",
            unit = "个",
            category = IngredientCategory.VEGETABLE
        )

        ingredientRepository.insertIngredient(ingredient)

        // Create recipe with ingredient note
        val recipe = Recipe(
            id = "recipe-1",
            name = "番茄炒蛋",
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY
        )

        recipeRepository.insertRecipe(recipe)

        database.recipeDao().insertRecipeIngredient(
            RecipeIngredient(
                recipeId = recipe.id,
                ingredientId = ingredient.id,
                quantity = 3.0,
                unit = "个",
                notes = "要成熟的，不要太硬"
            )
        )

        // Create meal plan with notes
        val mealPlan = MealPlan(
            id = "meal-1",
            date = System.currentTimeMillis(),
            mealType = MealType.LUNCH,
            recipeId = recipe.id,
            servings = 2,
            notes = "多买几个备用"
        )

        viewModel.addMealPlan(mealPlan)

        // Generate shopping list
        val recipeIngredients = database.recipeDao().getRecipeIngredients(recipe.id).first()

        val shoppingList = recipeIngredients.map { ingredient ->
            ShoppingListItem(
                ingredientId = ingredient.ingredientId,
                name = ingredient.ingredientId,
                quantity = ingredient.quantity + 2,  // Add extra based on meal plan notes
                unit = ingredient.unit ?: "",
                notes = ingredient.notes,
                forRecipe = recipe.name
            )
        }

        // Verify shopping list includes notes
        assertEquals(1, shoppingList.size)
        assertEquals("要成熟的，不要太硬", shoppingList[0].notes)
    }

    // Helper data class for shopping list
    data class ShoppingListItem(
        val ingredientId: String,
        val name: String,
        var quantity: Double,
        val unit: String,
        val notes: String? = null,
        val forRecipe: String = "",
        val haveInPantry: Double = 0.0
    )
}
