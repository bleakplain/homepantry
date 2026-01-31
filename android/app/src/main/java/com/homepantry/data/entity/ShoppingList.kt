package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 购物清单
 */
@Entity(
    tableName = "shopping_lists",
    indices = [Index("date"), Index("isCompleted")]
)
data class ShoppingList(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val items: String = "[]",                    // JSON: ShoppingItem 列表
    val isCompleted: Boolean = false,
    val totalEstimated: Double? = null,          // 预计花费
    val actualTotal: Double? = null,             // 实际花费
    val store: String? = null,                   // 购物地点
    val mealPlanIds: String = "[]",              // JSON: 关联的菜单计划 ID
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * 购物清单项
 */
@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId"), Index("category")]
)
data class ShoppingItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val listId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,                         // 分类 (蔬菜/肉类/调料等)
    val estimatedPrice: Double? = null,
    val actualPrice: Double? = null,
    val isPurchased: Boolean = false,
    val isChecked: Boolean = false,
    val notes: String? = null,
    val sortOrder: Int = 0,
    val recipeIds: String = "[]"                 // JSON: 需要此食材的菜谱 ID
)

/**
 * 购物清单分类
 */
enum class ShoppingCategory {
    VEGETABLES,     // 蔬菜
    MEAT,           // 肉类
    SEAFOOD,        // 海鲜
    DAIRY,          // 乳制品
    DRY_GOODS,      // 干货
    CONDIMENTS,     // 调料
    FRUITS,         // 水果
    SNACKS,         // 零食
    BEVERAGES,      // 饮料
    OTHER           // 其他
}
