package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ingredientId: String,
    val quantity: Double,
    val expiryDate: Long? = null, // Unix timestamp
    val purchasedDate: Long = System.currentTimeMillis()
)
