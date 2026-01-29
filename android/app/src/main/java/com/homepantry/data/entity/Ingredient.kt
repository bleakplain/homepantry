package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val unit: String, // g, ml, piece, etc.
    val category: IngredientCategory,
    val shelfLifeDays: Int? = null,
    val iconUrl: String? = null
)

enum class IngredientCategory {
    VEGETABLE,
    FRUIT,
    MEAT,
    SEAFOOD,
    DAIRY,
    GRAIN,
    SPICE,
    SAUCE,
    OTHER
}
