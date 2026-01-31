package com.homepantry.viewmodel

import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.Ingredient
import com.homepantry.data.entity.IngredientCategory
import com.homepantry.data.entity.PantryItem
import com.homepantry.data.repository.IngredientRepository
import com.homepantry.ui.ingredient.IngredientUi
import com.homepantry.ui.ingredient.PantryItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IngredientViewModel(
    private val repository: IngredientRepository
) : BaseViewModel() {

    private val _pantryItems = MutableStateFlow<List<PantryItemUi>>(emptyList())
    val pantryItems: StateFlow<List<PantryItemUi>> = _pantryItems.asStateFlow()

    private val _allIngredients = MutableStateFlow<List<IngredientUi>>(emptyList())
    val allIngredients: StateFlow<List<IngredientUi>> = _allIngredients.asStateFlow()

    init {
        loadPantryItems()
        loadAllIngredients()
    }

    private fun loadPantryItems() {
        viewModelScope.launch {
            setLoading(true)
            try {
                repository.getPantryItems().collect { items ->
                    _pantryItems.value = items.map { it.toPantryItemUi() }
                    setLoading(false)
                }
            } catch (e: Exception) {
                setError("加载食材箱失败: ${e.message}")
                setLoading(false)
            }
        }
    }

    private fun loadAllIngredients() {
        launchInBackground {
            repository.getAllIngredients().collect { ingredients ->
                _allIngredients.value = ingredients.map { it.toIngredientUi() }
            }
        }
    }

    fun addPantryItem(
        name: String,
        quantity: Double,
        unit: String,
        expiryDays: Int?
    ) {
        execute("食材添加成功") {
            val ingredient = Ingredient(
                id = generateId(),
                name = name,
                unit = unit,
                category = IngredientCategory.OTHER
            )
            repository.addIngredient(ingredient)

            val pantryItem = PantryItem(
                id = generateId(),
                ingredientId = ingredient.id,
                quantity = quantity,
                expiryDate = expiryDays?.let { System.currentTimeMillis() + it * 24 * 60 * 60 * 1000L }
            )
            repository.addPantryItem(pantryItem)
        }
    }

    fun deletePantryItem(itemId: String) {
        execute("食材删除成功") {
            repository.deletePantryItem(itemId)
        }
    }

    private fun generateId(): String {
        return System.currentTimeMillis().toString()
    }
}

fun PantryItem.toPantryItemUi(ingredientName: String = "", icon: String = "🥕"): PantryItemUi {
    return PantryItemUi(
        id = id,
        name = ingredientName,
        quantity = quantity,
        unit = unit,
        expiryDays = expiryDate?.let {
            val days = ((it - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            days
        },
        icon = icon
    )
}

fun Ingredient.toIngredientUi(): IngredientUi {
    return IngredientUi(
        id = id,
        name = name,
        category = category,
        icon = getCategoryIcon(category)
    )
}

fun getCategoryIcon(category: IngredientCategory): String {
    return when (category) {
        IngredientCategory.VEGETABLE -> "🥬"
        IngredientCategory.FRUIT -> "🍎"
        IngredientCategory.MEAT -> "🥩"
        IngredientCategory.SEAFOOD -> "🐟"
        IngredientCategory.DAIRY -> "🥛"
        IngredientCategory.GRAIN -> "🌾"
        IngredientCategory.SPICE -> "🧂"
        IngredientCategory.SAUCE -> "🫙"
        IngredientCategory.OTHER -> "🥕"
    }
}
