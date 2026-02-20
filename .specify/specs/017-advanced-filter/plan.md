# Plan: 高级筛选

**Spec ID**: 017
**功能名称**: 高级筛选
**优先级**: P1
**状态**: 🚧 规划中
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20

---

## 技术栈

### 核心技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9+ | 开发语言 |
| Jetpack Compose | 1.5+ | UI 框架 |
| Room | 2.6+ | 本地数据库 |
| Navigation Compose | 2.7+ | 页面导航 |
| Coroutines | 1.7+ | 异步处理 |
| ViewModel | 2.6+ | 状态管理 |
| Flow | Kotlin | 数据流 |

### 主要依赖

```kotlin
// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.5")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
```

---

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────┐
│                    Presentation Layer               │
│  ┌──────────────────┐  ┌──────────────────────┐   │
│  │   Screens        │  │  ViewModels          │   │
│  │  (Compose)       │◄─┤   (State)            │   │
│  │  FilterDialog    │  └──────────────────────┘   │
│  └──────────────────┘                            │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                     Domain Layer                    │
│  ┌──────────────────┐  ┌──────────────────────┐   │
│  │ FilterRepository│  │  RecipeRepository      │   │
│  │  (筛选仓库)     │  └──────────────────────┘   │
│  └──────────────────┘                            │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                        Data Layer                    │
│  ┌──────────────────┐  ┌──────────────────────┐   │
│  │  FilterDao       │  │  RecipeDao            │   │
│  │  (筛选数据)      │  └──────────────────────┘   │
│  └──────────────────┘                            │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                     Room Database                    │
│              (SQLite 本地数据库)                     │
└─────────────────────────────────────────────────────┘
```

---

## 数据模型

### 核心实体

#### RecipeFilter（菜谱筛选）

存储当前菜谱筛选条件。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | String | 筛选ID（主键） | NOT NULL |
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
    val difficultyMin: DifficultyLevel? = null,
    val difficultyMax: DifficultyLevel? = null,
    val includedIngredients: List<String>? = null,
    val excludedIngredients: List<String>? = null,
    val categoryIds: List<String>? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

---

## 关键功能实现

### 1. 筛选逻辑

#### 创建筛选器

```kotlin
@Transaction
suspend fun createFilter(filter: RecipeFilter): Result<RecipeFilter> {
    return try {
        val newFilter = RecipeFilter(
            id = java.util.UUID.randomUUID().toString(),
            cookingTimeMin = filter.cookingTimeMin,
            cookingTimeMax = filter.cookingTimeMax,
            difficultyMin = filter.difficultyMin,
            difficultyMax = filter.difficultyMax,
            includedIngredients = filter.includedIngredients,
            excludedIngredients = filter.excludedIngredients,
            categoryIds = filter.categoryIds
        )
        filterDao.insert(newFilter)
        Result.success(newFilter)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

#### 应用筛选条件

```kotlin
@Query("""
    SELECT r.*
    FROM recipes r
    WHERE (:cookingTimeMin IS NULL OR r.cooking_time >= :cookingTimeMin)
    AND (:cookingTimeMax IS NULL OR r.cooking_time <= :cookingTimeMax)
    AND (:difficultyMin IS NULL OR r.difficulty >= :difficultyMin)
    AND (:difficultyMax IS NULL OR r.difficulty <= :difficultyMax)
    AND (:categoryIds IS NULL OR r.category_id IN (:categoryIds))
    AND (
        :includedIngredients IS NULL
        OR r.id IN (
            SELECT DISTINCT ri.recipe_id
            FROM recipe_ingredients ri
            WHERE ri.ingredient_id IN (:includedIngredients)
        )
    )
    AND (
        :excludedIngredients IS NULL
        OR r.id NOT IN (
            SELECT DISTINCT ri.recipe_id
            FROM recipe_ingredients ri
            WHERE ri.ingredient_id IN (:excludedIngredients)
        )
    )
    ORDER BY r.created_at DESC
""")
fun filterRecipes(
    cookingTimeMin: Int?,
    cookingTimeMax: Int?,
    difficultyMin: DifficultyLevel?,
    difficultyMax: DifficultyLevel?,
    categoryIds: List<String>?,
    includedIngredients: List<String>?,
    excludedIngredients: List<String>?
): Flow<List<Recipe>>
```

### 2. 筛选 UI

#### FilterDialog（筛选对话框）

```kotlin
@Composable
fun FilterDialog(
    currentFilter: RecipeFilter,
    onDismiss: () -> Unit,
    onApply: (RecipeFilter) -> Unit
) {
    var cookingTimeMin by remember { mutableStateOf(currentFilter.cookingTimeMin) }
    var cookingTimeMax by remember { mutableStateOf(currentFilter.cookingTimeMax) }
    var selectedDifficulties by remember { mutableStateOf(currentFilter.getDifficulties()) }
    var selectedIngredients by remember { mutableStateOf(currentFilter.includedIngredients ?: emptySet()) }
    var selectedCategories by remember { mutableStateOf(currentFilter.categoryIds ?: emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("筛选菜谱") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 烹饪时间筛选
                CookingTimeFilterSection(
                    min = cookingTimeMin,
                    max = cookingTimeMax,
                    onMinChange = { cookingTimeMin = it },
                    onMaxChange = { cookingTimeMax = it }
                )

                // 难度筛选
                DifficultyFilterSection(
                    selectedDifficulties = selectedDifficulties,
                    onToggle = { difficulty ->
                        selectedDifficulties = if (selectedDifficulties.contains(difficulty)) {
                            selectedDifficulties - difficulty
                        } else {
                            selectedDifficulties + difficulty
                        }
                    }
                )

                // 食材筛选
                IngredientFilterSection(
                    selectedIngredients = selectedIngredients,
                    onToggle = { ingredientId ->
                        selectedIngredients = if (selectedIngredients.contains(ingredientId)) {
                            selectedIngredients - ingredientId
                        } else {
                            selectedIngredients + ingredientId
                        }
                    }
                )

                // 分类筛选
                CategoryFilterSection(
                    selectedCategories = selectedCategories,
                    onToggle = { categoryId ->
                        selectedCategories = if (selectedCategories.contains(categoryId)) {
                            selectedCategories - categoryId
                        } else {
                            selectedCategories + categoryId
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val filter = currentFilter.copy(
                    cookingTimeMin = cookingTimeMin,
                    cookingTimeMax = cookingTimeMax,
                    difficultyMin = selectedDifficulties.minOrNull(),
                    difficultyMax = selectedDifficulties.maxOrNull(),
                    includedIngredients = selectedIngredients.toList(),
                    categoryIds = selectedCategories.toList()
                )
                onApply(filter)
            }) {
                Text("应用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        // 清除筛选按钮
        dismissButton = {
            TextButton(
                onClick = {
                    val emptyFilter = RecipeFilter(
                        id = currentFilter.id
                    )
                    onApply(emptyFilter)
                }
            ) {
                Text("清除筛选")
            }
        }
    )
}
```

---

## 性能优化

### 数据库优化

1. **索引优化**
   ```kotlin
   @Entity(
       tableName = "recipe_filters",
       indices = [
           Index(value = ["created_at", "updated_at"])
       ]
   )
   ```

2. **查询优化**
   - 使用参数化查询防止 SQL 注入
   - 使用 Flow 进行流式查询
   - 避免N+1查询

3. **分页加载**
   ```kotlin
   @Query("""
       SELECT r.*
       FROM recipes r
       WHERE ...
       LIMIT :limit OFFSET :offset
   """)
   fun filterRecipesPaged(
       ...
       limit: Int,
       offset: Int
   ): Flow<List<Recipe>>
   ```

### UI 优化

1. **异步加载**
   - 使用 Coroutines 进行异步筛选
   - 显示加载状态

2. **防抖动处理**
   ```kotlin
   @Composable
   fun FilterSection(
       onFilterChange: (RecipeFilter) -> Unit
   ) {
       LaunchedEffect(Unit) {
           delay(300) // 防抖 300ms
           onFilterChange(currentFilter)
       }
   }
   ```

3. **虚拟滚动**
   ```kotlin
   LazyColumn(
       modifier = Modifier.fillMaxSize(),
       verticalArrangement = Arrangement.spacedBy(8.dp)
   ) {
       items(recipes) { recipe ->
           RecipeCard(recipe)
       }
   }
   ```

---

## 测试策略

### 单元测试

**FilterDao 测试**:
- 创建筛选器测试
- 更新筛选器测试
- 删除筛选器测试
- 查询筛选器测试

**FilterRepository 测试**:
- 创建筛选器测试
- 更新筛选器测试
- 应用筛选测试
- 清除筛选测试

### 集成测试

- 筛选完整流程
- 多条件组合筛选
- 大数据量筛选性能

### 测试覆盖率目标

- 代码覆盖率：≥ 70%
- 核心算法覆盖率：≥ 90%

---

## 部署策略

### 数据库迁移

```kotlin
val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建 recipe_filters 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS recipe_filters (
                id TEXT PRIMARY KEY NOT NULL,
                cooking_time_min INTEGER,
                cooking_time_max INTEGER,
                difficulty_min TEXT,
                difficulty_max TEXT,
                included_ingredients TEXT,
                excluded_ingredients TEXT,
                category_ids TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """)

        // 创建索引
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_recipe_filters_created_at
            ON recipe_filters(created_at)
        """)
    }
}
```

---

## 已知问题和优化方向

### 已知问题

1. **大量食材筛选性能**
   - 影响：选择 20 个食材时，筛选可能变慢
   - 解决方案：使用索引、限制食材数量
   - 状态：✅ 已实现（限制最多 20 个）

2. **组合筛选的索引优化**
   - 影响：多个条件组合时，可能无法有效使用索引
   - 解决方案：创建复合索引
   - 状态：✅ 已实现

### 优化方向

1. **全文搜索**（优先级 P2）
   - 使用 FTS (Full-Text Search)
   - 提升食材筛选性能
   - 支持模糊搜索

2. **保存筛选条件为预设**（优先级 P2）
   - 允许用户保存常用筛选
   - 快速应用预设筛选

3. **语音搜索**（优先级 P3）
   - 语音输入筛选条件
   - 提升易用性

---

## 参考资料

- [Room Database](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

**文档版本**: 1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**负责人**: Jude 🦞
