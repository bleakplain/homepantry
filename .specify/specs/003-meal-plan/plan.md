# Plan: 餐食计划

**Spec ID**: 003
**功能名称**: 餐食计划
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
│                    Presentation Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐   │
│  │   Screens    │  │  ViewModels  │  │  Navigation    │   │
│  │  (Compose)   │◄─┤   (State)    │─►│    (Graph)     │   │
│  └──────────────┘  └──────────────┘  └────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐   │
│  │ Use Cases   │  │ Repositories │  │   Mappers      │   │
│  │ (Optional)  │  │  (Abstract)  │  │                │   │
│  └──────────────┘  └──────────────┘  └────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                        Data Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐   │
│  │  Room DB     │  │     DAOs     │  │   Entities     │   │
│  │  (SQLite)   │◄─┤  (Queries)   │─►│   (Models)     │   │
│  └──────────────┘  └──────────────┘  └────────────────┘   │
└─────────────────────────────────────────────────────┘
```

---

## 数据层设计

### Entity 定义

```kotlin
@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long,           // Unix timestamp
    val mealType: MealType,    // BREAKFAST, LUNCH, DINNER, SNACK
    val recipeId: String,
    val servings: Int,
    val notes: String? = null
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}
```

### DAO 设计

```kotlin
@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans ORDER BY date ASC")
    fun getAllMealPlans(): Flow<List<MealPlan>>

    @Query("SELECT * FROM meal_plans WHERE date >= :startDate AND date < :endDate ORDER BY date ASC")
    fun getMealPlansForWeek(startDate: Long, endDate: Long): Flow<List<MealPlan>>

    @Query("SELECT * FROM meal_plans WHERE date = :date ORDER BY mealType ASC")
    fun getMealPlansForDate(date: Long): Flow<List<MealPlan>>

    @Query("SELECT * FROM meal_plans WHERE id = :mealPlanId")
    suspend fun getMealPlanById(mealPlanId: String): MealPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan)

    @Update
    suspend fun updateMealPlan(mealPlan: MealPlan)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlan)

    @Query("DELETE FROM meal_plans WHERE date = :date")
    suspend fun deleteMealPlansForDate(date: Long)
}
```

### Repository 设计

```kotlin
class MealPlanRepository(
    private val mealPlanDao: MealPlanDao,
    private val recipeDao: RecipeDao
) {
    fun getAllMealPlans(): Flow<List<MealPlan>> = mealPlanDao.getAllMealPlans()

    fun getMealPlansForWeek(startDate: Long, endDate: Long? = null): Flow<List<MealPlan>> {
        val endDateValue = endDate ?: (startDate + 7 * 24 * 60 * 60 * 1000)
        return mealPlanDao.getMealPlansForWeek(startDate, endDateValue)
    }

    fun getMealPlansForDate(date: Long): Flow<List<MealPlan>> =
        mealPlanDao.getMealPlansForDate(date)

    suspend fun addMealPlan(mealPlan: MealPlan) = mealPlanDao.insertMealPlan(mealPlan)

    suspend fun updateMealPlan(mealPlan: MealPlan) = mealPlanDao.updateMealPlan(mealPlan)

    suspend fun deleteMealPlan(mealPlan: MealPlan) = mealPlanDao.deleteMealPlan(mealPlan)

    suspend fun copyDayToAnother(fromDate: Long, toDate: Long) {
        val plans = mealPlanDao.getMealPlansForDate(fromDate)
        plans.forEach { mealPlanDao.insertMealPlan(it.copy(date = toDate)) }
    }
}
```

---

## 关键算法

### 1. 周菜单生成

```kotlin
class WeeklyMenuGenerator(
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository
) {
    suspend fun generateWeeklyMenu(
        startDate: Long,
        constraints: MenuConstraints
    ): List<WeeklyMenu> {
        // 生成 2-3 个方案
        val plans = mutableListOf<WeeklyMenu>()

        repeat(3) { index ->
            val menu = generateMenuForWeek(startDate, constraints, index)
            plans.add(menu)
        }

        return plans
    }

    private suspend fun generateMenuForWeek(
        startDate: Long,
        constraints: MenuConstraints,
        seed: Int
    ): WeeklyMenu {
        val days = 7
        val menu = mutableListOf<DailyMenu>()

        for (i in 0 until days) {
            val date = startDate + (i * 24 * 60 * 60 * 1000)
            val dailyMenu = generateDailyMenu(date, constraints, seed)
            menu.add(dailyMenu)
        }

        return WeeklyMenu(
            id = UUID.randomUUID().toString(),
            name = "方案 ${seed + 1}",
            days = menu
        )
    }
}

data class MenuConstraints(
    val maxCookingTime: Int = 30,
    val balanceVegetables: Boolean = true,
    val avoidRepeating: Boolean = true
)
```

---

## 测试策略

### 单元测试

```kotlin
class MealPlanRepositoryTest {
    @Mock
    private lateinit var mealPlanDao: MealPlanDao

    private lateinit var repository: MealPlanRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = MealPlanRepository(mealPlanDao, fakeRecipeDao)
    }

    @Test
    fun `add meal plan calls dao insert`() = runTest {
        val plan = MealPlan(date = System.currentTimeMillis(), recipeId = "1")
        repository.addMealPlan(plan)

        verify(mealPlanDao).insertMealPlan(eq(plan))
    }

    @Test
    fun `get meal plans for week calls dao query`() = runTest {
        val start = System.currentTimeMillis()
        repository.getMealPlansForWeek(start)

        verify(mealPlanDao).getMealPlansForWeek(eq(start), any())
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
            CREATE TABLE IF NOT EXISTS meal_plans (
                id TEXT PRIMARY KEY NOT NULL,
                date INTEGER NOT NULL,
                mealType TEXT NOT NULL,
                recipeId TEXT NOT NULL,
                servings INTEGER NOT NULL,
                notes TEXT
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
