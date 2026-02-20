package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.ShoppingListItem
import com.homepantry.data.repository.ShoppingListRepository
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 购物列表视图模型
 */
class ShoppingListViewModel(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ShoppingListViewModel"
    }

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "ShoppingListViewModel init")
        loadShoppingList()
    }

    /**
     * 加载购物列表
     */
    private fun loadShoppingList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadShoppingList") {
                Logger.enter("loadShoppingList")

                shoppingListRepository.getAllShoppingListItems()
                    .collect { items ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                items = items
                            )
                        }
                        Logger.d(TAG, "加载购物列表成功：${items.size} 个")
                    }

                Logger.exit("loadShoppingList")
            }
        }
    }

    /**
     * 添加购物列表项
     */
    fun addShoppingListItem(
        name: String,
        recipeId: String? = null,
        quantity: Int = 1,
        unit: String = "个"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("addShoppingListItem") {
                Logger.enter("ShoppingListViewModel.addShoppingListItem", name, recipeId)

                val item = ShoppingListItem(
                    id = "shopping_item_${java.util.UUID.randomUUID().toString()}",
                    name = name,
                    recipeId = recipeId,
                    quantity = quantity,
                    unit = unit,
                    isPurchased = false,
                    createdAt = System.currentTimeMillis()
                )

                shoppingListRepository.addShoppingListItem(item)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                items = it.items + it,
                                successMessage = "添加成功"
                            )
                        }
                        Logger.d("ShoppingListViewModel.addShoppingListItem", "添加成功")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "添加失败：${e.message}"
                            )
                        }
                        Logger.e("ShoppingListViewModel.addShoppingListItem", "添加失败", e)
                    }

                Logger.exit("ShoppingListViewModel.addShoppingListItem")
            }
        }
    }

    /**
     * 标记为已购买
     */
    fun markAsPurchased(itemId: String) {
        viewModelScope.launch {
            PerformanceMonitor.recordMethodPerformance("markAsPurchased") {
                Logger.enter("ShoppingListViewModel.markAsPurchased", itemId)

                shoppingListRepository.markAsPurchased(itemId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                items = it.items.map { if (it.id == itemId) it.copy(isPurchased = true) else it },
                                successMessage = "标记成功"
                            )
                        }
                        Logger.d("ShoppingListViewModel.markAsPurchased", "标记成功：$itemId")
                    }
                    .onFailure { e ->
                        Logger.e("ShoppingListViewModel.markAsPurchased", "标记失败：$itemId", e)
                    }

                Logger.exit("ShoppingListViewModel.markAsPurchased")
            }
        }
    }

    /**
     * 删除购物列表项
     */
    fun deleteShoppingListItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("deleteShoppingListItem") {
                Logger.enter("ShoppingListViewModel.deleteShoppingListItem", itemId)

                shoppingListRepository.deleteShoppingListItem(itemId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                items = it.items.filter { it.id != itemId },
                                successMessage = "删除成功"
                            )
                        }
                        Logger.d("ShoppingListViewModel.deleteShoppingListItem", "删除成功：$itemId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "删除失败：${e.message}"
                            )
                        }
                        Logger.e("ShoppingListViewModel.deleteShoppingListItem", "删除失败：$itemId", e)
                    }

                Logger.exit("ShoppingListViewModel.deleteShoppingListItem")
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "ShoppingListViewModel onCleared")
    }
}

/**
 * 购物列表 UI 状态
 */
data class ShoppingListUiState(
    val isLoading: Boolean = false,
    val items: List<ShoppingListItem> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
