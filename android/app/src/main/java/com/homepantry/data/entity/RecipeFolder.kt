package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 菜谱收藏夹关联实体
 */
@Entity(
    tableName = "recipe_folders",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["folderId"]),
        Index(value = ["recipeId", "folderId"], unique = true)
    ]
)
data class RecipeFolder(
    @PrimaryKey val id: String,
    val recipeId: String,
    val folderId: String,
    val addedAt: Long = System.currentTimeMillis()
)
