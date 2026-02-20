package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.Category
import com.homepantry.data.repository.CategoryRepository
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 分类视图模型
 */
class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CategoryViewModel"
    }

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "CategoryViewModel init")
        loadCategories()
    }

    /**
     * 加载所有分类
     */
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadCategories") {
                Logger.enter("CategoryViewModel.loadCategories")

                categoryRepository.getAllCategories()
                    .collect { categories ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categories = categories
                            )
                        }
                        Logger.d(TAG, "加载分类成功：${categories.size} 个")
                    }

                Logger.exit("CategoryViewModel.loadCategories")
            }
        }
    }

    /**
     * 创建分类
     */
    fun createCategory(
        name: String,
        icon: String? = null,
        color: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("createCategory") {
                Logger.enter("CategoryViewModel.createCategory", name, icon, color)

                categoryRepository.createCategory(name, icon, color)
                    .onSuccess { category ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categories = it.categories + category,
                                successMessage = "分类创建成功"
                            )
                        }
                        Logger.d("CategoryViewModel.createCategory", "分类创建成功")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "创建失败：${e.message}"
                            )
                        }
                        Logger.e("CategoryViewModel.createCategory", "分类创建失败", e)
                    }

                Logger.exit("CategoryViewModel.createCategory")
            }
        }
    }

    /**
     * 更新分类
     */
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("updateCategory") {
                Logger.enter("CategoryViewModel.updateCategory", category.id)

                categoryRepository.updateCategory(category)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categories = it.categories.map { if (it.id == category.id) category else it },
                                successMessage = "分类更新成功"
                            )
                        }
                        Logger.d("CategoryViewModel.updateCategory", "分类更新成功")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "更新失败：${e.message}"
                            )
                        }
                        Logger.e("CategoryViewModel.updateCategory", "分类更新失败", e)
                    }

                Logger.exit("CategoryViewModel.updateCategory")
            }
        }
    }

    /**
     * 删除分类
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("deleteCategory") {
                Logger.enter("CategoryViewModel.deleteCategory", categoryId)

                categoryRepository.deleteCategory(categoryId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categories = it.categories.filter { it.id != categoryId },
                                successMessage = "删除成功"
                            )
                        }
                        Logger.d("CategoryViewModel.deleteCategory", "分类删除成功：$categoryId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "删除失败：${e.message}"
                            )
                        }
                        Logger.e("CategoryViewModel.deleteCategory", "分类删除失败：$categoryId", e)
                    }

                Logger.exit("CategoryViewModel.deleteCategory")
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
        Logger.d(TAG, "CategoryViewModel onCleared")
    }
}

/**
 * 分类 UI 状态
 */
data class CategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
