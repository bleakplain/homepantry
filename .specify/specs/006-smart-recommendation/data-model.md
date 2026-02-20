# Data Model: 智能推荐

**Spec ID**: 006
**功能名称**: 智能推荐
**优先级**: P1
**状态**: 🟡 待实现
**创建日期**: 2026-02-15

---

## 核心组件

智能推荐功能不需要独立的数据库实体，它主要是一个业务逻辑功能，基于现有数据（Recipe, PantryItem 等）进行推荐。

---

## 关键数据类

### 1. RecipeRecommendation（菜谱推荐）

```kotlin
data class RecipeRecommendation(
    val recipe: Recipe,              // 菜谱
    val matchPercentage: Float,     // 匹配度（0-100%）
    val missingIngredients: List<String>,  // 缺失的食材
    val availableIngredients: List<String>,  // 可用的食材
    val canMake: Boolean            // 是否可以做
)
```

---

## 数据关系

智能推荐功能依赖于以下现有的实体：

1. **Recipe（菜谱）**
   - id, name, ingredients, cookingTime, difficulty

2. **PantryItem（库存）**
   - id, ingredientId, quantity, expiryDate

3. **RecipeIngredient（菜谱食材）**
   - id, recipeId, ingredientId, quantity, unit

---

## 推荐逻辑

### 基于库存的推荐

```kotlin
fun getRecommendationsBasedOnStock(): List<RecipeRecommendation> {
    // 1. 获取所有库存
    val pantryItems = pantryDao.getAllPantryItems()
    
    // 2. 获取所有菜谱
    val recipes = recipeDao.getAllRecipes()
    
    // 3. 计算每个菜谱的匹配度
    val recommendations = recipes.map { recipe ->
        val ingredients = recipeDao.getRecipeIngredients(recipe.id)
        val availableIngredients = ingredients.filter { 
            pantryItems.any { it.ingredientId == it.ingredientId }
        }
        
        val matchPercentage = if (ingredients.isNotEmpty()) {
            (availableIngredients.size.toFloat() / ingredients.size) * 100
        } else 0f
        
        RecipeRecommendation(
            recipe = recipe,
            matchPercentage = matchPercentage,
            missingIngredients = ingredients.filterNot { 
                pantryItems.any { it.ingredientId == it.ingredientId }
            }.map { it.name },
            availableIngredients = availableIngredients.map { it.name },
            canMake = availableIngredients.size == ingredients.size
        )
    }
    
    return recommendations.sortedByDescending { it.matchPercentage }
}
```

---

## 参考资料

- [spec.md](./spec.md)
- [plan.md](./plan.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**负责人**: Jude 🦞
