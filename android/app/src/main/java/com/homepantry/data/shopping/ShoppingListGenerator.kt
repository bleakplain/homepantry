package com.homepantry.data.shopping

import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.dao.MealPlanDao
import com.homepantry.data.dao.PantryItem
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.MealPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class ShoppingItem(
    val ingredientId: String,
    val ingredientName: String,
    val unit: String,
    val neededQuantity: Double,
    val availableQuantity: Double = 0.0,
    val toBuyQuantity: Double = 0.0,
    val category: String
)

data class ShoppingList(
    val id: String,
    val name: String,
    val items: List<ShoppingItem>,
    val totalItems: Int,
    val estimatedCost: Double? = null
)

class ShoppingListGenerator(
    private val recipeDao: RecipeDao,
    private val mealPlanDao: MealPlanDao,
    private val ingredientDao: IngredientDao
) {

    suspend fun generateShoppingList(
        startDate: Long,
        endDate: Long
    ): Result<ShoppingList> {
        return try {
            // Get meal plans for the date range
            val mealPlans = mutableListOf<MealPlan>()
            mealPlanDao.getMealPlansForWeek(startDate, endDate)
                .collect { plans ->
                    mealPlans.addAll(plans)
                }

            // Get all recipe ingredients needed
            val neededIngredients = mutableMapOf<String, Double>()
            mealPlans.forEach { mealPlan ->
                val ingredients = recipeDao.getRecipeIngredients(mealPlan.recipeId)
                ingredients.forEach { recipeIngredient ->
                    val currentQuantity = neededIngredients[recipeIngredient.ingredientId] ?: 0.0
                    neededIngredients[recipeIngredient.ingredientId] =
                        currentQuantity + (recipeIngredient.quantity * mealPlan.servings / 2) // Assuming base recipe is for 2
                }
            }

            // Get available pantry items
            val availableIngredients = mutableMapOf<String, Double>()
            ingredientDao.getAllPantryItems().collect { pantryItems ->
                pantryItems.forEach { pantryItem ->
                    val currentQuantity = availableIngredients[pantryItem.ingredientId] ?: 0.0
                    availableIngredients[pantryItem.ingredientId] =
                        currentQuantity + pantryItem.quantity
                }
            }

            // Calculate shopping list
            val shoppingItems = mutableListOf<ShoppingItem>()
            neededIngredients.forEach { (ingredientId, neededQty) ->
                val availableQty = availableIngredients[ingredientId] ?: 0.0
                val toBuyQty = maxOf(0.0, neededQty - availableQty)

                if (toBuyQty > 0) {
                    val ingredient = ingredientDao.getIngredientById(ingredientId)
                    ingredient?.let {
                        shoppingItems.add(
                            ShoppingItem(
                                ingredientId = it.id,
                                ingredientName = it.name,
                                unit = it.unit,
                                neededQuantity = neededQty,
                                availableQuantity = availableQty,
                                toBuyQuantity = toBuyQty,
                                category = it.category.name
                            )
                        )
                    }
                }
            }

            // Sort by category
            shoppingItems.sortBy { it.category }

            val shoppingList = ShoppingList(
                id = "list-${System.currentTimeMillis()}",
                name = "购物清单 ${formatDate(startDate)}",
                items = shoppingItems,
                totalItems = shoppingItems.size
            )

            Result.success(shoppingList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateShoppingListForRecipes(
        recipeIds: List<String>
    ): Result<ShoppingList> {
        return try {
            val neededIngredients = mutableMapOf<String, Double>()

            // Get all ingredients from selected recipes
            recipeIds.forEach { recipeId ->
                val ingredients = recipeDao.getRecipeIngredients(recipeId)
                ingredients.forEach { recipeIngredient ->
                    val currentQuantity = neededIngredients[recipeIngredient.ingredientId] ?: 0.0
                    neededIngredients[recipeIngredient.ingredientId] =
                        currentQuantity + recipeIngredient.quantity
                }
            }

            // Get available pantry items
            val availableIngredients = mutableMapOf<String, Double>()
            ingredientDao.getAllPantryItems().collect { pantryItems ->
                pantryItems.forEach { pantryItem ->
                    val currentQuantity = availableIngredients[pantryItem.ingredientId] ?: 0.0
                    availableIngredients[pantryItem.ingredientId] =
                        currentQuantity + pantryItem.quantity
                }
            }

            // Calculate shopping list
            val shoppingItems = mutableListOf<ShoppingItem>()
            neededIngredients.forEach { (ingredientId, neededQty) ->
                val availableQty = availableIngredients[ingredientId] ?: 0.0
                val toBuyQty = maxOf(0.0, neededQty - availableQty)

                if (toBuyQty > 0) {
                    val ingredient = ingredientDao.getIngredientById(ingredientId)
                    ingredient?.let {
                        shoppingItems.add(
                            ShoppingItem(
                                ingredientId = it.id,
                                ingredientName = it.name,
                                unit = it.unit,
                                neededQuantity = neededQty,
                                availableQuantity = availableQty,
                                toBuyQuantity = toBuyQty,
                                category = it.category.name
                            )
                        )
                    }
                }
            }

            // Sort by category
            shoppingItems.sortBy { it.category }

            val shoppingList = ShoppingList(
                id = "list-${System.currentTimeMillis()}",
                name = "购物清单",
                items = shoppingItems,
                totalItems = shoppingItems.size
            )

            Result.success(shoppingList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
        return format.format(date)
    }
}
