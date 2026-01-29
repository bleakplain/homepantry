package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String? = null,
    val color: String? = null, // Hex color code
    val sortOrder: Int = 0
)
