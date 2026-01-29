# HomePantry 开发指南

## 目录

1. [快速开始](#快速开始)
2. [开发环境配置](#开发环境配置)
3. [编码规范](#编码规范)
4. [测试指南](#测试指南)
5. [调试技巧](#调试技巧)
6. [常见问题](#常见问题)
7. [贡献流程](#贡献流程)

---

## 快速开始

### 首次设置

```bash
# 1. 克隆项目
git clone <repository-url>
cd homepantry

# 2. 打开 Android Studio
# File → Open → 选择 android 目录

# 3. 等待 Gradle 同步完成

# 4. 运行应用
# 点击 Run 按钮或按 Shift+F10
```

### 项目结构速览

```
android/app/src/main/java/com/homepantry/
├── MainActivity.kt           # 应用入口
├── data/                     # 数据层
│   ├── dao/                 # 数据访问对象
│   ├── entity/              # 数据库实体
│   ├── repository/          # 仓库实现
│   └── database/            # 数据库配置
├── ui/                       # UI 层
│   ├── home/                # 主页面
│   ├── recipe/              # 菜谱页面
│   └── theme/               # 主题配置
├── viewmodel/                # 视图模型
└── navigation/               # 导航配置
```

---

## 开发环境配置

### 必需工具

| 工具 | 最低版本 | 推荐版本 |
|------|----------|----------|
| JDK | 17 | 17 |
| Android Studio | Hedgehog (2023.1.1) | Iguana (2024.1.1) |
| Gradle | 8.0 | 8.4 |
| Kotlin | 1.9.0 | 1.9.22 |

### Android Studio 插件推荐

1. **Kotlin** - 官方 Kotlin 支持
2. **Jetpack Compose** - Compose UI 支持
3. **Android APK Support** - APK 分析
4. **GitLens** - Git 增强工具

### Gradle 配置

```kotlin
// android/build.gradle.kts
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}

// android/app/build.gradle.kts
android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.homepantry"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
```

---

## 编码规范

### Kotlin 代码风格

#### 命名规范

```kotlin
// 类名使用 PascalCase
class RecipeRepository
data class RecipeDetail
enum class DifficultyLevel

// 函数和变量使用 camelCase
fun getRecipeById(): Recipe
val recipeName: String
var cookingTime: Int = 30

// 常量使用 UPPER_SNAKE_CASE
const val MAX_RECIPE_NAME_LENGTH = 50
const val DEFAULT_COOKING_TIME = 30

// 私有属性可以省略前缀下划线（ViewModel 除外）
private val secretKey = "abc"

// ViewModel 状态属性可以使用下划线前缀
private val _uiState = MutableStateFlow(...)
val uiState: StateFlow<...> = _uiState.asStateFlow()
```

#### 文件组织

```kotlin
// 1. 文件头注释（可选）
// 2. 包声明
package com.homepantry.data.entity

// 3. 导入语句
import androidx.room.Entity
import androidx.room.PrimaryKey

// 4. 类/接口/对象声明
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String,
    val name: String
)

// 5. 伴生对象
companion object {
    const val DEFAULT_SERVINGS = 4
}

// 6. 扩展函数（如果有）
fun Recipe.formattedName(): String {
    return name.trim()
}
```

#### 函数编写

```kotlin
// 单表达式函数
fun isValidRecipeName(name: String): Boolean = name.length in 2..50

// 多参数函数使用命名参数
fun createRecipe(
    name: String,
    cookingTime: Int = 30,
    servings: Int = 4,
    difficulty: DifficultyLevel = DifficultyLevel.EASY
): Recipe {
    // 实现
}

// 默认参数放在最后
fun searchRecipes(
    query: String,
    categoryId: String? = null,
    maxTime: Int? = null
): Flow<List<Recipe>> {
    // 实现
}
```

#### Compose 函数规范

```kotlin
// Composable 函数使用 PascalCase
@Composable
fun RecipeListItem(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 始终提供 modifier 参数
    // 参数顺序：必需参数 -> 可选参数 -> modifier
}

// 状态提升
@Composable
fun RecipeList(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 状态由上层管理
}

// 内部状态
@Composable
fun SearchBar(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    // ...
}
```

### 注释规范

```kotlin/**
 * 菜谱实体类
 *
 * @property id 唯一标识符
 * @property name 菜谱名称（2-50字符）
 * @property cookingTime 烹饪时间（分钟）
 * @property servings 份数
 * @property difficulty 难度等级
 */
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String,
    val name: String,
    val cookingTime: Int,
    val servings: Int,
    val difficulty: DifficultyLevel
)

// 函数注释
/**
 * 根据现有食材推荐菜谱
 *
 * @param pantryItems 当前储藏室中的食材
 * @return 可以制作的菜谱列表，按匹配度排序
 */
suspend fun recommendRecipes(
    pantryItems: List<PantryItem>
): List<RecipeRecommendation> {
    // 实现
}

// 行内注释 - 解释"为什么"而不是"是什么"
val cookingTime = if (recipe.isQuickMeal) {
    15  // 快速菜谱预设较短时间
} else {
    recipe.cookingTime
}

// TODO 注释
// TODO: 添加图片缓存功能以提高加载速度
// FIXME: 处理菜谱名称中的特殊字符
// OPTIMIZE: 优化大量菜谱时的查询性能
```

---

## 测试指南

### 单元测试

```kotlin
// 测试文件位置
// app/src/test/java/com/homepantry/

class RecipeRepositoryTest {

    private lateinit var repository: RecipeRepository
    private lateinit var dao: FakeRecipeDao

    @Before
    fun setup() {
        dao = FakeRecipeDao()
        repository = RecipeRepository(dao)
    }

    @Test
    fun `getRecipeById returns recipe when exists`() = runTest {
        // Given
        val recipe = Recipe(id = "1", name = "番茄炒蛋")
        dao.insertRecipe(recipe)

        // When
        val result = repository.getRecipeById("1")

        // Then
        assertThat(result).isEqualTo(recipe)
    }

    @Test
    fun `createRecipe validates input`() = runTest {
        // Given
        val invalidRecipe = Recipe(id = "1", name = "")

        // When & Then
        assertThrows<ValidationException> {
            repository.createRecipe(invalidRecipe)
        }
    }
}
```

### UI 测试

```kotlin
// 测试文件位置
// app/src/androidTest/java/com/homepantry/

class RecipeListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `click on recipe navigates to detail`() {
        // Given
        val recipe = Recipe(id = "1", name = "番茄炒蛋")
        var clickedRecipeId: String? = null

        composeTestRule.setContent {
            RecipeListScreen(
                recipes = listOf(recipe),
                onRecipeClick = { clickedRecipeId = it }
            )
        }

        // When
        composeTestRule
            .onNodeWithText("番茄炒蛋")
            .performClick()

        // Then
        assertThat(clickedRecipeId).isEqualTo("1")
    }
}
```

### 运行测试

```bash
# 所有单元测试
./gradlew test

# 所有 UI 测试
./gradlew connectedAndroidTest

# 特定测试类
./gradlew test --tests RecipeRepositoryTest

# 带覆盖率报告
./gradlew test jacocoTestReport
```

---

## 调试技巧

### Logcat 过滤

```kotlin
// 使用统一的 TAG
private const val TAG = "RecipeViewModel"

// 使用 Timber（推荐）或 Log
Timber.d("Loading recipe: $recipeId")
// 或
Log.d(TAG, "Loading recipe: $recipeId")

// 过滤 Logcat
//TAG:RecipeViewModel
```

### 断点调试

1. **行断点**：点击行号设置
2. **条件断点**：右键断点 → Edit Breakpoint → Condition
3. **日志断点**：右键断点 → Edit Breakpoint → Log evaluated expression

### 数据库调试

```kotlin
// 在 build.gradle 中添加数据库导出
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.exportSchema" to "true"
                )
            }
        }
    }
}

// 使用 Database Inspector
// View → Tool Windows → Database Inspector
```

### Compose 预览调试

```kotlin
@Preview(showBackground = true)
@Composable
fun RecipeListItemPreview() {
    HomePantryTheme {
        RecipeListItem(
            recipe = Recipe(
                id = "1",
                name = "番茄炒蛋",
                cookingTime = 15,
                servings = 2
            ),
            onClick = {}
        )
    }
}
```

---

## 常见问题

### 编译错误

#### Q: Room 编译器找不到实体类
```kotlin
// 确保 ksp 插件已配置
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    ksp("androidx.room:room-compiler:2.6.1")
}
```

#### Q: Compose 编译错误
```bash
# 清理并重新构建
./gradlew clean
./gradlew build
```

### 运行时错误

#### Q: 数据库迁移失败
```kotlin
// 添加 Migration
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE recipes ADD COLUMN imageUrl TEXT")
    }
}

// 在数据库构建器中添加
.databaseBuilder(
    context,
    HomePantryDatabase::class.java,
    "homepantry.db"
)
.addMigrations(MIGRATION_1_2)
.build()
```

#### Q: Flow 没有发射数据
```kotlin
// 确保在正确的 Dispatcher 上收集
LaunchedEffect(Unit) {
    viewModel.uiState
        .flowWithLifecycle(lifecycle)
        .collect { state ->
            // 处理状态
        }
}
```

### 性能问题

#### Q: 列表滚动卡顿
```kotlin
// 1. 使用 key 参数
LazyColumn {
    items(recipes, key = { it.id }) { recipe ->
        RecipeListItem(recipe)
    }
}

// 2. 避免在 Composable 中创建对象
@Composable
fun MyComposable() {
    // 不好的做法
    val items = listOf(1, 2, 3)

    // 好的做法
    val items = remember { listOf(1, 2, 3) }
}
```

---

## 贡献流程

### 分支策略

```
main        - 生产分支，只接受合并
  ↑
develop     - 开发分支，日常开发
  ↑
feature/*   - 功能分支
bugfix/*    - 修复分支
hotfix/*    - 紧急修复分支
```

### 提交规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型 (type):**
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例:**
```
feat(recipe): add ingredient search functionality

Implement fuzzy search for ingredients with:
- Case-insensitive matching
- Partial string matching
- Search history

Closes #123

Co-Authored-By: Your Name <email@example.com>
```

### Pull Request 流程

1. **创建功能分支**
   ```bash
   git checkout -b feature/add-recipe-rating
   ```

2. **开发并提交**
   ```bash
   git add .
   git commit -m "feat(recipe): add rating system"
   git push origin feature/add-recipe-rating
   ```

3. **创建 Pull Request**
   - 在 GitHub 上创建 PR
   - 填写 PR 模板
   - 请求代码审查

4. **代码审查**
   - 处理审查意见
   - 修改代码并推送

5. **合并**
   - Squash and merge 到 develop
   - 删除功能分支

### 代码审查清单

- [ ] 代码遵循项目编码规范
- [ ] 添加了必要的注释
- [ ] 包含单元测试
- [ ] 测试覆盖率没有降低
- [ ] 没有引入新的警告
- [ ] 更新了相关文档
- [ ] 通过了所有 CI 检查

---

## 资源链接

### 官方文档
- [Kotlin 官方文档](https://kotlinlang.org/docs/)
- [Jetpack Compose 文档](https://developer.android.com/jetpack/compose)
- [Room 数据库指南](https://developer.android.com/training/data-storage/room)
- [Android 架构指南](https://developer.android.com/topic/architecture)

### 推荐阅读
- [Effective Kotlin](https://www.amazon.com/Effective-Kotlin-Marco-Vermeulen/dp/1734145288)
- [Kotlin Coroutines Deep Dive](https://www.amazon.com/Kotlin-Coroutines-Deep-Dive-Marcin-Kozlowski/dp/B08DQJ72BD)
- [Android UI 高级编程](https://item.jd.com/12543863.html)

---

*文档版本: 1.0*
*最后更新: 2025-01-30*
