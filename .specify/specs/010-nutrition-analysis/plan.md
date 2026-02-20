# Plan: 菜谱管理基础功能

**Spec ID**: 001
**功能名称**: 菜谱管理基础功能
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15

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

// Coil (图片加载)
implementation("io.coil-kt:coil-compose:2.5.0")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
```

---

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐   │
│  │   Screens    │  │  ViewModels  │  │  Navigation    │   │
│  │  (Compose)   │◄─┤   (State)    │─►│    (Graph)     │   │
│  └──────────────┘  └──────────────┘  └────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐   │
│  │ Use Cases   │  │ Repositories │  │   Mappers      │   │
│  │ (Optional)  │  │  (Abstract)  │  │                │   │
│  └──────────────┘  └──────────────┘  └────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                        Data Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐   │
│  │  Room DB     │  │     DAOs     │  │   Entities     │   │
│  │  (SQLite)   │◄─┤  (Queries)   │─►│   (Models)     │   │
│  └──────────────┘  └──────────────┘  └────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 数据层设计

#### Entity 定义

```kotlin
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val cookingTime: Int,
    val servings: Int,
    val difficulty: String,  // DifficultyLevel.name
    val categoryId: String?,
    val tags: String?,  // JSON array
    val isFavorite: Boolean,
    val favoritePosition: Int?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey
    val id: String,
    val name: String,
    val unit: String,
    val category: String  // IngredientCategory.name
)

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredient(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val ingredientId: String?,
    val name: String,  // 直接存储，便于查询
    val quantity: Double,
    val unit: String,
    val notes: String?,
    val sortOrder: Int
)

@Entity(tableName = "recipe_instructions")
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

#### DAO 定义

```kotlin
@Dao
interface RecipeDao {
    // 基础 CRUD
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    // Recipe ingredients
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getRecipeIngredients(recipeId: String): List<RecipeIngredient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(ingredient: RecipeIngredient)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: String)

    // Recipe instructions
    @Query("SELECT * FROM recipe_instructions WHERE recipeId = :recipeId ORDER BY stepNumber")
    suspend fun getRecipeInstructions(recipeId: String): List<RecipeInstruction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeInstruction(instruction: RecipeInstruction)

    @Query("DELETE FROM recipe_instructions WHERE recipeId = :recipeId")
    suspend fun deleteRecipeInstructions(recipeId: String)

    // Favorites
    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY favoritePosition ASC, createdAt DESC")
    fun getFavoriteRecipes(): Flow<List<Recipe>>

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean)

    // Advanced search
    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchRecipes(query: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getRecipesByCategory(categoryId: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE difficulty = :difficulty ORDER BY createdAt DESC")
    fun getRecipesByDifficulty(difficulty: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE cookingTime <= :maxTime ORDER BY cookingTime ASC")
    fun getRecipesByMaxCookingTime(maxTime: Int): Flow<List<Recipe>>
}
```

#### Repository 定义

```kotlin
class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val recipeInstructionDao: RecipeInstructionDao
) {
    // 获取所有菜谱
    fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
    }

    // 获取菜谱详情（含食材和步骤）
    suspend fun getRecipeDetail(recipeId: String): RecipeDetail? {
        val recipe = recipeDao.getRecipeById(recipeId) ?: return null
        val ingredients = recipeDao.getRecipeIngredients(recipeId)
        val instructions = recipeDao.getRecipeInstructions(recipeId)

        return RecipeDetail(recipe, ingredients, instructions)
    }

    // 创建完整菜谱
    @Transaction
    suspend fun createRecipe(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>,
        instructions: List<RecipeInstruction>
    ) {
        recipeDao.insertRecipe(recipe)
        ingredients.forEach { recipeDao.insertRecipeIngredient(it) }
        instructions.forEach { recipeDao.insertRecipeInstruction(it) }
    }

    // 更新完整菜谱
    @Transaction
    suspend fun updateRecipe(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>,
        instructions: List<RecipeInstruction>
    ) {
        recipeDao.updateRecipe(recipe)
        recipeDao.deleteRecipeIngredients(recipe.id)
        recipeDao.deleteRecipeInstructions(recipe.id)
        ingredients.forEach { recipeDao.insertRecipeIngredient(it) }
        instructions.forEach { recipeDao.insertRecipeInstruction(it) }
    }

    // 删除菜谱
    @Transaction
    suspend fun deleteRecipe(recipeId: String) {
        val recipe = recipeDao.getRecipeById(recipeId) ?: return
        recipeDao.deleteRecipe(recipe)
        recipeDao.deleteRecipeIngredients(recipeId)
        recipeDao.deleteRecipeInstructions(recipeId)
    }

    // 搜索菜谱
    fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query)
    }

    // 按分类获取菜谱
    fun getRecipesByCategory(categoryId: String): Flow<List<Recipe>> {
        return recipeDao.getRecipesByCategory(categoryId)
    }

    // 获取收藏菜谱
    fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeDao.getFavoriteRecipes()
    }

    // 更新收藏状态
    suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean) {
        recipeDao.updateFavoriteStatus(recipeId, isFavorite)
    }
}

// 数据模型
data class RecipeDetail(
    val recipe: Recipe,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<RecipeInstruction>
)
```

### 业务逻辑层设计

#### Use Cases

```kotlin
class CreateRecipeUseCase(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String?,
        cookingTime: Int,
        servings: Int,
        difficulty: DifficultyLevel,
        categoryId: String?,
        tags: List<String>,
        imageUrl: String?,
        ingredients: List<RecipeIngredient>,
        instructions: List<RecipeInstruction>
    ): Result<Recipe> {
        // 验证输入
        if (name.length < 2 || name.length > 50) {
            return Result.failure(ValidationError("菜名长度必须在2-50字符之间"))
        }

        // 创建菜谱
        val recipe = Recipe(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            imageUrl = imageUrl,
            cookingTime = cookingTime,
            servings = servings,
            difficulty = difficulty.name,
            categoryId = categoryId,
            tags = if (tags.isNotEmpty()) Json.encodeToString(tags) else null,
            isFavorite = false,
            favoritePosition = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return try {
            recipeRepository.createRecipe(recipe, ingredients, instructions)
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetRecipeDetailUseCase(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Result<RecipeDetail> {
        return try {
            val detail = recipeRepository.getRecipeDetail(recipeId)
                ?: return Result.failure(NotFoundError("菜谱不存在"))
            Result.success(detail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class SearchRecipesUseCase(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(query: String): Flow<List<Recipe>> {
        return recipeRepository.searchRecipes(query)
    }
}
```

### 表现层设计

#### ViewModel

```kotlin
@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // 加载所有菜谱
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

    // 搜索菜谱
    fun searchRecipes(query: String) {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            try {
                recipeRepository.searchRecipes(query)
                    .collect { recipes ->
                        _uiState.value = RecipeUiState.Success(recipes)
                    }
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error(e.message)
            }
        }
    }

    // 收藏/取消收藏
    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            try {
                recipeRepository.updateFavoriteStatus(
                    recipe.id,
                    !recipe.isFavorite
                )
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    // 删除菜谱
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                recipeRepository.deleteRecipe(recipeId)
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

#### Screen

```kotlin
@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel = viewModel(),
    onRecipeClick: (String) -> Unit,
    onAddRecipe: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecipes()
    }

    Scaffold(
        topBar = {
            RecipeListTopBar(
                onSearch = { query -> viewModel.searchRecipes(query) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecipe) {
                Icon(Icons.Default.Add, contentDescription = "添加菜谱")
            }
        }
    ) { paddingValues ->
        when (uiState) {
            is RecipeUiState.Loading -> {
                LoadingIndicator()
            }
            is RecipeUiState.Success -> {
                RecipeList(
                    recipes = (uiState as RecipeUiState.Success).recipes,
                    onRecipeClick = onRecipeClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is RecipeUiState.Error -> {
                ErrorScreen(message = (uiState as RecipeUiState.Error).message)
            }
        }
    }
}

@Composable
fun RecipeList(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recipes, key = { it.id }) { recipe ->
            RecipeListItem(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.id) }
            )
        }
    }
}

@Composable
fun RecipeListItem(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 图片
            if (recipe.imageUrl != null) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "烹饪时间",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${recipe.cookingTime}分钟",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
```

---

## 数据模型

### 数据库表关系

```
recipes (菜谱)
    │
    ├── recipe_ingredients (菜谱食材) ── ingredients (食材)
    │
    └── recipe_instructions (制作步骤)
```

### 索引设计

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
data class Recipe(...)
```

---

## 关键算法

### 搜索算法

**模糊匹配**:
```sql
SELECT * FROM recipes
WHERE name LIKE '%' || :query || '%'
ORDER BY createdAt DESC
```

**优化**:
- 添加 `name` 字段索引
- 使用 FTS (Full-Text Search) 优化大规模数据

### 分页加载

```kotlin
@Query("SELECT * FROM recipes ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
fun getRecipesPaged(limit: Int, offset: Int): Flow<List<Recipe>>
```

---

## 性能优化

### 数据库优化

1. **索引优化**
   - `categoryId`: 加速分类查询
   - `name`: 加速搜索
   - `createdAt`: 加速排序
   - `isFavorite, favoritePosition`: 加速收藏查询

2. **查询优化**
   - 使用 `@Transaction` 保证原子性
   - 使用 `Flow` 实现实时更新
   - 使用分页减少单次查询数据量

3. **缓存策略**
   - 使用 Coil 缓存图片
   - 使用 Room 数据库缓存菜谱数据

### UI 优化

1. **虚拟化列表**
   ```kotlin
   LazyColumn {
       items(recipes, key = { it.id }) { recipe ->
           RecipeListItem(recipe)
       }
   }
   ```

2. **避免重组**
   ```kotlin
   val difficultyColor = remember(recipe.difficulty) {
       getDifficultyColor(recipe.difficulty)
   }
   ```

3. **图片加载优化**
   ```kotlin
   AsyncImage(
       model = ImageRequest.Builder(LocalContext.current)
           .data(recipe.imageUrl)
           .crossfade(true)
           .build(),
       contentDescription = recipe.name
   )
   ```

---

## 测试策略

### 单元测试

```kotlin
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
    fun `createRecipe with ingredients and instructions`() = runTest {
        // Given
        val recipe = Recipe(id = "1", name = "番茄炒蛋")
        val ingredients = listOf(
            RecipeIngredient(id = "1", recipeId = "1", name = "鸡蛋", quantity = 2.0, unit = "个")
        )
        val instructions = listOf(
            RecipeInstruction(id = "1", recipeId = "1", stepNumber = 1, instruction = "打散鸡蛋")
        )

        // When
        repository.createRecipe(recipe, ingredients, instructions)

        // Then
        val result = repository.getRecipeDetail("1")
        assertThat(result).isNotNull()
        assertThat(result!!.ingredients).hasSize(1)
        assertThat(result!!.instructions).hasSize(1)
    }
}
```

### UI 测试

```kotlin
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
                onRecipeClick = { clickedRecipeId = it },
                onAddRecipe = {}
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

---

## 部署策略

### 数据库迁移

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE recipes ADD COLUMN imageUrl TEXT")
    }
}

Room.databaseBuilder(
    context,
    HomePantryDatabase::class.java,
    "homepantry.db"
)
    .addMigrations(MIGRATION_1_2)
    .fallbackToDestructiveMigration()
    .build()
```

### Gradle 配置

```kotlin
android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.homepantry"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
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

## 监控和日志

### 日志策略

```kotlin
private const val TAG = "RecipeViewModel"

fun loadRecipes() {
    Timber.d("Loading recipes...")
    viewModelScope.launch {
        try {
            recipeRepository.getAllRecipes().collect { recipes ->
                Timber.d("Loaded ${recipes.size} recipes")
                _uiState.value = RecipeUiState.Success(recipes)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load recipes")
            _uiState.value = RecipeUiState.Error(e.message)
        }
    }
}
```

### 性能监控

```kotlin
// 使用 Benchmark 测试性能
@RunWith(AndroidJUnit4::class)
class RecipeListBenchmark {
    @get:Rule
    val benchmarkRule = ComposeBenchmarkRule()

    @Test
    fun benchmarkRecipeList() {
        benchmarkRule.measureRepeated {
            RecipeList(recipes = testRecipes, onRecipeClick = {})
        }
    }
}
```

---

## 参考资料

- [REQUIREMENTS.md](../../../docs/REQUIREMENTS.md)
- [ARCHITECTURE.md](../../../docs/ARCHITECTURE.md)
- [DEVELOPMENT.md](../../../docs/DEVELOPMENT.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
