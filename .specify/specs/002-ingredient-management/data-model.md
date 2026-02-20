# Data Model: 食材管理

**Spec ID**: 002
**功能名称**: 食材管理
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15

---

## 实体定义

### 1. Ingredient（食材）

**表名**: `ingredients`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | String | PRIMARY KEY | 唯一标识符（UUID） |
| name | String | NOT NULL | 食材名称 |
| unit | String | NOT NULL | 单位（g, ml, piece, etc.） |
| category | String | NOT NULL | 分类（IngredientCategory.name） |
| shelfLifeDays | Int? | NULL | 保质期（天） |
| iconUrl | String? | NULL | 图标 URL |

**索引**:
- `name`: 加速搜索
- `category`: 加速分类查询

**Kotlin 定义**:
```kotlin
@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val unit: String,
    val category: IngredientCategory,
    val shelfLifeDays: Int? = null,
    val iconUrl: String? = null
)
```

---

### 2. PantryItem（库存食材）

**表名**: `pantry_items`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | String | PRIMARY KEY | 唯一标识符（UUID） |
| ingredientId | String | NOT NULL, FOREIGN KEY | 食材 ID（外键） |
| name | String | NOT NULL | 食材名称（直接存储） |
| quantity | Double | NOT NULL | 数量 |
| unit | String | NOT NULL | 单位 |
| purchaseDate | Long? | NULL | 购买日期 |
| expiryDate | Long? | NULL | 保质期（Unix timestamp） |
| storageLocation | String | NOT NULL | 存放位置（StorageLocation.name） |
| notes | String? | NULL | 备注 |

**索引**:
- `ingredientId`: 加速按食材查询
- `expiryDate`: 加速保质期排序
- `storageLocation`: 加速按位置查询

**外键**:
- `ingredientId` → `ingredients.id` (ON DELETE CASCADE)

**Kotlin 定义**:
```kotlin
@Entity(
    tableName = "pantry_items",
    indices = [
        Index(value = ["ingredientId"]),
        Index(value = ["expiryDate"]),
        Index(value = ["storageLocation"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PantryItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ingredientId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val purchaseDate: Long? = null,
    val expiryDate: Long? = null,
    val storageLocation: String = StorageLocation.PANTRY.name,
    val notes: String? = null
)
```

---

## 关系设计

### 1. 一对多关系

#### Ingredient (1) -- (N) PantryItem
```sql
ingredients.id = pantry_items.ingredientId
```

---

## 枚举定义

### StorageLocation（存储位置）

```kotlin
enum class StorageLocation {
    FRIDGE,         // 冷藏
    FREEZER,        // 冷冻
    PANTRY,         // 常温（储藏室）
    OTHER           // 其他
}
```

### IngredientCategory（食材分类）

```kotlin
enum class IngredientCategory {
    VEGETABLE,  // 蔬菜
    FRUIT,       // 水果
    MEAT,        // 肉类
    SEAFOOD,     // 海鲜
    DAIRY,       // 乳制品
    GRAIN,       // 谷物
    SPICE,       // 调料
    SAUCE,       // 酱料
    OTHER        // 其他
}
```

---

## 数据模型验证

### 验证规则

#### Ingredient 验证

```kotlin
data class IngredientInput(
    val name: String,
    val unit: String,
    val category: IngredientCategory
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (name.isEmpty() || name.length > 50) {
            errors.add("食材名称长度必须在1-50字符之间")
        }
        if (unit.isEmpty()) {
            errors.add("单位不能为空")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
```

#### PantryItem 验证

```kotlin
data class PantryItemInput(
    val ingredientId: String,
    val quantity: Double,
    val unit: String
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (quantity <= 0) {
            errors.add("数量必须大于0")
        }
        if (unit.isEmpty()) {
            errors.add("单位不能为空")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
```

---

## 数据模型图

```
┌─────────────────┐
│   ingredients    │
│   (食材)          │
└────────┬─────────┘
         │
         └──────────────────
                 │
                 ▼
        ┌─────────────────┐
        │   pantry_items  │
        │   (库存)         │
        └─────────────────┘
```

---

## 参考资料

- [spec.md](./spec.md)
- [plan.md](./plan.md)
- [tasks.md](./tasks.md)
- [ARCHITECTURE.md](../../../docs/ARCHITECTURE.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
