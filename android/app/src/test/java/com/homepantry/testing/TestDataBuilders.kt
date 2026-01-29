package com.homepantry.testing

import com.homepantry.data.entity.*

/**
 * Test data builders for creating test instances of domain models.
 * Provides a fluent API for creating test data with sensible defaults.
 */
object TestDataBuilders {

    fun createRecipe(
        id: String = "recipe-${System.currentTimeMillis()}",
        name: String = "测试菜谱",
        description: String? = "测试描述",
        cookingTime: Int = 30,
        servings: Int = 4,
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        isFavorite: Boolean = false,
        categoryId: String? = null,
        imageUrl: String? = null,
        createdAt: Long = System.currentTimeMillis()
    ) = Recipe(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        cookingTime = cookingTime,
        servings = servings,
        difficulty = difficulty,
        isFavorite = isFavorite,
        categoryId = categoryId,
        createdAt = createdAt
    )

    fun createIngredient(
        id: String = "ing-${System.currentTimeMillis()}",
        name: String = "番茄",
        unit: String = "个",
        category: IngredientCategory = IngredientCategory.VEGETABLE
    ) = Ingredient(
        id = id,
        name = name,
        unit = unit,
        category = category
    )

    fun createRecipeIngredient(
        id: String = "ri-${System.currentTimeMillis()}",
        recipeId: String = "recipe-1",
        ingredientId: String = "ing-1",
        quantity: Double = 2.0,
        notes: String? = null
    ) = RecipeIngredient(
        id = id,
        recipeId = recipeId,
        ingredientId = ingredientId,
        quantity = quantity,
        notes = notes
    )

    fun createRecipeInstruction(
        id: String = "inst-${System.currentTimeMillis()}",
        recipeId: String = "recipe-1",
        stepNumber: Int = 1,
        instructionText: String = "第一步"
    ) = RecipeInstruction(
        id = id,
        recipeId = recipeId,
        stepNumber = stepNumber,
        instructionText = instructionText
    )

    fun createPantryItem(
        id: String = "pantry-${System.currentTimeMillis()}",
        ingredientId: String = "ing-1",
        quantity: Double = 5.0,
        unit: String = "个",
        expiryDate: Long? = System.currentTimeMillis() + 86400000 * 3 // 3 days from now
    ) = PantryItem(
        id = id,
        ingredientId = ingredientId,
        quantity = quantity,
        unit = unit,
        expiryDate = expiryDate
    )

    fun createMealPlan(
        id: String = "meal-${System.currentTimeMillis()}",
        date: Long = System.currentTimeMillis(),
        mealType: MealType = MealType.LUNCH,
        recipeId: String = "recipe-1",
        servings: Int = 2,
        notes: String? = null
    ) = MealPlan(
        id = id,
        date = date,
        mealType = mealType,
        recipeId = recipeId,
        servings = servings,
        notes = notes
    )

    fun createCategory(
        id: String = "cat-${System.currentTimeMillis()}",
        name: String = "家常菜",
        icon: String? = "🍳",
        color: String? = "#FF5722",
        sortOrder: Int = 0
    ) = Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        sortOrder = sortOrder
    )

    fun createRecipeRating(
        id: String = "rating-${System.currentTimeMillis()}",
        recipeId: String = "recipe-1",
        rating: Float = 4.5f,
        review: String? = "很好吃",
        cookedDate: Long = System.currentTimeMillis(),
        wouldCookAgain: Boolean = true
    ) = RecipeRating(
        id = id,
        recipeId = recipeId,
        rating = rating,
        review = review,
        cookedDate = cookedDate,
        wouldCookAgain = wouldCookAgain
    )

    /**
     * Create a list of test recipes
     */
    fun createRecipeList(count: Int): List<Recipe> {
        return (1..count).map { index ->
            createRecipe(
                id = "recipe-$index",
                name = "测试菜谱 $index",
                difficulty = when (index % 3) {
                    0 -> DifficultyLevel.EASY
                    1 -> DifficultyLevel.MEDIUM
                    else -> DifficultyLevel.HARD
                }
            )
        }
    }

    /**
     * Create a list of test ingredients
     */
    fun createIngredientList(count: Int): List<Ingredient> {
        val categories = IngredientCategory.values()
        return (1..count).map { index ->
            createIngredient(
                id = "ing-$index",
                name = "食材$index",
                category = categories[index % categories.size]
            )
        }
    }

    /**
     * Create a complete recipe with ingredients and instructions
     */
    fun createCompleteRecipe(
        recipe: Recipe = createRecipe(),
        ingredients: List<RecipeIngredient> = listOf(createRecipeIngredient()),
        instructions: List<RecipeInstruction> = listOf(createRecipeInstruction())
    ): TestDataBuilders.CompleteRecipe {
        return TestDataBuilders.CompleteRecipe(recipe, ingredients, instructions)
    }

    data class CompleteRecipe(
        val recipe: Recipe,
        val ingredients: List<RecipeIngredient>,
        val instructions: List<RecipeInstruction>
    )
}

/**
 * Constants for testing
 */
object TestConstants {
    // Test timeouts
    const val TEST_TIMEOUT_SHORT = 1000L
    const val TEST_TIMEOUT_MEDIUM = 3000L
    const val TEST_TIMEOUT_LONG = 5000L

    // Test data sizes
    const val TEST_RECIPE_COUNT = 10
    const val TEST_INGREDIENT_COUNT = 20
    const val TEST_MEAL_PLAN_COUNT = 7

    // Test strings
    const val EMPTY_STRING = ""
    const val TEST_SEARCH_QUERY = "番茄"
    const val TEST_SPECIAL_CHARACTERS = "!@#$%^&*()"

    // Test values
    const val TEST_COOKING_TIME = 30
    const val TEST_SERVINGS = 4
    const val TEST_QUANTITY = 2.0

    // Test dates
    val TEST_PAST_DATE = System.currentTimeMillis() - 86400000
    val TEST_FUTURE_DATE = System.currentTimeMillis() + 86400000 * 7
}

/**
 * Assertions for testing
 */
object TestAssertions {
    /**
     * Assert that a recipe has the expected values
     */
    fun assertRecipeEquals(
        expected: Recipe,
        actual: Recipe?,
        message: String? = null
    ) {
        if (actual == null) {
            throw AssertionError("$message: Recipe is null")
        }
        if (expected.id != actual.id) {
            throw AssertionError("$message: ID mismatch - expected ${expected.id}, got ${actual.id}")
        }
        if (expected.name != actual.name) {
            throw AssertionError("$message: Name mismatch - expected ${expected.name}, got ${actual.name}")
        }
        if (expected.cookingTime != actual.cookingTime) {
            throw AssertionError("$message: Cooking time mismatch - expected ${expected.cookingTime}, got ${actual.cookingTime}")
        }
        if (expected.servings != actual.servings) {
            throw AssertionError("$message: Servings mismatch - expected ${expected.servings}, got ${actual.servings}")
        }
        if (expected.difficulty != actual.difficulty) {
            throw AssertionError("$message: Difficulty mismatch - expected ${expected.difficulty}, got ${actual.difficulty}")
        }
        if (expected.isFavorite != actual.isFavorite) {
            throw AssertionError("$message: Favorite status mismatch - expected ${expected.isFavorite}, got ${actual.isFavorite}")
        }
    }

    /**
     * Assert that a list contains expected elements
     */
    fun <T> assertListContains(
        list: List<T>,
        item: T,
        message: String? = null
    ) {
        if (item !in list) {
            throw AssertionError("$message: List does not contain expected item: $item")
        }
    }

    /**
     * Assert that a list has the expected size
     */
    fun <T> assertListSize(
        list: List<T>,
        expectedSize: Int,
        message: String? = null
    ) {
        if (list.size != expectedSize) {
            throw AssertionError("$message: List size mismatch - expected $expectedSize, got ${list.size}")
        }
    }
}
