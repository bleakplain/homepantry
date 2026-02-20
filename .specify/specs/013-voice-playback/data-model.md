# Data Model: 菜谱管理基础功能

**Spec ID**: 001
**功能名称**: 菜谱管理基础功能
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15

---

## 实体定义

### 1. Recipe（菜谱）

**表名**: `recipes`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | String | PRIMARY KEY | 唯一标识符（UUID） |
| name | String | NOT NULL | 菜谱名称（2-50 字符） |
| description | String? | NULL | 菜谱描述 |
| imageUrl | String? | NULL | 菜谱图片 URL |
| cookingTime | Int | NOT NULL | 烹饪时间（分钟，1-999） |
| servings | Int | NOT NULL | 份数（1-100） |
| difficulty | String | NOT NULL | 难度等级（DifficultyLevel.name） |
| categoryId | String? | NULL | 分类 ID（外键） |
| tags | String? | NULL | 标签（JSON 数组） |
| isFavorite | Boolean | NOT NULL | 是否收藏 |
| favoritePosition | Int? | NULL | 收藏位置（排序用） |
| createdAt | Long | NOT NULL | 创建时间（时间戳） |
| updatedAt | Long | NOT NULL | 更新时间（时间戳） |

**索引**:
- `categoryId`: 加速分类查询
- `name`: 加速搜索
- `createdAt`: 加速排序
- `isFavorite, favoritePosition`: 加速收藏查询

**Kotlin 定义**:
```kotlin
@Entity(
    tableName = "recipes",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["name"]),
        Index(value = ["createdAt"]),
        Index(value = ["isFavorite", "favoritePosition"])
    ]
)
data class Recipe(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val cookingTime: Int,
    val servings: Int,
    val difficulty: String,
    val categoryId: String?,
    val tags: String?,
    val isFavorite: Boolean,
    val favoritePosition: Int?,
    val createdAt: Long,
    val updatedAt: Long
)
```

---

### 2. Ingredient（食材）

**表名**: `ingredients`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | String | PRIMARY KEY | 唯一标识符（UUID） |
| name | String | NOT NULL | 食材名称 |
| unit | String | NOT NULL | 单位（克、毫升、个等） |
| category | String | NOT NULL | 食材分类（IngredientCategory.name） |

**Kotlin 定义**:
```kotlin
@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey
    val id: String,
    val name: String,
    val unit: String,
    val category: String
)
```

---

### 3. RecipeIngredient（菜谱食材关联）

**表名**: `recipe_ingredients`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | String | PRIMARY KEY | 唯一标识符（UUID） |
| recipeId | String | NOT NULL, FOREIGN KEY | 菜谱 ID（外键） |
| ingredientId | String? | NULL, FOREIGN KEY | 食材 ID（外键，可为空） |
| name | String | NOT NULL | 食材名称（直接存储，便于查询） |
| quantity | Double | NOT NULL | 用量 |
| unit | String | NOT NULL | 单位 |
| notes | String? | NULL | 备注（如"去蒂切块"） |
| sortOrder | Int | NOT NULL | 排序（显示顺序） |

**索引**:
- `recipeId`: 加速按菜谱查询食材

**Kotlin 定义**:
```kotlin
@Entity(
    tableName = "recipe_ingredients",
    indices = [
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
data class RecipeIngredient(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val ingredientId: String?,
    val name: String,
    val quantity: Double,
    val unit: String,
    val notes: String?,
    val sortOrder: Int
)
```

---

### 4. RecipeInstruction（菜谱步骤）

**表名**: `recipe_instructions`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | String | PRIMARY KEY | 唯一标识符（UUID） |
| recipeId | String | NOT NULL, FOREIGN KEY | 菜谱 ID（外键） |
| stepNumber | Int | NOT NULL | 步骤序号（1, 2, 3...） |
| instruction | String | NOT NULL | 步骤描述 |
| image | String? | NULL | 步骤配图 URL |
| duration | Int? | NULL | 此步耗时（秒） |
| temperature | Int? | NULL | 温度（如烤箱温度） |
| isKeyStep | Boolean | NOT NULL | 是否关键步骤 |
| reminder | String? | NULL | 提醒内容 |

**索引**:
- `recipeId`: 加速按菜谱查询步骤

**Kotlin 定义**:
```kotlin
@Entity(
    tableName = "recipe_instructions",
    indices = [
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
data class RecipeInstruction(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val stepNumber: Int,
    val instruction: String,
    val image: String?,
    val duration: Int?,
    val temperature: Int?,
    val isKeyStep: Boolean,
    val reminder: String?
)
```

---

## 关系设计

### 1. 一对多关系

#### Recipe (1) -- (N) RecipeIngredient
```sql
recipes.id = recipe_ingredients.recipeId
```

#### Recipe (1) -- (N) RecipeInstruction
```sql
recipes.id = recipe_instructions.recipeId
```

#### Category (1) -- (N) Recipe
```sql
categories.id = recipes.categoryId
```

---

### 2. 多对多关系

#### Recipe (N) -- (M) Ingredient
通过 `recipe_ingredients` 表实现：
```sql
recipes.id = recipe_ingredients.recipeId
ingredients.id = recipe_ingredients.ingredientId
```

---

## 枚举定义

### DifficultyLevel（难度等级）

```kotlin
enum class DifficultyLevel {
    EASY,       // 简单
    MEDIUM,     // 中等
    HARD        // 困难
}
```

### MealType（餐次）

```kotlin
enum class MealType {
    BREAKFAST,  // 早餐
    LUNCH,      // 午餐
    DINNER,     // 晚餐
    SNACK       // 加餐
}
```

### IngredientCategory（食材分类）

```kotlin
enum class IngredientCategory {
    VEGETABLES, // 蔬菜
    MEAT,       // 肉类
    SEAFOOD,    // 海鲜
    FRUITS,     // 水果
    GRAINS,     // 谷物
    DAIRY,      // 乳制品
    SPICES,     // 调料
    OTHER       // 其他
}
```

---

## 数据模型验证

### 验证规则

#### Recipe 验证

```kotlin
data class RecipeInput(
    val name: String,
    val cookingTime: Int,
    val servings: Int
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (name.length < 2 || name.length > 50) {
            errors.add("菜谱名称长度必须在2-50字符之间")
        }
        if (cookingTime < 1 || cookingTime > 999) {
            errors.add("烹饪时间必须在1-999分钟之间")
        }
        if (servings < 1 || servings > 100) {
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
         ├─────────────────────┬─────────────────────┐
         │                     │                     │
         ▼                     ▼                     ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│recipe_ingredients│  │recipe_instructions│  │  categories     │
│(菜谱食材)       │  │(菜谱步骤)       │  │(分类)          │
└────────┬────────┘  └─────────────────┘  └─────────────────┘
         │
         ▼
┌─────────────────┐
│  ingredients    │
│(食材)          │
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
