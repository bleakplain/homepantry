# Plan: 洭物清单

**Spec ID**: 004
**功能名称**: 洭物清单
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9+ | 开发语言 |
| Jetpack Compose | 1.5+ | UI 框架 |
| Room | 2.6+ | 本地数据库 |
| Coroutines | 1.7+ | 异步处理 |
| Flow | Kotlin | 数据流 |

---

## 数据层设计

### Entity 定义

```kotlin
@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey
    val id: String,
    val name: String,
    val date: Long,
    val items: String,  // JSON: ShoppingItem 列表
    val isCompleted: Boolean,
    val totalEstimated: Double?,
    val actualTotal: Double?,
    val store: String?,
    val mealPlanIds: String,  // JSON: 关联的菜单计划 ID
    val createdAt: Long,
    val completedAt: Long?
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey
    val id: String,
    val listId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,  // ShoppingCategory.name
    val estimatedPrice: Double?,
    val actualPrice: Double?,
    val isPurchased: Boolean,
    val isChecked: Boolean,
    val notes: String?,
    val sortOrder: Int,
    val recipeIds: String  // JSON: 需要此食材的菜谱 ID
)

enum class ShoppingCategory {
    VEGETABLES,
    MEAT,
    SEAFOOD,
    DAIRY,
    DRY_GOODS,
    CONDIMENTS,
    FRUITS,
    SNACKS,
    BEVERAGES,
    OTHER
}
```

---

## 核心算法

### 1. 智能合并算法

```kotlin
fun mergeItems(items: List<ShoppingItem>): List<ShoppingItem> {
    return items.groupBy { it.name }
        .map { (name, items) ->
            items.reduce { acc, item ->
                acc.copy(
                    quantity = acc.quantity + item.quantity,
                    estimatedPrice = if (acc.estimatedPrice != null && item.estimatedPrice != null) {
                        acc.estimatedPrice + item.estimatedPrice
                    } else null
                )
            }
        }
}
```

---

## 测试策略

### 单元测试

```kotlin
class ShoppingListRepositoryTest {
    @Test
    fun `merge items calls dao update`() = runTest {
        val items = listOf(
            ShoppingItem(id = "1", name = "番茄", quantity = 1.0),
            ShoppingItem(id = "2", name = "番茄", quantity = 2.0)
        )

        repository.mergeItems(items)

        verify(dao).updateItem(any())
    }
}
```

---

## 部署策略

### 数据库迁移

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS shopping_lists (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                date INTEGER NOT NULL,
                items TEXT NOT NULL,
                isCompleted INTEGER NOT NULL,
                totalEstimated REAL,
                actualTotal REAL,
                store TEXT,
                mealPlanIds TEXT,
                createdAt INTEGER NOT NULL,
                completedAt INTEGER
            )
        """.trimIndent())
    }
}
```

---

## 参考资料

- [spec.md](./spec.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
