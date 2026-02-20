# Data Model: 营养分析与数据统计

**Spec ID**: 015
**功能名称**: 营养分析与数据统计
**创建日期**: 2026-02-15
**最后更新**: 2026-02-20

---

## 实体定义

### NutritionInfo（营养信息）

存储菜谱的营养信息。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| recipeId | String | 菜谱ID（主键） | NOT NULL |
| servingSize | Int | 每份大小（人份） | NOT NULL, DEFAULT 1 |
| calories | Int? | 热量（卡路里） | NULLABLE |
| protein | Double? | 蛋白质（克） | NULLABLE |
| fat | Double? | 脂肪（克） | NULLABLE |
| carbs | Double? | 碳水化合物（克） | NULLABLE |
| fiber | Double? | 膳食纤维（克） | NULLABLE |
| sodium | Int? | 钠（毫克） | NULLABLE |

**Room 定义**:
```kotlin
@Entity(tableName = "nutrition_info")
data class NutritionInfo(
    @PrimaryKey val recipeId: String,
    val servingSize: Int = 1,
    val calories: Int? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null,
    val fiber: Double? = null,
    val sodium: Int? = null
)
```

---

## 关系设计

### 与其他实体的关系

```
recipes (菜谱)
    │
    └── nutrition_info (营养信息) - 一对一
```

**说明**:
- 每个菜谱最多有一个营养信息记录
- 营养信息是可选的（菜谱可以没有营养数据）

---

## 数据类

### MealNutrition（餐食营养）

存储单餐的营养信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| recipeId | String | 菜谱ID |
| calories | Int | 热量（卡路里） |
| protein | Double | 蛋白质（克） |
| fat | Double | 脂肪（克） |
| carbs | Double | 碳水化合物（克） |
| fiber | Double | 膳食纤维（克） |
| sodium | Int | 钠（毫克） |

**代码**:
```kotlin
data class MealNutrition(
    val recipeId: String,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val fiber: Double,
    val sodium: Int
)
```

---

### TotalNutrition（总营养）

存储总营养信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| calories | Int | 热量（卡路里） |
| protein | Double | 蛋白质（克） |
| fat | Double | 脂肪（克） |
| carbs | Double | 碳水化合物（克） |
| fiber | Double | 膳食纤维（克） |
| sodium | Int | 钠（毫克） |

**代码**:
```kotlin
data class TotalNutrition(
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val fiber: Double,
    val sodium: Int
)
```

---

### NutritionAssessment（营养评估）

存储营养评估结果。

| 字段 | 类型 | 说明 |
|------|------|------|
| score | Int | 评分（0-100） |
| level | NutritionLevel | 等级（优秀/良好/一般/较差） |
| issues | List<String> | 问题列表 |
| warnings | List<String> | 警告列表 |

**代码**:
```kotlin
data class NutritionAssessment(
    val score: Int,
    val level: NutritionLevel,
    val issues: List<String>,
    val warnings: List<String>
)
```

---

### DailyNutritionReport（单日营养报告）

存储单日营养分析报告。

| 字段 | 类型 | 说明 |
|------|------|------|
| date | Long | 日期（时间戳） |
| mealNutrition | Map<MealType, MealNutrition> | 各餐营养 |
| totalNutrition | TotalNutrition | 总营养 |
| assessment | NutritionAssessment | 营养评估 |

**代码**:
```kotlin
data class DailyNutritionReport(
    val date: Long,
    val mealNutrition: Map<MealType, MealNutrition>,
    val totalNutrition: TotalNutrition,
    val assessment: NutritionAssessment
)
```

---

### WeeklyNutritionReport（周营养报告）

存储周营养分析报告。

| 字段 | 类型 | 说明 |
|------|------|------|
| dailyReports | List<DailyNutritionReport> | 每日报告列表 |
| averageNutrition | AverageNutrition | 平均营养 |
| trends | NutritionTrends | 营养趋势 |
| suggestions | List<String> | 改进建议 |

**代码**:
```kotlin
data class WeeklyNutritionReport(
    val dailyReports: List<DailyNutritionReport>,
    val averageNutrition: AverageNutrition,
    val trends: NutritionTrends,
    val suggestions: List<String>
)
```

---

### AverageNutrition（平均营养）

存储平均营养信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| avgCalories | Int | 平均热量（卡路里） |
| avgProtein | Double | 平均蛋白质（克） |

**代码**:
```kotlin
data class AverageNutrition(
    val avgCalories: Int,
    val avgProtein: Double
)
```

---

### NutritionTrends（营养趋势）

存储营养趋势信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| calorieTrend | TrendType | 热量趋势 |
| proteinTrend | TrendType | 蛋白质趋势 |
| summary | String | 趋势总结 |

**代码**:
```kotlin
data class NutritionTrends(
    val calorieTrend: TrendType,
    val proteinTrend: TrendType,
    val summary: String
)
```

---

### NutritionComparison（营养对比）

存储实际与推荐营养的对比。

| 字段 | 类型 | 说明 |
|------|------|------|
| actual | TotalNutrition | 实际摄入 |
| recommended | RecommendedNutrition | 推荐摄入 |
| differences | NutritionDifferences | 差异 |

**代码**:
```kotlin
data class NutritionComparison(
    val actual: TotalNutrition,
    val recommended: RecommendedNutrition,
    val differences: NutritionDifferences
)
```

---

### RecommendedNutrition（推荐营养）

存储推荐营养摄入量。

| 字段 | 类型 | 说明 |
|------|------|------|
| calories | Int | 推荐热量（卡路里） |
| protein | Int | 推荐蛋白质（克） |
| fat | Int | 推荐脂肪（克） |
| carbs | Int | 推荐碳水（克） |
| fiber | Int | 推荐纤维（克） |
| sodium | Int | 推荐钠（毫克） |

**代码**:
```kotlin
data class RecommendedNutrition(
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int,
    val fiber: Int,
    val sodium: Int
)
```

---

### NutritionDifferences（营养差异）

存储实际与推荐的差异。

| 字段 | 类型 | 说明 |
|------|------|------|
| calories | Int | 热量差异 |
| protein | Double | 蛋白质差异（克） |
| fat | Double | 脂肪差异（克） |
| carbs | Double | 碳水差异（克） |
| fiber | Double | 纤维差异（克） |
| sodium | Int | 钠差异（毫克） |

**代码**:
```kotlin
data class NutritionDifferences(
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val fiber: Double,
    val sodium: Int
)
```

---

### NutritionAdvice（营养建议）

存储营养建议。

| 字段 | 类型 | 说明 |
|------|------|------|
| type | AdviceType | 建议类型（信息/建议/警告） |
| category | String | 建议类别 |
| message | String | 建议消息 |
| suggestion | String | 具体建议 |

**代码**:
```kotlin
data class NutritionAdvice(
    val type: AdviceType,
    val category: String,
    val message: String,
    val suggestion: String
)
```

---

### ExportRecipe（导出菜谱）

存储导出的菜谱数据。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 菜谱ID |
| name | String | 菜名 |
| description | String? | 描述 |
| imageUrl | String? | 图片URL |
| cookingTime | Int | 烹饪时间（分钟） |
| servings | Int | 份量 |
| difficulty | String | 难度 |
| categoryName | String? | 分类名称 |
| tags | String | 标签 |
| ingredients | List<ExportIngredient> | 食材列表 |
| instructions | List<String> | 步骤列表 |

**代码**:
```kotlin
data class ExportRecipe(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val cookingTime: Int,
    val servings: Int,
    val difficulty: String,
    val categoryName: String?,
    val tags: String,
    val ingredients: List<ExportIngredient>,
    val instructions: List<String>
)
```

---

### ExportIngredient（导出食材）

存储导出的食材数据。

| 字段 | 类型 | 说明 |
|------|------|------|
| ingredientName | String | 食材名称 |
| quantity | Double | 用量 |
| unit | String | 单位 |

**代码**:
```kotlin
data class ExportIngredient(
    val ingredientName: String,
    val quantity: Double,
    val unit: String
)
```

---

### RecipeExportData（菜谱导出数据）

存储导出的菜谱集合。

| 字段 | 类型 | 说明 |
|------|------|------|
| version | String | 版本号 |
| exportDate | Long | 导出日期（时间戳） |
| recipes | List<ExportRecipe> | 菜谱列表 |

**代码**:
```kotlin
data class RecipeExportData(
    val version: String = "1.0",
    val exportDate: Long = System.currentTimeMillis(),
    val recipes: List<ExportRecipe>
)
```

---

## 枚举定义

### NutritionLevel（营养等级）

```kotlin
enum class NutritionLevel {
    EXCELLENT,  // 优秀（80-100分）
    GOOD,       // 良好（60-79分）
    FAIR,       // 一般（40-59分）
    POOR        // 较差（0-39分）
}
```

### TrendType（趋势类型）

```kotlin
enum class TrendType {
    INCREASING,  // 上升
    DECREASING,  // 下降
    STABLE       // 稳定
}
```

### AdviceType（建议类型）

```kotlin
enum class AdviceType {
    INFO,        // 信息
    SUGGESTION,  // 建议
    WARNING      // 警告
}
```

### Gender（性别）

```kotlin
enum class Gender {
    MALE,    // 男性
    FEMALE   // 女性
}
```

### ActivityLevel（活动水平）

```kotlin
enum class ActivityLevel {
    SEDENTARY,      // 久坐
    LIGHT,          // 轻度活动
    MODERATE,       // 中度活动
    ACTIVE,         // 活跃
    VERY_ACTIVE     // 非常活跃
}
```

### HealthGoal（健康目标）

```kotlin
enum class HealthGoal {
    WEIGHT_LOSS,       // 减肥
    MUSCLE_GAIN,       // 增肌
    MAINTENANCE,       // 保持
    HEALTHY_EATING     // 健康饮食
}
```

---

## 数据流向

### 营养分析流程

```
MealPlan (餐食计划)
    ↓
NutritionAnalyzer
    ↓ (从 Recipe 获取营养信息)
TotalNutrition (总营养)
    ↓
NutritionAssessment (营养评估)
    ↓
DailyNutritionReport / WeeklyNutritionReport
```

### 数据导出流程

```
Recipe (菜谱)
    ↓
ExportRecipe (导出菜谱)
    ↓
RecipeExportData (导出数据)
    ↓
Gson 序列化
    ↓
JSON 文件
```

### 数据导入流程

```
JSON 文件
    ↓
Gson 反序列化
    ↓
RecipeExportData (导入数据)
    ↓
ExportRecipe (导入菜谱)
    ↓
Recipe (菜谱)
    ↓
数据库
```

---

## 数据验证

### 导入数据验证

1. **版本检查**
   - 检查文件版本是否兼容

2. **数据完整性**
   - 必填字段检查
   - 数据类型检查

3. **重复检查**
   - 检查菜谱ID是否重复
   - 重复菜谱可以跳过或更新

### 营养数据验证

1. **数值范围**
   - 热量：0-10000 卡路里
   - 蛋白质：0-500 克
   - 脂肪：0-500 克
   - 碳水：0-1000 克
   - 纤维：0-100 克
   - 钠：0-20000 毫克

2. **一致性**
   - 总营养 = 各餐营养之和
   - 营养评分 0-100

---

## 索引设计

### nutrition_info 表索引

```sql
-- recipeId 是主键，自动创建索引
CREATE INDEX idx_nutrition_info_recipe_id ON nutrition_info(recipe_id);
```

---

## 参考资料

- [Room Database](https://developer.android.com/training/data-storage/room)
- [Gson User Guide](https://github.com/google/gson/blob/master/UserGuide.md)
- [Kotlin Data Classes](https://kotlinlang.org/docs/data-classes.html)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-20
**负责人**: Jude 🦞
