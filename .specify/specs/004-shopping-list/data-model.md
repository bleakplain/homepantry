# Data Model: 洋物清单

**Spec ID**: 004
**功能名称**: 洋物清单
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15

---

## 实体定义

### 1. ShoppingList（购物清单）

**表名**: `shopping_lists`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | String | PRIMARY KEY | 唯一标识符（UUID） |
| name | String | NOT NULL | 清单名称 |
| date | Long | NOT NULL | 日期（Unix timestamp） |
| items | String | NOT NULL | 项列表（JSON: ShoppingItem） |
| isCompleted | Boolean | NOT NULL | 是否完成 |
| totalEstimated | Double? | NULL | 预计花费 |
| actualTotal | Double? | NULL | 实际花费 |

**索引**:
- `date`: 加速按日期查询
- `isCompleted`: 加速查询完成状态

---

### 2. ShoppingItem（购物项）

**表名**: `shopping_items`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | String | PRIMARY KEY | 唯一标识符（UUID） |
| listId | String | NOT NULL, FOREIGN KEY | 清单 ID（外键） |
| name | String | NOT NULL | 食材名称 |
| quantity | Double | NOT NULL | 数量 |
| unit | String | NOT NULL | 单位 |
| category | String | NOT NULL | 分类（ShoppingCategory.name） |
| estimatedPrice | Double? | NULL | 预估价格 |
| actualPrice | Double? | NULL | 实际价格 |
| isPurchased | Boolean | NOT NULL | 是否已购买 |
| isChecked | Boolean | NOT NULL | 是否已勾选 |
| notes | String? | NULL | 备注 |
| sortOrder | Int | NOT NULL | 排序 |
| recipeIds | String | NULL | 菜谱 ID 列表（JSON） |

**索引**:
- `listId`: 加速按清单查询
- `category`: 加速按分类查询

**外键**:
- `listId` → `shopping_lists.id` (ON DELETE CASCADE)

---

## 枚举定义

### ShoppingCategory（购物分类）

```kotlin
enum class ShoppingCategory {
    VEGETABLES,  // 蔬菜
    MEAT,        // 肉类
    SEAFOOD,     // 海鲜
    DAIRY,       // 乳制品
    DRY_GOODS,   // 干货
    CONDIMENTS,  // 调料
    FRUITS,      // 水果
    SNACKS,      // 零食
    BEVERAGES,   // 饮料
    OTHER        // 其他
}
```

---

## 关系设计

### 一对多关系

#### ShoppingList (1) -- (N) ShoppingItem
```sql
shopping_lists.id = shopping_items.listId
```

---

## 数据模型验证

### 验证规则

#### ShoppingList 验证

```kotlin
data class ShoppingListInput(
    val name: String,
    val date: Long
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (name.isEmpty() || name.length > 50) {
            errors.add("清单名称长度必须在1-50字符之间")
        }
        if (date < 0) {
            errors.add("日期不能为负数")
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
│ shopping_lists  │
│   (购物清单）     │
└────────┬─────────┘
         │
         └──────────────────
                 │
                 ▼
        ┌─────────────────┐
        │ shopping_items  │
        │    (购物项）     │
        └─────────────────┘
```

---

## 参考资料

- [spec.md](./spec.md)
- [plan.md](./plan.md)
- [ARCHITECTURE.md](../../../docs/ARCHITECTURE.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
