# Plan: 营养分析与数据统计

**Spec ID**: 015
**功能名称**: 营养分析与数据统计
**优先级**: P1
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-20

---

## 技术栈

### 核心技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9+ | 开发语言 |
| Coroutines | 1.7+ | 异步处理 |
| Flow | Kotlin | 数据流 |
| Gson | 2.10+ | JSON 序列化 |
| Room | 2.6+ | 数据库访问 |

### 主要依赖

```kotlin
// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

// Gson
implementation("com.google.code.gson:gson:2.10.1")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
```

---

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────┐
│                    Presentation Layer               │
│  ┌──────────────────┐  ┌──────────────────────┐   │
│  │ Nutrition Screen │  │ Export/Import UI     │   │
│  │   (营养分析页面) │  │  (导出/导入页面)     │   │
│  └──────────────────┘  └──────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                     Business Layer                  │
│  ┌──────────────────┐  ┌──────────────────────┐   │
│  │ NutritionAnalyzer│  │ RecipeExporter      │   │
│  │   (营养分析器)    │  │  (菜谱导出器)       │   │
│  └──────────────────┘  └──────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                        Data Layer                    │
│  ┌──────────────────┐  ┌──────────────────────┐   │
│  │ RecipeDao        │  │ NutritionInfoDao     │   │
│  │  (菜谱数据访问)  │  │  (营养信息数据访问)  │   │
│  └──────────────────┘  └──────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                     Room Database                    │
│              (SQLite 本地数据库)                     │
└─────────────────────────────────────────────────────┘
```

---

## 数据模型

### 核心实体

#### NutritionInfo（营养信息）
```kotlin
@Entity(tableName = "nutrition_info")
data class NutritionInfo(
    @PrimaryKey val recipeId: String,
    val servingSize: Int,           // 每份大小
    val calories: Int?,            // 热量（卡路里）
    val protein: Double?,          // 蛋白质（克）
    val fat: Double?,             // 脂肪（克）
    val carbs: Double?,           // 碳水化合物（克）
    val fiber: Double?,            // 膳食纤维（克）
    val sodium: Int?               // 钠（毫克）
)
```

### 数据类

#### DailyNutritionReport（单日营养报告）
```kotlin
data class DailyNutritionReport(
    val date: Long,                           // 日期
    val mealNutrition: Map<MealType, MealNutrition>,  // 各餐营养
    val totalNutrition: TotalNutrition,               // 总营养
    val assessment: NutritionAssessment              // 营养评估
)
```

#### WeeklyNutritionReport（周营养报告）
```kotlin
data class WeeklyNutritionReport(
    val dailyReports: List<DailyNutritionReport>,  // 每日报告
    val averageNutrition: AverageNutrition,         // 平均营养
    val trends: NutritionTrends,                    // 营养趋势
    val suggestions: List<String>                   // 改进建议
)
```

#### TotalNutrition（总营养）
```kotlin
data class TotalNutrition(
    val calories: Int,          // 热量
    val protein: Double,         // 蛋白质
    val fat: Double,             // 脂肪
    val carbs: Double,           // 碳水
    val fiber: Double,           // 纤维
    val sodium: Int              // 钠
)
```

#### NutritionAssessment（营养评估）
```kotlin
data class NutritionAssessment(
    val score: Int,              // 评分（0-100）
    val level: NutritionLevel,   // 等级
    val issues: List<String>,    // 问题
    val warnings: List<String>   // 警告
)

enum class NutritionLevel {
    EXCELLENT,  // 优秀（80-100分）
    GOOD,       // 良好（60-79分）
    FAIR,       // 一般（40-59分）
    POOR        // 较差（0-39分）
}
```

#### NutritionTrends（营养趋势）
```kotlin
data class NutritionTrends(
    val calorieTrend: TrendType,  // 热量趋势
    val proteinTrend: TrendType,  // 蛋白质趋势
    val summary: String           // 趋势总结
)

enum class TrendType {
    INCREASING,  // 上升
    DECREASING,  // 下降
    STABLE       // 稳定
}
```

#### ExportRecipe（导出菜谱）
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

#### RecipeExportData（菜谱导出数据）
```kotlin
data class RecipeExportData(
    val version: String = "1.0",        // 版本
    val exportDate: Long,               // 导出日期
    val recipes: List<ExportRecipe>      // 菜谱列表
)
```

---

## 关键算法

### 1. 营养评分算法

**目的**: 将营养数据转换为易于理解的分数

**算法步骤**:
1. 热量评分（20分）：
   - 1500-2200卡路里：不扣分
   - 1200-1499或2201-2500：扣5分
   - 其他：扣10分

2. 蛋白质评分（25分）：
   - 60-100克：不扣分
   - 40-59或101-150：扣5分
   - 其他：扣15分

3. 脂肪评分（20分）：
   - 40-70克：不扣分
   - 30-39或71-80：扣5分
   - 其他：扣10分

4. 碳水评分（20分）：
   - 150-250克：不扣分
   - 100-149或251-300：扣5分
   - 其他：扣10分

5. 纤维评分（15分）：
   - ≥25克：不扣分
   - 20-24克：扣5分
   - <20克：扣15分

**代码**:
```kotlin
private fun calculateNutritionScore(
    calories: Int,
    protein: Double,
    fat: Double,
    carbs: Double,
    fiber: Double
): Int {
    var score = 100

    score -= when {
        calories in 1500..2200 -> 0
        calories in 1200..1499 || calories in 2201..2500 -> 5
        else -> 10
    }

    score -= when {
        protein in 60.0..100.0 -> 0
        protein in 40.0..59.0 || protein in 101.0..150.0 -> 5
        else -> 15
    }

    // ... 其他指标

    return score.coerceAtLeast(0)
}
```

### 2. 趋势检测算法

**目的**: 检测营养摄入的变化趋势

**算法步骤**:
1. 将数据分为前半部分和后半部分
2. 计算两部分的平均值
3. 计算差异
4. 与阈值比较（平均值的10%）
5. 判断趋势

**代码**:
```kotlin
private fun detectTrend(values: List<Double>): TrendType {
    if (values.size < 2) return TrendType.STABLE

    val firstHalf = values.take(values.size / 2).average()
    val secondHalf = values.drop(values.size / 2).average()
    val difference = secondHalf - firstHalf
    val threshold = values.average() * 0.1

    return when {
        difference > threshold -> TrendType.INCREASING
        difference < -threshold -> TrendType.DECREASING
        else -> TrendType.STABLE
    }
}
```

### 3. 推荐摄入量计算算法

**目的**: 根据个人信息计算每日推荐营养摄入量

**算法**:
- 使用 Mifflin-St Jeor 公式计算基础代谢率（BMR）
- 根据活动水平计算活动系数
- 每日热量 = BMR × 活动系数
- 蛋白质：15%热量（÷4）
- 脂肪：25%热量（÷9）
- 碳水：50%热量（÷4）
- 纤维：25克（固定）
- 钠：2300毫克（固定）

**代码**:
```kotlin
private fun getRecommendations(
    gender: Gender,
    age: Int,
    activityLevel: ActivityLevel
): RecommendedNutrition {
    val bmr = when (gender) {
        Gender.MALE -> 10 * 70 + 6.25 * 175 - 5 * age + 5
        Gender.FEMALE -> 10 * 60 + 6.25 * 165 - 5 * age - 161
    }

    val activityMultiplier = when (activityLevel) {
        ActivityLevel.SEDENTARY -> 1.2
        ActivityLevel.LIGHT -> 1.375
        ActivityLevel.MODERATE -> 1.55
        ActivityLevel.ACTIVE -> 1.725
        ActivityLevel.VERY_ACTIVE -> 1.9
    }

    val dailyCalories = (bmr * activityMultiplier).toInt()

    return RecommendedNutrition(
        calories = dailyCalories,
        protein = (dailyCalories * 0.15 / 4).toInt(),
        fat = (dailyCalories * 0.25 / 9).toInt(),
        carbs = (dailyCalories * 0.50 / 4).toInt(),
        fiber = 25,
        sodium = 2300
    )
}
```

---

## 性能优化

### 数据库优化

1. **索引优化**
   - 为 `recipe_id` 添加索引
   - 为 `date` 添加索引（餐食计划）

2. **查询优化**
   - 使用 Flow 进行流式查询
   - 使用 DAO 的 `@Query` 进行批量查询

3. **缓存策略**
   - 缓存推荐摄入量（相同参数）
   - 缓存营养分析结果（短期）

### UI 优化

1. **异步加载**
   - 使用 Coroutines 进行异步计算
   - 显示加载进度

2. **分页加载**
   - 周营养报告分日显示
   - 菜谱导出进度条

---

## 测试策略

### 单元测试

**NutritionAnalyzer 测试**:
- 营养计算准确性
- 营养评分逻辑
- 趋势检测算法
- 推荐摄入量计算

**RecipeExporter 测试**:
- 导出功能
- 导入功能
- 数据完整性
- 错误处理

### 集成测试

- 营养分析完整流程
- 数据导出完整流程
- 错误场景处理

### 测试覆盖率目标

- 代码覆盖率：≥ 70%
- 核心算法覆盖率：≥ 90%

---

## 部署策略

### 数据库迁移

- `NutritionInfo` 表迁移脚本
- 添加必要的索引

### 配置更新

- 导出文件格式版本控制
- 导入验证规则

---

## 已知问题和优化方向

### 已知问题

1. **营养数据不完整**
   - 影响：计算准确性降低
   - 解决方案：提供默认值，提示用户补充

2. **导入格式错误**
   - 影响：导入失败
   - 解决方案：严格验证，提供错误提示

### 优化方向

1. **图表可视化**
   - 添加饼图显示营养占比
   - 添加折线图显示趋势

2. **AI 自动估算**
   - 根据菜谱自动估算营养信息
   - 集成外部营养数据库

3. **社交分享**
   - 分享营养报告到社交平台
   - 导出为 PDF

---

## 参考资料

- [Mifflin-St Jeor 公式](https://en.wikipedia.org/wiki/Mifflin-St_Jeor_equation)
- [中国居民膳食指南](http://www.dietaryguideline.org.cn/)
- [NutritionAnalyzer.kt](../../../android/app/src/main/java/com/homepantry/data/nutrition/NutritionAnalyzer.kt)
- [RecipeExporter.kt](../../../android/app/src/main/java/com/homepantry/data/export/RecipeExporter.kt)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-20
**负责人**: Jude 🦞
