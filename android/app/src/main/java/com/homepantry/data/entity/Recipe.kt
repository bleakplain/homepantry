package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val cookingTime: Int, // in minutes
    val servings: Int,
    val difficulty: DifficultyLevel,
    val categoryId: String? = null,
    val tags: String = "", // JSON array as string
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String? = null, // Family member ID
    val isFavorite: Boolean = false,
    val favoritePosition: Int? = null // For custom ordering
)

enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}
