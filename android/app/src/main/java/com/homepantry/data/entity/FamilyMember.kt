package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 家庭成员
 */
@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val relationship: String,                       // 关系 (自己/配偶/孩子/父母)
    val age: Int? = null,
    val preferredCuisines: String = "[]",          // JSON: 喜欢的菜系
    val dislikedIngredients: String = "[]",       // JSON: 不喜欢的食材
    val allergies: String = "[]",                 // JSON: 过敏原
    val favoriteRecipes: String = "[]",           // JSON: 喜欢的菜谱 ID
    val dislikedRecipes: String = "[]",           // JSON: 不喜欢的菜谱 ID
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
