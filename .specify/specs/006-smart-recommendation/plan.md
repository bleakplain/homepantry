# Plan: 智能推荐

**Spec ID**: 006
**功能名称**: 智能推荐
**优先级**: P1
**状态**: 🟡 待实现
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

---

## 核心算法

### 1. 基于库存的推荐

```kotlin
suspend fun getRecommendationsBasedOnStock(): List<RecipeRecommendation> {
    val pantryItems = pantryDao.getAllPantryItems()
    val availableIngredientIds = pantryItems.map { it.ingredientId }.toSet()

    val recipes = recipeDao.getAllRecipes()
    val recommendations = recipes.map { recipe ->
        val ingredients = recipeDao.getRecipeIngredients(recipe.id)
        val availableIngredients = ingredients.filter { availableIngredientIds.contains(it.ingredientId) }

        val matchPercentage = if (ingredients.isNotEmpty()) {
            (availableIngredients.size.toFloat() / ingredients.size) * 100
        } else 0f

        RecipeRecommendation(
            recipe = recipe,
            matchPercentage = matchPercentage,
            canMake = availableIngredients.size == ingredients.size
        )
    }

    return recommendations.sortedByDescending { it.matchPercentage }
}
```

---

## 测试策略

### 单元测试

```kotlin
class RecipeRecommenderTest {
    @Test
    fun `get recommendations based on stock returns sorted list`() = runTest {
        val recommendations = recommender.getRecommendationsBasedOnStock()

        // 验证排序
        recommendations.forEachIndexed { index, item ->
            if (index < recommendations.size - 1) {
                assertTrue(
                    item.matchPercentage >= recommendations[index + 1].matchPercentage
                )
            }
        }
    }
}
```

---

## 参考资料

- [spec.md](./spec.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**负责人**: Jude 🦞
