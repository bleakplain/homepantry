# Data Model: 收藏分类管理

**Spec ID**: 016
**功能名称**: 收藏分类管理
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20

---

## 实体定义

### Folder（收藏夹）

存储收藏夹信息。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | String | 收藏夹ID（主键） | NOT NULL |
| name | String | 收藏夹名称 | NOT NULL, 2-20 字符 |
| icon | String? | 图标名称 | NULLABLE |
| color | String? | 颜色代码（如 #FF6B35） | NULLABLE |
| sortOrder | Int | 排序顺序 | NOT NULL, DEFAULT 0 |
| isSystem | Boolean | 是否系统默认 | NOT NULL, DEFAULT false |
| createdAt | Long | 创建时间（时间戳） | NOT NULL |
| updatedAt | Long | 更新时间（时间戳） | NOT NULL |

**Room 定义**:
```kotlin
@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

---

### RecipeFolder（菜谱收藏夹关联）

存储菜谱与收藏夹的关联关系。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | String | 关联ID（主键） | NOT NULL |
| recipeId | String | 菜谱ID | NOT NULL, 外键 |
| folderId | String | 收藏夹ID | NOT NULL, 外键 |
| addedAt | Long | 添加时间（时间戳） | NOT NULL |

**Room 定义**:
```kotlin
@Entity(
    tableName = "recipe_folders",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["folderId"]),
        Index(value = ["recipeId", "folderId"], unique = true)
    ]
)
data class RecipeFolder(
    @PrimaryKey val id: String,
    val recipeId: String,
    val folderId: String,
    val addedAt: Long = System.currentTimeMillis()
)
```

---

## 关系设计

### 与其他实体的关系

```
folders (收藏夹)
    │
    ├── recipe_folders (关联表)
    │       │
    │       └── recipes (菜谱) - 一对多
```

**说明**:
- 每个收藏夹可以包含多个菜谱
- 每个菜谱可以属于多个收藏夹
- 多对多关系，通过关联表实现

---

## 数据类

### FolderWithCount（收藏夹及菜谱数量）

```kotlin
data class FolderWithCount(
    @Embedded val folder: Folder,
    val recipeCount: Int
)
```

### RecipeWithFolders（菜谱及所属收藏夹）

```kotlin
data class RecipeWithFolders(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val folders: List<Folder>
)
```

### FolderWithRecipes（收藏夹及菜谱列表）

```kotlin
data class FolderWithRecipes(
    @Embedded val folder: Folder,
    @Relation(
        parentColumn = "id",
        entityColumn = "folderId"
    )
    val recipes: List<Recipe>
)
```

---

## 索引设计

### folders 表索引

```sql
-- 主键自动创建索引
CREATE INDEX idx_folders_sort_order ON folders(sort_order);
CREATE INDEX idx_folders_name ON folders(name);
```

### recipe_folders 表索引

```sql
-- 外键和唯一约束自动创建索引
CREATE INDEX idx_recipe_folders_recipe_id ON recipe_folders(recipe_id);
CREATE INDEX idx_recipe_folders_folder_id ON recipe_folders(folder_id);
CREATE UNIQUE INDEX idx_recipe_folders_unique ON recipe_folders(recipe_id, folder_id);
```

---

## 数据流向

### 收藏菜谱流程

```
用户操作（收藏到收藏夹）
    ↓
FolderRepository.addToFolder()
    ↓
RecipeFolderDao.insert()
    ↓
recipe_folders 表
    ↓
Flow/LiveData 更新
    ↓
UI 更新
```

### 删除收藏夹流程

```
用户操作（删除收藏夹）
    ↓
FolderRepository.deleteFolder()
    ↓
@Transaction 事务
    ├─ RecipeFolderDao.deleteByFolderId()
    └─ FolderDao.deleteById()
    ↓
两个表都更新
    ↓
Flow/LiveData 更新
    ↓
UI 更新
```

---

## 数据验证

### Folder 验证

1. **名称验证**
   ```kotlin
   fun validateFolderName(name: String): Result<Unit> {
       return when {
           name.length < 2 -> Result.failure(Exception("名称不能少于2个字符"))
           name.length > 20 -> Result.failure(Exception("名称不能超过20个字符"))
           else -> Result.success(Unit)
       }
   }
   ```

2. **颜色验证**
   ```kotlin
   fun validateFolderColor(color: String?): Result<Unit> {
       return if (color == null || color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
           Result.success(Unit)
       } else {
           Result.failure(Exception("颜色格式不正确"))
       }
   }
   ```

### RecipeFolder 验证

1. **重复检查**
   ```kotlin
   suspend fun isRecipeInFolder(recipeId: String, folderId: String): Boolean {
       return recipeFolderDao.exists(recipeId, folderId) > 0
   }
   ```

---

## 默认数据

### 系统默认收藏夹

```kotlin
object DefaultFolders {
    val DEFAULT_FOLDER = Folder(
        id = "default",
        name = "我的收藏",
        icon = "star",
        color = "#FFD700",
        sortOrder = 0,
        isSystem = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
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
