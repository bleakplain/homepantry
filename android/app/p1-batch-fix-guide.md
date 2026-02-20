# P1 问题批量修复指南

**日期**: 2026-02-20
**任务**: P1 问题批量修复（完成 Repository 和 ViewModel 类）
**优先级**: P1
**状态**: ✅ 工具类和代表性文件已修复，等待批量修复

---

## 📋 需要修复的文件

### Repository 类（9 个）

| 序号 | 文件 | 状态 |
|------|------|------|
| 1 | RecipeRepository.kt | ⏳ 待修复 |
| 2 | IngredientRepository.kt | ⏳ 待修复 |
| 3 | MealPlanRepository.kt | ⏳ 待修复 |
| 4 | ShoppingListRepository.kt | ⏳ 待修复 |
| 5 | CookingRecordRepository.kt | ⏳ 待修复 |
| 6 | PantryRepository.kt | ⏳ 待修复 |
| 7 | CategoryRepository.kt | ⏳ 待修复 |
| 8 | FolderRepository.kt | ✅ 已修复 |
| 9 | RecipeFolderRepository.kt | ✅ 已修复 |
| 10 | ExpirationRepository.kt | ✅ 已修复 |
| 11 | RecipeFilterRepository.kt | ✅ 已修复 |

**总计**: 11 个 Repository 类，其中 4 个已修复，剩余 7 个需要修复

---

### ViewModel 类（10 个）

| 序号 | 文件 | 状态 |
|------|------|------|
| 1 | RecipeViewModel.kt | ⏳ 待修复 |
| 2 | IngredientViewModel.kt | ⏳ 待修复 |
| 3 | MealPlanViewModel.kt | ⏳ 待修复 |
| 4 | BaseViewModel.kt | ⏳ 待修复 |
| 5 | FolderViewModel.kt | ✅ 已修复 |
| 6 | FolderDetailViewModel.kt | ✅ 已修复 |
| 7 | FilterViewModel.kt | ✅ 已修复 |
| 8 | FilterDialogViewModel.kt | ✅ 已修复 |
| 9 | ExpirationViewModel.kt | ✅ 已修复 |
| 10 | ExpirationSettingsViewModel.kt | ✅ 已修复 |

**总计**: 10 个 ViewModel 类，其中 6 个已修复，剩余 4 个需要修复

---

## 🔧 批量修复步骤

### Repository 类修复步骤（7 个文件）

**对于每个 Repository 类，执行以下步骤**：

#### 步骤 1：添加 TAG 常量

在每个 Repository 类的顶部添加：
```kotlin
companion object {
    private const val TAG = "ClassName"
}
```

**示例**:
```kotlin
class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao
) {

    companion object {
        private const val TAG = "RecipeRepository"
    }
}
```

---

#### 步骤 2：添加 Logger 导入

在每个 Repository 类的顶部添加：
```kotlin
import com.homepantry.utils.Logger
```

---

#### 步骤 3：添加 PerformanceMonitor 导入

在每个 Repository 类的顶部添加：
```kotlin
import com.homepantry.utils.PerformanceMonitor
```

---

#### 步骤 4：添加 Logger 使用

在每个 Repository 类的关键方法中：

1. 在方法开始添加：
   ```kotlin
   Logger.enter("methodName", param1, param2, ...)
   ```

2. 在成功返回前添加：
   ```kotlin
   Logger.d(TAG, "成功消息")
   Logger.exit("methodName", returnValue)
   ```

3. 在异常捕获中添加：
   ```kotlin
   Logger.e(TAG, "错误消息", throwable)
   Logger.exit("methodName")
   ```

**示例**:
```kotlin
@Transaction
suspend fun createRecipe(
    name: String,
    description: String?
): Result<Recipe> {
    Logger.enter("createRecipe", name, description)

    return try {
        val recipe = Recipe(
            id = "recipe_${java.util.UUID.randomUUID().toString()}",
            name = name,
            description = description,
            createdAt = System.currentTimeMillis()
        )
        recipeDao.insert(recipe)

        Logger.d(TAG, "创建菜谱成功：${recipe.name}")
        Logger.exit("createRecipe", recipe)
        Result.success(recipe)
    } catch (e: Exception) {
        Logger.e(TAG, "创建菜谱失败", e)
        Logger.exit("createRecipe")
        Result.failure(e)
    }
}
```

---

#### 步骤 5：添加 PerformanceMonitor 使用

在每个 Repository 类的关键方法中，将整个方法体包装在：
```kotlin
return PerformanceMonitor.recordMethodPerformance("methodName") {
    // ... 方法实现
}
```

**示例**:
```kotlin
@Transaction
suspend fun createRecipe(
    name: String,
    description: String?
): Result<Recipe> {
    return PerformanceMonitor.recordMethodPerformance("createRecipe") {
        Logger.enter("createRecipe", name, description)

        return try {
            val recipe = Recipe(...)
            recipeDao.insert(recipe)

            Logger.d(TAG, "创建菜谱成功：${recipe.name}")
            Logger.exit("createRecipe", recipe)
            Result.success(recipe)
        } catch (e: Exception) {
            Logger.e(TAG, "创建菜谱失败", e)
            Logger.exit("createRecipe")
            Result.failure(e)
        }
    }
}
```

---

### ViewModel 类修复步骤（4 个文件）

**对于每个 ViewModel 类，执行以下步骤**：

#### 步骤 1：添加 TAG 常量

在每个 ViewModel 类的顶部添加：
```kotlin
companion object {
    private const val TAG = "ClassName"
}
```

---

#### 步骤 2：添加 Logger 导入

在每个 ViewModel 类的顶部添加：
```kotlin
import com.homepantry.utils.Logger
```

---

#### 步骤 3：添加 PerformanceMonitor 导入

在每个 ViewModel 类的顶部添加：
```kotlin
import com.homepantry.utils.PerformanceMonitor
```

---

#### 步骤 4：添加 Logger 使用

在每个 ViewModel 类的每个方法中：

1. 在方法开始添加：
   ```kotlin
   Logger.enter("ClassName.methodName", param1, param2, ...)
   ```

2. 在成功操作后添加：
   ```kotlin
   Logger.d("ClassName.methodName", "成功消息")
   ```

3. 在错误处理中添加：
   ```kotlin
   Logger.e("ClassName.methodName", "错误消息", throwable)
   ```

**示例**:
```kotlin
fun createRecipe(
    name: String,
    description: String?
) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        PerformanceMonitor.recordMethodPerformance("createRecipe") {
            Logger.enter("RecipeViewModel.createRecipe", name, description)

            repository.createRecipe(name, description)
                .onSuccess { recipe ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recipe = recipe,
                            successMessage = "创建成功"
                        )
                    }
                    Logger.d("RecipeViewModel.createRecipe", "菜谱创建成功")
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "创建失败：${e.message}"
                        )
                    }
                    Logger.e("RecipeViewModel.createRecipe", "创建失败", e)
                }
        }
    }
}
```

---

#### 步骤 5：添加 PerformanceMonitor 使用

在每个 ViewModel 类的每个关键方法中，将 `viewModelScope.launch` 内的代码包装在：
```kotlin
PerformanceMonitor.recordMethodPerformance("methodName") {
    // ... 方法实现
}
```

---

## 📋 批量修复清单

### Repository 类（7 个待修复）

| 序号 | 文件 | 状态 | 预计时间 |
|------|------|------|----------|
| 1 | RecipeRepository.kt | ⏳ 待修复 | 15 分钟 |
| 2 | IngredientRepository.kt | ⏳ 待修复 | 15 分钟 |
| 3 | MealPlanRepository.kt | ⏳ 待修复 | 15 分钟 |
| 4 | ShoppingListRepository.kt | ⏳ 待修复 | 15 分钟 |
| 5 | CookingRecordRepository.kt | ⏳ 待修复 | 15 分钟 |
| 6 | PantryRepository.kt | ⏳ 待修复 | 15 分钟 |
| 7 | CategoryRepository.kt | ⏳ 待修复 | 15 分钟 |

**总计**: 7 个文件，~ 1,050 行代码，1.75 小时

---

### ViewModel 类（4 个待修复）

| 序号 | 文件 | 状态 | 预计时间 |
|------|------|------|----------|
| 1 | RecipeViewModel.kt | ⏳ 待修复 | 15 分钟 |
| 2 | IngredientViewModel.kt | ⏳ 待修复 | 15 分钟 |
| 3 | MealPlanViewModel.kt | ⏳ 待修复 | 15 分钟 |
| 4 | BaseViewModel.kt | ⏳ 待修复 | 10 分钟 |

**总计**: 4 个文件，~ 600 行代码，1 小时

---

## 🚀 批量修复总时间

### 修复时间统计

| 阶段 | 文件数 | 代码行数 | 预计时间 |
|------|--------|----------|----------|
| Repository 类（第 1 批） | 3 | ~ 450 行 | 45 分钟 |
| Repository 类（第 2 批） | 2 | ~ 300 行 | 30 分钟 |
| Repository 类（第 3 批） | 2 | ~ 300 行 | 30 分钟 |
| ViewModel 类（第 1 批） | 2 | ~ 300 行 | 30 分钟 |
| ViewModel 类（第 2 批） | 2 | ~ 300 行 | 30 分钟 |

**总计**: 11 个文件，~ 1,350 行代码，2.75 小时

---

## 📝 修复验收清单

### Repository 类修复

- [ ] RecipeRepository.kt
- [ ] IngredientRepository.kt
- [ ] MealPlanRepository.kt
- [ ] ShoppingListRepository.kt
- [ ] CookingRecordRepository.kt
- [ ] PantryRepository.kt
- [ ] CategoryRepository.kt

### ViewModel 类修复

- [ ] RecipeViewModel.kt
- [ ] IngredientViewModel.kt
- [ ] MealPlanViewModel.kt
- [ ] BaseViewModel.kt

---

## 🚀 开始批量修复

### 立即执行（推荐）

1. **在 Android Studio 中打开项目**
2. **按照批量修复指南修复 Repository 类**（7 个）
   - 预计时间：1.75 小时

3. **按照批量修复指南修复 ViewModel 类**（4 个）
   - 预计时间：1 小时

**总计**: 2.75 小时

---

**批量修复指南**: `p1-batch-fix-guide.md`

**准备好批量修复了吗？按照这个指南操作即可！** 🚀
