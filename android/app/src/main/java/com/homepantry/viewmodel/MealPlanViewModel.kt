package com.homepantry.viewmodel

import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.MealPlan
import com.homepantry.data.entity.MealType
import com.homepantry.data.entity.Recipe
import com.homepantry.data.repository.MealPlanRepository
import com.homepantry.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class MealPlanViewModel(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository
) : BaseViewModel() {

    private val _mealPlans = MutableStateFlow<Map<Date, List<MealPlanUi>>>(emptyMap())
    val mealPlans: StateFlow<Map<Date, List<MealPlanUi>>> = _mealPlans.asStateFlow()

    private val _availableRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val availableRecipes: StateFlow<List<Recipe>> = _availableRecipes.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    init {
        loadMealPlans()
        loadRecipes()
    }

    private fun loadMealPlans() {
        viewModelScope.launch {
            setLoading(true)
            try {
                mealPlanRepository.getMealPlansForWeek(
                    startDate = getWeekStartDate(_selectedDate.value)
                ).collect { plans ->
                    val grouped = plans.groupBy { it.date }
                    _mealPlans.value = grouped.mapKeys { Date(it.key) }
                        .mapValues { entry ->
                            entry.value.map { plan -> plan.toMealPlanUi() }
                        }
                    setLoading(false)
                }
            } catch (e: Exception) {
                setError("加载餐食计划失败: ${e.message}")
                setLoading(false)
            }
        }
    }

    private fun loadRecipes() {
        launchInBackground {
            recipeRepository.getAllRecipes().collect { recipes ->
                _availableRecipes.value = recipes
            }
        }
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
        loadMealPlansForDate(date)
    }

    private fun loadMealPlansForDate(date: Date) {
        launchInBackground {
            try {
                mealPlanRepository.getMealPlansForDate(date.time).collect { plans ->
                    val dayPlans = plans.map { it.toMealPlanUi() }
                    val current = _mealPlans.value.toMutableMap()
                    current[date] = dayPlans
                    _mealPlans.value = current
                }
            } catch (e: Exception) {
                setError("加载餐食计划失败: ${e.message}")
            }
        }
    }

    fun addMealPlan(
        date: Date,
        mealType: MealType,
        recipeId: String,
        servings: Int,
        notes: String?
    ) {
        execute("餐食计划添加成功") {
            val mealPlan = MealPlan(
                id = generateId(),
                date = date.time,
                mealType = mealType,
                recipeId = recipeId,
                servings = servings,
                notes = notes
            )
            mealPlanRepository.addMealPlan(mealPlan)
            loadMealPlansForDate(date)
        }
    }

    fun updateMealPlan(
        mealPlanId: String,
        recipeId: String,
        servings: Int,
        notes: String?
    ) {
        execute("餐食计划更新成功") {
            val existing = mealPlanRepository.getMealPlanById(mealPlanId)
            if (existing != null) {
                val updated = existing.copy(
                    recipeId = recipeId,
                    servings = servings,
                    notes = notes
                )
                mealPlanRepository.updateMealPlan(updated)
            }
            loadMealPlansForDate(_selectedDate.value)
        }
    }

    fun deleteMealPlan(mealPlanId: String) {
        execute("餐食计划删除成功") {
            mealPlanRepository.deleteMealPlanById(mealPlanId)
            loadMealPlansForDate(_selectedDate.value)
        }
    }

    fun getMealPlansForDateAndType(date: Date, mealType: MealType): List<MealPlanUi> {
        return _mealPlans.value[date]?.filter { it.mealType == mealType } ?: emptyList()
    }

    private fun generateId(): String {
        return System.currentTimeMillis().toString()
    }

    private fun getWeekStartDate(date: Date): Long {
        val calendar = java.util.Calendar.getInstance().apply { time = date }
        calendar.firstDayOfWeek = java.util.Calendar.MONDAY
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        return calendar.timeInMillis
    }
}

data class MealPlanUi(
    val id: String,
    val date: Date,
    val mealType: MealType,
    val recipeId: String,
    val recipeName: String? = null,
    val servings: Int,
    val notes: String?
)

fun MealPlan.toMealPlanUi(recipeName: String? = null): MealPlanUi {
    return MealPlanUi(
        id = id,
        date = Date(date),
        mealType = mealType,
        recipeId = recipeId,
        recipeName = recipeName,
        servings = servings,
        notes = notes
    )
}
