package com.homepantry.data.repository

import com.homepantry.data.dao.MealPlanDao
import com.homepantry.data.entity.MealPlan
import kotlinx.coroutines.flow.Flow

class MealPlanRepository(private val mealPlanDao: MealPlanDao) {
    fun getAllMealPlans(): Flow<List<MealPlan>> = mealPlanDao.getAllMealPlans()

    fun getMealPlansForWeek(startDate: Long, endDate: Long? = null): Flow<List<MealPlan>> {
        val endDateValue = endDate ?: (startDate + 7 * 24 * 60 * 60 * 1000)
        return mealPlanDao.getMealPlansForWeek(startDate, endDateValue)
    }

    fun getMealPlansForDate(date: Long): Flow<List<MealPlan>> =
        mealPlanDao.getMealPlansForDate(date)

    suspend fun getMealPlanById(mealPlanId: String): MealPlan? =
        mealPlanDao.getMealPlanById(mealPlanId)

    suspend fun addMealPlan(mealPlan: MealPlan) = mealPlanDao.insertMealPlan(mealPlan)

    suspend fun updateMealPlan(mealPlan: MealPlan) = mealPlanDao.updateMealPlan(mealPlan)

    suspend fun deleteMealPlan(mealPlan: MealPlan) = mealPlanDao.deleteMealPlan(mealPlan)

    suspend fun deleteMealPlanById(mealPlanId: String) {
        mealPlanDao.getMealPlanById(mealPlanId)?.let { mealPlanDao.deleteMealPlan(it) }
    }

    suspend fun clearDayPlans(date: Long) = mealPlanDao.deleteMealPlansForDate(date)

    suspend fun copyDayToAnother(fromDate: Long, toDate: Long) {
        val plans = mealPlanDao.getMealPlansForDate(fromDate)
        // This would need to be implemented with a proper Flow collector
        // For now, this is a placeholder for the concept
    }
}
