package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.MealPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans ORDER BY date ASC")
    fun getAllMealPlans(): Flow<List<MealPlan>>

    @Query("SELECT * FROM meal_plans WHERE date >= :startDate AND date < :endDate ORDER BY date ASC")
    fun getMealPlansForWeek(startDate: Long, endDate: Long): Flow<List<MealPlan>>

    @Query("SELECT * FROM meal_plans WHERE date = :date ORDER BY mealType ASC")
    fun getMealPlansForDate(date: Long): Flow<List<MealPlan>>

    @Query("SELECT * FROM meal_plans WHERE id = :mealPlanId")
    suspend fun getMealPlanById(mealPlanId: String): MealPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan)

    @Update
    suspend fun updateMealPlan(mealPlan: MealPlan)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlan)

    @Query("DELETE FROM meal_plans WHERE date = :date")
    suspend fun deleteMealPlansForDate(date: Long)
}
