# Data Model: 餐食计划

**Spec ID**: 003
**功能名称**: 餐食计划
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15

---

## 实体定义

### 1. MealPlan（餐食计划）

**表名**: `meal_plans`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | String | PRIMARY KEY | 唯一标识符（UUID） |
| date | Long | NOT NULL | 日期（Unix timestamp） |
| mealType | String | NOT NULL | 餐次（MealType.name） |
| recipeId | String | NOT NULL, FOREIGN KEY | 菜谱 ID（外键） |
| servings | Int | NOT NULL | 份数 |
| notes | String? | NULL | 备注 |

**索引**:
- `date`: 加速按日期查询
- `mealType`: 加速按餐次查询
- `recipeId`: 加速按菜谱查询

**外键**:
- `recipeId` → `recipes.id` (ON DELETE CASCADE)

**Kotlin 定义**:
```kotlin
@Entity(
    tableName = "meal_plans",
    indices = [
        Index(value = ["date"]),
        Index(value = ["mealType"]),
        Index(value = ["recipeId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MealPlan(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long,
    val mealType: String,
    val recipeId: String,
    val servings: Int,
    val notes: String?
)
```

---

## 关系设计

### 一对多关系

#### Recipe (1) -- (N) MealPlan
```sql
recipes.id = meal_plans.recipeId
```

---

## 枚举定义

### MealType（餐次）

```kotlin
enum class MealType {
    BREAKFAST,  // 早餐
    LUNCH,      // 午餐
    DINNER,     // 晚餐
    SNACK       // 加餐
}
```

---

## 数据模型验证

### 验证规则

#### MealPlan 验证

```kotlin
data class MealPlanInput(
    val date: Long,
    val mealType: MealType,
    val recipeId: String,
    val servings: Int
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (date < 0) {
            errors.add("日期不能为负数")
        }
        if (servings <= 0 || servings > 100) {
            errors.add("份数必须在1-100之间")
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
│   recipes       │
│   (菜谱)        │
└────────┬────────┘
         │
         └──────────────────
                 │
                 ▼
        ┌─────────────────┐
│    meal_plans    │
│   (餐食计划）     │
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
