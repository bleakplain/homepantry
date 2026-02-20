# Data Model: 高级筛选

**Spec ID**: 017
**功能名称**: 高级筛选
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20

---

## 实体定义

### RecipeFilter（菜谱筛选器）

存储当前菜谱筛选条件。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | String | 筛选器ID（主键） | NOT NULL |
| cookingTimeMin | Int? | 最短烹饪时间（分钟） | NULLABLE |
| cookingTimeMax | Int? | 最长烹饪时间（分钟） | NULLABLE |
| difficultyMin | DifficultyLevel? | 最低难度 | NULLABLE |
| difficultyMax | DifficultyLevel? | 最高难度 | NULLABLE |
| includedIngredients | List<String>? | 包含的食材ID列表 | NULLABLE |
| excludedIngredients | List<String>? | 排除的食材ID列表 | NULLABLE |
| categoryIds | List<String>? | 分类ID列表 | NULLABLE |
| createdAt | Long | 创建时间（时间戳） | NOT NULL |
| updatedAt | Long | 更新时间（时间戳） | NOT NULL |

**Room 定义**:
```kotlin
@Entity(tableName = "recipe_filters")
data class RecipeFilter(
    @PrimaryKey val id: String,
    val cookingTimeMin: Int? = null,
    val cookingTimeMax: Int? = null,
    val difficultyMin: com.homepantry.data.entity.DifficultyLevel? = null,
    val difficultyMax: com.homepantry.data.entity.DifficultyLevel? = null,
    val includedIngredients: List<String>? = null,
    val excludedIngredients: List<String>? = null,
    val categoryIds: List<String>? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

---

## 关系设计

### 与其他实体的关系

```
recipe_filters (筛选器)
    │
    ├── recipe_ingredients (菜谱食材) ── ingredients (食材)
    │
    ├── recipes (菜谱)
    │
    └── categories (分类)
```

**说明**:
- 筛选器通过 recipe_ingredients 关联 ingredients
- 筛选器筛选 recipes 表
- 筛选器通过 categories 关联分类

---

## 数据类

### RecipeFilterCriteria（筛选条件）

```kotlin
data class RecipeFilterCriteria(
    val cookingTimeMin: Int? = null,
    val cookingTimeMax: Int? = null,
    val difficultyMin: DifficultyLevel? = null,
    val difficultyMax: DifficultyLevel? = null,
    val includedIngredients: Set<String> = emptySet(),
    val excludedIngredients: Set<String> = emptySet(),
    val categoryIds: Set<String> = emptySet()
) {
    fun isEmpty(): Boolean {
        return cookingTimeMin == null &&
               cookingTimeMax == null &&
               difficultyMin == null &&
               difficultyMax == null &&
               includedIngredients.isEmpty() &&
               excludedIngredients.isEmpty() &&
               categoryIds.isEmpty()
    }
}
```

### RecipeFilterResult（筛选结果）

```kotlin
data class RecipeFilterResult(
    val recipes: List<Recipe>,
    val totalCount: Int,
    val appliedCriteria: RecipeFilterCriteria,
    val elapsedTime: Long
)
```

### CookingTimeRange（烹饪时间范围）

```kotlin
data class CookingTimeRange(
    val min: Int?,
    val max: Int?
) {
    companion object {
        val UNDER_15 = CookingTimeRange(min = null, max = 14)
        val BETWEEN_15_30 = CookingTimeRange(min = 15, max = 30)
        val BETWEEN_30_60 = CookingTimeRange(min = 30, max = 60)
        val OVER_60 = CookingTimeRange(min = 61, max = null)
    }
}
```

---

## 索引设计

### recipe_filters 表索引

```sql
-- 主键自动创建索引
CREATE INDEX idx_recipe_filters_created_at ON recipe_filters(created_at);
CREATE INDEX idx_recipe_filters_updated_at ON recipe_filters(updated_at);
```

---

## 数据流向

### 筛选流程

```
用户操作（选择筛选条件）
    ↓
RecipeFilterCriteria
    ↓
RecipeRepository.filterRecipes()
    ↓
@Query（组合 SQL 查询）
    ↓
Flow<List<Recipe>>
    ↓
UI 更新
```

---

## 数据验证

### 筛选条件验证

1. **时间范围验证**
   ```kotlin
   fun validateCookingTimeRange(min: Int?, max: Int?): Result<Unit> {
       return when {
           min != null && max != null && min > max -> Result.failure(Exception("最短时间不能大于最长时间"))
           min != null && min < 0 -> Result.failure(Exception("最短时间不能小于 0"))
           max != null && max < 0 -> Result.failure(Exception("最长时间不能小于 0"))
           else -> Result.success(Unit)
       }
   }
   ```

2. **难度范围验证**
   ```kotlin
   fun validateDifficultyRange(min: DifficultyLevel?, max: DifficultyLevel?): Result<Unit> {
       return when {
           min != null && max != null && min.ordinal > max.ordinal -> Result.failure(Exception("最低难度不能高于最高难度"))
           else -> Result.success(Unit)
       }
   }
   ```

3. **食材数量验证**
   ```kotlin
   fun validateIngredientCount(count: Int): Result<Unit> {
       return when {
           count < 0 -> Result.failure(Exception("食材数量不能小于 0"))
           count > 20 -> Result.failure(Exception("食材数量不能超过 20"))
           else -> Result.success(Unit)
       }
   }
   ```

---

## 默认数据

### 预设筛选条件

```kotlin
object PresetFilters {
    val QUICK_MEALS = RecipeFilterCriteria(
        cookingTimeMin = null,
        cookingTimeMax = 14
    )

    val SIMPLE = RecipeFilterCriteria(
        difficultyMin = DifficultyLevel.EASY,
        difficultyMax = DifficultyLevel.MEDIUM
    )

    val HEALTHY = RecipeFilterCriteria(
        includedIngredients = setOf(
            "ingredient-1",  // 番茄
            "ingredient-2",  // 菠菜
            "ingredient-3"   // 豆腐
        ),
        excludedIngredients = setOf(
            "ingredient-10",  // 肥类
            "ingredient-11"   // 油腻食材
        )
    )

    val SICHUAN_CUISINE = RecipeFilterCriteria(
        categoryIds = setOf("category-1", "category-2") // 川菜分类
    )
}
```

---

## 参考资料

- [Room Database](https://developer.android.com/training/data-storage/room)
- [Kotlin Data Classes](https://kotlinlang.org/docs/data-classes.html)
- [SQLite Indexes](https://www.sqlite.org/lang_createindex.html)

---

**文档版本**: 1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**负责人**: Jude 🦞
