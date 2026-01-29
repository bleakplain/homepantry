package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.launch

class IngredientViewModel(
    private val repository: IngredientRepository
) : ViewModel() {

    private val _pantryItems = MutableStateFlow<List<PantryItemUi>>(emptyList())
    val pantryItems: StateFlow<List<PantryItemUi>> = _pantryItems.asStateFlow()

    private val _allIngredients = MutableStateFlow<List<IngredientUi>>(emptyList())
    val allIngredients: StateFlow<List<IngredientUi>> = _allIngredients.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadPantryItems()
        loadAllIngredients()
    }

    private fun loadPantryItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getPantryItems().collect { items ->
                    _pantryItems.value = items.map { it.toPantryItemUi() }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "加载食材箱失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun loadAllIngredients() {
        viewModelScope.launch {
            try {
                repository.getAllIngredients().collect { ingredients ->
                    _allIngredients.value = ingredients.map { it.toIngredientUi() }
                }
            } catch (e: Exception) {
                _error.value = "加载食材库失败: ${e.message}"
            }
        }
    }

    fun addPantryItem(
        name: String,
        quantity: Double,
        unit: String,
        expiryDays: Int?
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
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
                _successMessage.value = "食材添加成功"
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "添加食材失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deletePantryItem(itemId: String) {
        viewModelScope.launch {
            try {
                repository.deletePantryItem(itemId)
                _successMessage.value = "食材删除成功"
            } catch (e: Exception) {
                _error.value = "删除食材失败: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
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
