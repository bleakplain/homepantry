package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 食材库存
 */
@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ingredientId: String,
    val name: String,                              // 食材名称
    val quantity: Double,
    val unit: String,                              // 单位
    val purchaseDate: Long? = null,                // 购买日期
    val expiryDate: Long? = null,                  // 保质期 (Unix timestamp)
    val storageLocation: StorageLocation = StorageLocation.PANTRY, // 存放位置
    val notes: String? = null
)

/**
 * 存储位置
 */
enum class StorageLocation {
    FRIDGE,         // 冷藏
    FREEZER,        // 冷冻
    PANTRY,         // 常温 (储藏室)
    OTHER           // 其他
}
