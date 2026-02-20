package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.MealPlan
import com.homepantry.data.repository.MealPlanRepository
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 食谱计划视图模型
 */
class MealPlanViewModel(
    private val mealPlanRepository: MealPlanRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MealPlanViewModel"
    }

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "MealPlanViewModel init")
        loadMealPlans()
    }

    /**
     * 加载所有食谱计划
     */
    private fun loadMealPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadMealPlans") {
                Logger.enter("MealPlanViewModel.loadMealPlans")

                mealPlanRepository.getAllMealPlans()
                    .collect { mealPlans ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                mealPlans = mealPlans
                            )
                        }
                    }

                Logger.exit("MealPlanViewModel.loadMealPlans")
            }
        }
    }

    /**
     * 创建食谱计划
     */
    fun createMealPlan(
        name: String,
        meals: List<MealPlanItem>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("createMealPlan") {
                Logger.enter("MealPlanViewModel.createMealPlan", name, meals.size)

                mealPlanRepository.createMealPlan(name, meals)
                    .onSuccess { mealPlan ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                mealPlans = it.mealPlans + mealPlan,
                                successMessage = "食谱计划创建成功"
                            )
                        }
                        Logger.d(TAG, "食谱计划创建成功：${mealPlan.name}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "创建失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "食谱计划创建失败", e)
                    }

                Logger.exit("MealPlanViewModel.createMealPlan")
            }
        }
    }

    /**
     * 删除食谱计划
     */
    fun deleteMealPlan(mealPlanId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("deleteMealPlan") {
                Logger.enter("MealPlanViewModel.deleteMealPlan", mealPlanId)

                mealPlanRepository.deleteMealPlan(mealPlanId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                mealPlans = it.mealPlans.filter { it.id != mealPlanId },
                                successMessage = "删除成功"
                            )
                        }
                        Logger.d(TAG, "食谱计划删除成功：$mealPlanId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "删除失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "食谱计划删除失败：$mealPlanId", e)
                    }

                Logger.exit("MealPlanViewModel.deleteMealPlan")
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
        Logger.d(TAG, "MealPlanViewModel onCleared")
    }
}

/**
 * 食谱计划 UI 状态
 */
data class MealPlanUiState(
    val isLoading: Boolean = false,
    val mealPlans: List<MealPlan> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * 食谱计划项目
 */
data class MealPlanItem(
    val recipeId: String,
    val mealType: MealType
)

/**
 * 餐食类型
 */
enum class MealType {
    BREAKFAST, LUNCH, DINNER
}
