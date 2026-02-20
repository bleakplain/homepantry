# Plan: 食材管理

**Spec ID**: 002
**功能名称**: 食材管理
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

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
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
│  │ Repositories │  │   Mappers    │  │   Use Cases    │   │
│  │  (Abstract)  │  │              │  │  (Optional)    │   │
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

---

## 数据层设计

### Entity 定义

#### 1. Ingredient（食材）

```kotlin
@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,                  // 食材名称
    val unit: String,                  // 单位 (g, ml, piece, etc.)
    val category: IngredientCategory, // 分类
    val shelfLifeDays: Int? = null,     // 保质期（天）
    val iconUrl: String? = null          // 图标 URL
)
```

#### 2. PantryItem（库存）

```kotlin
@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ingredientId: String,          // 食材 ID
    val name: String,                  // 食材名称
    val quantity: Double,              // 数量
    val unit: String,                  // 单位
    val purchaseDate: Long? = null,     // 购买日期
    val expiryDate: Long? = null,       // 保质期 (Unix timestamp)
    val storageLocation: StorageLocation = StorageLocation.PANTRY, // 存放位置
    val notes: String? = null
)
```

#### 3. StorageLocation（存储位置）

```kotlin
enum class StorageLocation {
    FRIDGE,   // 冷藏
    FREEZER,   // 冷冻
    PANTRY,    // 常温 (储藏室)
    OTHER      // 其他
}
```

#### 4. IngredientCategory（食材分类）

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

### DAO 设计

#### IngredientDao 接口

```kotlin
@Dao
interface IngredientDao {
    // === Ingredient 操作 ===
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :ingredientId")
    suspend fun getIngredientById(ingredientId: String): Ingredient?

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchIngredients(query: String): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    // === PantryItem 操作 ===
    @Query("SELECT * FROM pantry_items ORDER BY purchasedDate DESC")
    fun getAllPantryItems(): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE ingredientId = :ingredientId")
    fun getPantryItemsByIngredient(ingredientId: String): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE storageLocation = :location ORDER BY expiryDate ASC")
    fun getPantryItemsByLocation(location: StorageLocation): Flow<List<PantryItem>>

    @Query("""
        SELECT pantry_items.* FROM pantry_items
        INNER JOIN ingredients ON pantry_items.ingredientId = ingredients.id
        ORDER BY pantry_items.expiryDate ASC
    """)
    fun getPantryItemsWithExpiry(): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE expiryDate < :expiryTime")
    suspend fun getExpiringItems(expiryTime: Long): List<PantryItem>

    @Query("SELECT * FROM pantry_items WHERE expiryDate BETWEEN :startTime AND :endTime")
    suspend fun getItemsExpiringBetween(startTime: Long, endTime: Long): List<PantryItem>

    // 即将到期的食材（N天内）
    @Query("""
        SELECT * FROM pantry_items
        WHERE expiryDate IS NOT NULL
        AND expiryDate > :now
        AND expiryDate <= :deadline
        ORDER BY expiryDate ASC
    """)
    fun getItemsExpiringSoon(now: Long, deadline: Long): Flow<List<PantryItem>>

    // 已过期的食材
    @Query("SELECT * FROM pantry_items WHERE expiryDate < :now")
    fun getExpiredItems(now: Long = System.currentTimeMillis()): Flow<List<PantryItem>>

    // 按存储位置统计
    @Query("SELECT storageLocation, COUNT(*) FROM pantry_items GROUP BY storageLocation")
    suspend fun getCountByStorageLocation(): Map<String, Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItems(items: List<PantryItem>)

    @Update
    suspend fun updatePantryItem(item: PantryItem)

    @Delete
    suspend fun deletePantryItem(item: PantryItem)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deletePantryItemById(id: String)

    @Query("DELETE FROM pantry_items WHERE expiryDate < :expiryTime")
    suspend fun deleteExpiredItems(expiryTime: Long)

    @Query("UPDATE pantry_items SET quantity = :quantity WHERE id = :id")
    suspend fun updatePantryItemQuantity(id: String, quantity: Double)

    @Query("SELECT COUNT(*) FROM pantry_items")
    suspend fun getPantryItemCount(): Int
}
```

**设计要点**:
- 返回 `Flow` 类型以支持实时数据更新
- 使用 `@Transaction` 保证复杂操作的原子性
- 查询参数支持可空类型实现灵活筛选

---

### Repository 设计

```kotlin
class IngredientRepository(
    private val ingredientDao: IngredientDao,
    private val recipeDao: RecipeDao
) {
    // === Ingredient 操作 ===
    fun getAllIngredients(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()

    suspend fun getIngredientById(ingredientId: String): Ingredient? =
        ingredientDao.getIngredientById(ingredientId)

    fun searchIngredients(query: String): Flow<List<Ingredient>> =
        ingredientDao.searchIngredients(query)

    suspend fun insertIngredient(ingredient: Ingredient) = ingredientDao.insertIngredient(ingredient)

    suspend fun updateIngredient(ingredient: Ingredient) = ingredientDao.updateIngredient(ingredient)

    suspend fun deleteIngredient(ingredient: Ingredient) = ingredientDao.deleteIngredient(ingredient)

    // === PantryItem 操作 ===
    fun getAllPantryItems(): Flow<List<PantryItem>> = ingredientDao.getAllPantryItems()

    fun getPantryItemsWithExpiry(): Flow<List<PantryItem>> =
        ingredientDao.getPantryItemsWithExpiry()

    suspend fun getExpiringItems(expiryTime: Long): List<PantryItem> =
        ingredientDao.getExpiringItems(expiryTime)

    suspend fun addPantryItem(item: PantryItem) = ingredientDao.insertPantryItem(item)

    suspend fun updatePantryItem(item: PantryItem) = ingredientDao.updatePantryItem(item)

    suspend fun deletePantryItem(itemId: String) = ingredientDao.deletePantryItemById(itemId)

    suspend fun removePantryItem(item: PantryItem) = ingredientDao.deletePantryItem(item)

    suspend fun cleanExpiredItems() {
        val now = System.currentTimeMillis()
        ingredientDao.deleteExpiredItems(now)
    }

    suspend fun getRecipeRecommendations(): List<String> {
        // Get all pantry items and find recipes that can be made with them
        val pantryItems = ingredientDao.getAllPantryItems()
        // Simplified version - in production, implement more sophisticated matching
        return emptyList()
    }
}
```

**职责**:
- 协调 DAO 操作
- 处理数据转换
- 实现业务逻辑
- 提供统一的数据接口

---

## 业务逻辑层设计

### Use Cases（可选扩展）

```kotlin
class AddIngredientUseCase(
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(
        name: String,
        unit: String,
        category: IngredientCategory
    ): Result<Ingredient> {
        // 验证输入
        if (name.isEmpty() || name.length > 50) {
            return Result.failure(ValidationError("食材名称长度必须在1-50字符之间"))
        }

        // 创建食材
        val ingredient = Ingredient(
            name = name,
            unit = unit,
            category = category
        )

        return try {
            ingredientRepository.insertIngredient(ingredient)
            Result.success(ingredient)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class AddPantryItemUseCase(
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(
        ingredientId: String,
        quantity: Double,
        expiryDate: Long?
    ): Result<PantryItem> {
        // 验证输入
        if (quantity <= 0) {
            return Result.failure(ValidationError("数量必须大于0"))
        }

        // 创建库存
        val pantryItem = PantryItem(
            ingredientId = ingredientId,
            quantity = quantity,
            expiryDate = expiryDate
        )

        return try {
            ingredientRepository.addPantryItem(pantryItem)
            Result.success(pantryItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 表现层设计

### ViewModel 设计

```kotlin
@HiltViewModel
class IngredientViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow<IngredientUiState>(IngredientUiState.Loading)
    val uiState: StateFlow<IngredientUiState> = _uiState.asStateFlow()

    // 加载所有食材
    fun loadIngredients() {
        viewModelScope.launch {
            _uiState.value = IngredientUiState.Loading
            try {
                ingredientRepository.getAllIngredients()
                    .collect { ingredients ->
                        _uiState.value = IngredientUiState.Success(ingredients)
                    }
            } catch (e: Exception) {
                _uiState.value = IngredientUiState.Error(e.message)
            }
        }
    }

    // 搜索食材
    fun searchIngredients(query: String) {
        viewModelScope.launch {
            _uiState.value = IngredientUiState.Loading
            try {
                ingredientRepository.searchIngredients(query)
                    .collect { ingredients ->
                        _uiState.value = IngredientUiState.Success(ingredients)
                    }
            } catch (e: Exception) {
                _uiState.value = IngredientUiState.Error(e.message)
            }
        }
    }

    // 添加食材
    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                ingredientRepository.insertIngredient(ingredient)
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    // 添加库存
    fun addPantryItem(item: PantryItem) {
        viewModelScope.launch {
            try {
                ingredientRepository.addPantryItem(item)
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    // 清理过期食材
    fun cleanExpiredItems() {
        viewModelScope.launch {
            try {
                ingredientRepository.cleanExpiredItems()
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}

// UI 状态封装
sealed class IngredientUiState {
    object Loading : IngredientUiState()
    data class Success(val ingredients: List<Ingredient>) : IngredientUiState()
    data class Error(val message: String?) : IngredientUiState()
}
```

---

## 关键算法

### 1. 保质期计算

```kotlin
// 计算即将到期的食材（N天内）
suspend fun getExpiringSoonItems(days: Int): List<PantryItem> {
    val now = System.currentTimeMillis()
    val deadline = now + (days * 24 * 60 * 60 * 1000)
    return ingredientDao.getExpiringItems(now, deadline)
}

// 计算已过期的食材
suspend fun getExpiredItems(): List<PantryItem> {
    val now = System.currentTimeMillis()
    return ingredientDao.getExpiredItems(now)
}
```

### 2. 库存统计

```kotlin
// 按存储位置统计
suspend fun getStorageLocationStats(): Map<StorageLocation, Int> {
    return ingredientDao.getCountByStorageLocation()
        .mapKeys { StorageLocation.valueOf(it) }
}
```

---

## 性能优化

### 数据库优化

```kotlin
@Entity(
    tableName = "ingredients",
    indices = [
        Index(value = ["name"]),      // 加速搜索
        Index(value = ["category"]),   // 加速分类查询
    ]
)
data class Ingredient(...)

@Entity(
    tableName = "pantry_items",
    indices = [
        Index(value = ["ingredientId"]),  // 加速按食材查询
        Index(value = ["expiryDate"]),   // 加速保质期排序
        Index(value = ["storageLocation"]) // 加速按位置查询
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
data class PantryItem(...)
```

### UI 优化

```kotlin
// 使用 LazyColumn 虚拟化长列表
@Composable
fun IngredientList(ingredients: List<Ingredient>) {
    LazyColumn {
        items(ingredients, key = { it.id }) { ingredient ->
            IngredientListItem(ingredient)
        }
    }
}

// 避免不必要的重组
@Composable
fun IngredientListItem(ingredient: Ingredient) {
    val categoryColor = remember(ingredient.category) {
        getCategoryColor(ingredient.category)
    }
    // ...
}
```

---

## 测试策略

### 单元测试

```kotlin
class IngredientRepositoryTest {
    private lateinit var repository: IngredientRepository
    private lateinit var dao: FakeIngredientDao

    @Before
    fun setup() {
        dao = FakeIngredientDao()
        repository = IngredientRepository(dao, fakeRecipeDao)
    }

    @Test
    fun `insert ingredient calls dao insert`() = runTest {
        val ingredient = Ingredient(id = "tomato", name = "番茄")
        repository.insertIngredient(ingredient)

        verify(dao).insertIngredient(eq(ingredient))
    }

    @Test
    fun `get expiring items calls dao getExpiringItems`() = runTest {
        repository.getExpiringItems(System.currentTimeMillis())

        verify(dao).getExpiringItems(any())
    }

    @Test
    fun `clean expired items calls dao deleteExpiredItems`() = runTest {
        repository.cleanExpiredItems()

        verify(dao).deleteExpiredItems(any())
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
            CREATE TABLE IF NOT EXISTS pantry_items (
                id TEXT PRIMARY KEY NOT NULL,
                ingredientId TEXT NOT NULL,
                name TEXT NOT NULL,
                quantity REAL NOT NULL,
                unit TEXT NOT NULL,
                purchaseDate INTEGER,
                expiryDate INTEGER,
                storageLocation TEXT NOT NULL,
                notes TEXT,
                FOREIGN KEY (ingredientId) REFERENCES ingredients(id) ON DELETE CASCADE
            )
        """.trimIndent())
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

---

## 参考资料

- [spec.md](./spec.md)
- [REQUIREMENTS.md](../../../docs/REQUIREMENTS.md)
- [ARCHITECTURE.md](../../../docs/ARCHITECTURE.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
