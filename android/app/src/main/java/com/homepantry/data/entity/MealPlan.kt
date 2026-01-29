package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long, // Unix timestamp
    val mealType: MealType,
    val recipeId: String,
    val servings: Int,
    val notes: String? = null
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}
