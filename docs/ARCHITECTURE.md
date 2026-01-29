# HomePantry 架构设计文档

## 目录

1. [架构概览](#架构概览)
2. [分层架构](#分层架构)
3. [数据层设计](#数据层设计)
4. [业务逻辑层设计](#业务逻辑层设计)
5. [表现层设计](#表现层设计)
6. [导航设计](#导航设计)
7. [状态管理](#状态管理)
8. [错误处理](#错误处理)
9. [性能优化](#性能优化)
10. [安全性考虑](#安全性考虑)

---

## 架构概览

HomePantry 采用 **MVVM (Model-View-ViewModel)** 架构模式，结合 **Clean Architecture** 原则，实现清晰的职责分离和可测试性。

```
┌─────────────────────────────────────────────────────────────┐
│                        Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Screens    │  │  ViewModels  │  │  Navigation  │      │
│  │  (Compose)   │◄─┤   (State)    │─►│    (Graph)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         Domain Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Use Cases   │  │  Repositories│  │   Mappers    │      │
│  │  (Optional)  │  │  (Abstract)  │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                          Data Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Room DB    │  │     DAOs     │  │   Entities   │      │
│  │   (SQLite)   │◄─┤  (Queries)   │─►│  (Models)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

---

## 分层架构

### 1. 表现层 (Presentation Layer)

**职责：** UI 渲染和用户交互

**组件：**
- **Screens**: Jetpack Compose 可组合函数
- **ViewModels**: 管理UI状态和业务逻辑
- **Navigation**: 处理屏幕间导航

**原则：**
- UI 不直接访问数据源
- 通过 ViewModel 观察状态变化
- 用户事件传递给 ViewModel 处理

### 2. 业务逻辑层 (Domain Layer)

**职责：** 业务规则和用例

**组件：**
- **Repositories**: 抽象数据访问接口
- **Use Cases**: 封装具体业务操作
- **Mappers**: 数据转换

**原则：**
- 不依赖具体实现
- 可复用的业务逻辑
- 独立于框架

### 3. 数据层 (Data Layer)

**职责：** 数据持久化和获取

**组件：**
- **Database**: Room 数据库
- **DAOs**: 数据访问对象
- **Entities**: 数据库实体模型

**原则：**
- 单一数据源
- 数据转换隔离
- 错误处理统一

---

## 数据层设计

### 数据库架构

```
HomePantryDatabase (Version 2)
│
├── Recipe (菜谱)
│   ├── RecipeIngredient (菜谱食材)
│   │   └── Ingredient (食材)
│   └── RecipeInstruction (制作步骤)
│
├── MealPlan (餐食计划)
│
├── Category (分类)
│
├── PantryItem (储藏室食材)
│
└── RecipeRating (菜谱评分)
```

### 关系设计

#### 一对多关系

```
Category (1) ── (N) Recipe
Recipe (1) ── (N) RecipeIngredient
Recipe (1) ── (N) RecipeInstruction
Recipe (1) ── (N) RecipeRating
```

#### 多对多关系

```
Recipe (N) ── (M) Ingredient
           (通过 RecipeIngredient)
```

### DAO 设计原则

```kotlin
@Dao
interface RecipeDao {
    // 基础 CRUD
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    // 复杂查询
    @Query("""
        SELECT * FROM recipes
        WHERE categoryId = :categoryId
        AND difficulty = :difficulty
        AND cookingTime <= :maxTime
        ORDER BY name COLLATE NOCASE
    """)
    fun searchRecipes(
        categoryId: String?,
        difficulty: DifficultyLevel?,
        maxTime: Int?
    ): Flow<List<Recipe>>
}
```

**设计要点：**
- 返回 `Flow` 类型以支持实时数据更新
- 使用 `@Transaction` 保证复杂操作的原子性
- 查询参数支持可空类型实现灵活筛选

---

## 业务逻辑层设计

### Repository 模式

```kotlin
class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val recipeInstructionDao: RecipeInstructionDao
) {
    // 获取菜谱详情（含食材和步骤）
    suspend fun getRecipeDetail(recipeId: String): RecipeDetail? {
        return withContext(Dispatchers.IO) {
            val recipe = recipeDao.getRecipeById(recipeId) ?: return@withContext null
            val ingredients = recipeDao.getRecipeIngredients(recipeId)
            val instructions = recipeDao.getRecipeInstructions(recipeId)

            RecipeDetail(recipe, ingredients, instructions)
        }
    }

    // 创建完整菜谱
    @Transaction
    suspend fun createRecipe(recipe: Recipe, ingredients: List<RecipeIngredient>, instructions: List<RecipeInstruction>) {
        recipeDao.insertRecipe(recipe)
        ingredients.forEach { recipeDao.insertRecipeIngredient(it) }
        instructions.forEach { recipeDao.insertRecipeInstruction(it) }
    }
}
```

**职责：**
- 协调多个 DAO 的操作
- 处理数据转换
- 实现业务规则
- 提供统一的数据接口

### Use Case 设计 (可选扩展)

```kotlin
// 当业务逻辑复杂时，可引入 Use Case
class GetRecipesForMealPlanUseCase(
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository
) {
    suspend operator fun invoke(): List<Recipe> {
        val pantryItems = pantryRepository.getPantryItems()
        val availableIngredients = pantryItems.map { it.ingredientId }

        return recipeRepository.getRecipes()
            .filter { recipe ->
                recipeRepository.getRecipeIngredients(recipe.id)
                    .all { it.ingredientId in availableIngredients }
            }
    }
}
```

---

## 表现层设计

### ViewModel 设计

```kotlin
@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // 用户操作
    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            try {
                recipeRepository.getAllRecipes()
                    .collect { recipes ->
                        _uiState.value = RecipeUiState.Success(recipes)
                    }
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error(e.message)
            }
        }
    }

    fun addRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                recipeRepository.insertRecipe(recipe)
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}

// UI 状态封装
sealed class RecipeUiState {
    object Loading : RecipeUiState()
    data class Success(val recipes: List<Recipe>) : RecipeUiState()
    data class Error(val message: String?) : RecipeUiState()
}
```

### Screen 设计

```kotlin
@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel = viewModel(),
    onRecipeClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecipes()
    }

    when (uiState) {
        is RecipeUiState.Loading -> {
            CircularProgressIndicator()
        }
        is RecipeUiState.Success -> {
            RecipeList(
                recipes = (uiState as RecipeUiState.Success).recipes,
                onRecipeClick = onRecipeClick
            )
        }
        is RecipeUiState.Error -> {
            ErrorScreen(message = (uiState as RecipeUiState.Error).message)
        }
    }
}
```

---

## 导航设计

### 导航图结构

```
                    ┌──────────────┐
                    │  home_route  │
                    │  HomeScreen  │
                    └───────┬──────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼──────┐  ┌────────▼────────┐  ┌──────▼──────┐
│recipes_route │  │ingredients_route│  │mealplan_... │
│RecipeList    │  │IngredientScreen │  │MealPlanScreen│
└───────┬──────┘  └─────────────────┘  └─────────────┘
        │
        ├──────────────┬──────────────┐
        │              │              │
┌───────▼──────┐┌─────▼─────┐┌──────▼──────┐
│recipe_detail ││add_recipe ││edit_recipe  │
│RecipeDetail  ││AddRecipe  ││EditRecipe   │
└──────────────┘└───────────┘└─────────────┘
```

### 类型安全导航

```kotlin
// 导航路由定义
sealed class Screen(val route: String) {
    object Home : Screen("home_route")
    object Recipes : Screen("recipes_route")
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }
    object AddRecipe : Screen("add_recipe")
    object EditRecipe : Screen("edit_recipe/{recipeId}") {
        fun createRoute(recipeId: String) = "edit_recipe/$recipeId"
    }
}

// NavHost 设置
@Composable
fun HomePantryNavHost(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) {
            RecipeDetailScreen(
                recipeId = it.arguments?.getString("recipeId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

## 状态管理

### 状态容器模式

```kotlin
// 集中管理屏幕状态
data class RecipeDetailState(
    val isLoading: Boolean = true,
    val recipe: Recipe? = null,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val instructions: List<RecipeInstruction> = emptyList(),
    val isFavorite: Boolean = false,
    val error: String? = null
)

// 使用 StateFlow
class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RecipeDetailState())
    val state: StateFlow<RecipeDetailState> = _state.asStateFlow()

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentRecipe = _state.value.recipe ?: return@launch
            val updatedRecipe = currentRecipe.copy(isFavorite = !currentRecipe.isFavorite)
            recipeRepository.updateRecipe(updatedRecipe)
            _state.update { it.copy(isFavorite = updatedRecipe.isFavorite) }
        }
    }
}
```

---

## 错误处理

### 分层错误处理

```kotlin
// 统一错误类型
sealed class AppError : Exception() {
    data class NetworkError(override val message: String) : AppError()
    data class DatabaseError(override val message: String) : AppError()
    data class ValidationError(override val message: String) : AppError()
    data class NotFoundError(override val message: String) : AppError()
}

// Repository 错误处理
suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: SQLiteException) {
        Result.failure(AppError.DatabaseError(e.message ?: "Database error"))
    } catch (e: Exception) {
        Result.failure(AppError.NetworkError(e.message ?: "Unknown error"))
    }
}

// ViewModel 错误处理
fun loadData() {
    viewModelScope.launch {
        _uiState.value = RecipeUiState.Loading
        when (val result = recipeRepository.getRecipes()) {
            is Result.Success -> {
                _uiState.value = RecipeUiState.Success(result.data)
            }
            is Result.Failure -> {
                _uiState.value = RecipeUiState.Error(
                    getUserFriendlyMessage(result.error)
                )
            }
        }
    }
}
```

---

## 性能优化

### 数据库优化

```kotlin
@Dao
interface RecipeDao {
    // 使用索引加速查询
    @Query("SELECT * FROM recipes WHERE categoryId = :categoryId")
    fun getRecipesByCategory(categoryId: String): Flow<List<Recipe>>

    // 关联查询减少数据库访问
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeWithDetails(recipeId: String): Flow<RecipeWithDetails>

    // 分页加载大数据集
    @Query("SELECT * FROM recipes LIMIT :limit OFFSET :offset")
    fun getRecipesPaged(limit: Int, offset: Int): Flow<List<Recipe>>
}
```

### UI 优化

```kotlin
// 使用 LazyColumn 虚拟化长列表
@Composable
fun RecipeList(recipes: List<Recipe>) {
    LazyColumn {
        items(recipes, key = { it.id }) { recipe ->
            RecipeListItem(recipe)
        }
    }
}

// 避免不必要的重组
@Composable
fun RecipeListItem(
    recipe: Recipe,
    modifier: Modifier = Modifier
) {
    // 使用 remember 缓存计算结果
    val difficultyColor = remember(recipe.difficulty) {
        getDifficultyColor(recipe.difficulty)
    }

    Card(modifier = modifier) {
        // ...
    }
}
```

---

## 安全性考虑

### 数据验证

```kotlin
// 输入验证
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
        if (cookingTime <= 0) {
            errors.add("烹饪时间必须为正数")
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

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
}
```

### SQL 注入防护

```kotlin
// Room 使用参数化查询，自动防止 SQL 注入
@Query("SELECT * FROM recipes WHERE name LIKE :searchQuery")
fun searchRecipes(searchQuery: String): Flow<List<Recipe>>

// 使用绑定参数
@Query("SELECT * FROM recipes WHERE id = :recipeId")
fun getRecipeById(recipeId: String): Flow<Recipe?>
```

---

## 扩展性考虑

### 依赖注入 (Hilt)

```kotlin
// 当项目复杂度增加时，可引入 Hilt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HomePantryDatabase {
        return Room.databaseBuilder(
            context,
            HomePantryDatabase::class.java,
            "homepantry.db"
        ).build()
    }

    @Provides
    fun provideRecipeDao(database: HomePantryDatabase): RecipeDao {
        return database.recipeDao()
    }
}
```

### 测试友好设计

```kotlin
// 使用接口抽象，方便测试
interface RecipeRepository {
    suspend fun getAllRecipes(): Flow<List<Recipe>>
    suspend fun getRecipeById(id: String): Recipe?
}

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao
) : RecipeRepository {
    // 实际实现
}

// 测试时使用 Fake 实现
class FakeRecipeRepository : RecipeRepository {
    // 测试实现
}
```

---

*文档版本: 1.0*
*最后更新: 2025-01-30*
