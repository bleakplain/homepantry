package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recipe_ratings")
data class RecipeRating(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String,
    val rating: Float, // 1-5 stars
    val review: String? = null,
    val cookedDate: Long = System.currentTimeMillis(),
    val wouldCookAgain: Boolean? = null
)
