package com.homepantry.data.nutrition

import com.homepantry.data.dao.NutritionInfoDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.MealPlan
import com.homepantry.data.entity.MealType
import kotlinx.coroutines.flow.first

/**
 * 营养分析器 - 分析菜单的营养情况
 */
class NutritionAnalyzer(
    private val recipeDao: RecipeDao,
    private val nutritionInfoDao: NutritionInfoDao
) {

    /**
     * 分析单日营养
     */
    suspend fun analyzeDailyNutrition(mealPlans: List<MealPlan>): DailyNutritionReport {
        var totalCalories = 0
        var totalProtein = 0.0
        var totalFat = 0.0
        var totalCarbs = 0.0
        var totalFiber = 0.0
        var totalSodium = 0

        val mealNutrition = mutableMapOf<MealType, MealNutrition>()

        mealPlans.forEach { mealPlan ->
            val nutrition = getNutritionForRecipe(mealPlan.recipeId, mealPlan.servings)
            mealNutrition[mealPlan.mealType] = nutrition

            totalCalories += nutrition.calories
            totalProtein += nutrition.protein
            totalFat += nutrition.fat
            totalCarbs += nutrition.carbs
            totalFiber += nutrition.fiber
            totalSodium += nutrition.sodium
        }

        // 评估营养均衡
        val assessment = assessNutritionBalance(
            calories = totalCalories,
            protein = totalProtein,
            fat = totalFat,
            carbs = totalCarbs,
            fiber = totalFiber
        )

        return DailyNutritionReport(
            date = mealPlans.firstOrNull()?.date ?: 0,
            mealNutrition = mealNutrition,
            totalNutrition = TotalNutrition(
                calories = totalCalories,
                protein = totalProtein,
                fat = totalFat,
                carbs = totalCarbs,
                fiber = totalFiber,
                sodium = totalSodium
            ),
            assessment = assessment
        )
    }

    /**
     * 分析周营养
     */
    suspend fun analyzeWeeklyNutrition(
        weeklyMealPlans: Map<Long, List<MealPlan>>
    ): WeeklyNutritionReport {
        val dailyReports = mutableListOf<DailyNutritionReport>()
        var totalWeeklyCalories = 0
        var totalWeeklyProtein = 0.0

        weeklyMealPlans.forEach { (date, mealPlans) ->
            val dailyReport = analyzeDailyNutrition(mealPlans)
            dailyReports.add(dailyReport)
            totalWeeklyCalories += dailyReport.totalNutrition.calories
            totalWeeklyProtein += dailyReport.totalNutrition.protein
        }

        // 计算平均值
        val days = weeklyMealPlans.size
        val avgDailyCalories = if (days > 0) totalWeeklyCalories / days else 0
        val avgDailyProtein = if (days > 0) totalWeeklyProtein / days else 0.0

        // 趋势分析
        val trends = analyzeTrends(dailyReports)

        return WeeklyNutritionReport(
            dailyReports = dailyReports,
            averageNutrition = AverageNutrition(
                avgCalories = avgDailyCalories,
                avgProtein = avgDailyProtein
            ),
            trends = trends,
            suggestions = generateWeeklySuggestions(dailyReports)
        )
    }

    /**
     * 比较实际摄入与推荐摄入
     */
    suspend fun compareToRecommendations(
        mealPlans: List<MealPlan>,
        gender: Gender = Gender.FEMALE,
        age: Int = 30,
        activityLevel: ActivityLevel = ActivityLevel.MODERATE
    ): NutritionComparison {
        val report = analyzeDailyNutrition(mealPlans)
        val recommendations = getRecommendations(gender, age, activityLevel)

        return NutritionComparison(
            actual = report.totalNutrition,
            recommended = recommendations,
            differences = calculateDifferences(report.totalNutrition, recommendations)
        )
    }

    /**
     * 获取菜谱的营养信息
     */
    suspend fun getNutritionForRecipe(
        recipeId: String,
        servings: Int = 1
    ): MealNutrition {
        val nutritionInfo = nutritionInfoDao.getNutritionByRecipe(recipeId)

        return if (nutritionInfo != null) {
            // 根据份数调整
            val ratio = servings.toFloat() / nutritionInfo.servingSize
            MealNutrition(
                recipeId = recipeId,
                calories = (nutritionInfo.calories ?: 0) * ratio,
                protein = (nutritionInfo.protein ?: 0.0) * ratio,
                fat = (nutritionInfo.fat ?: 0.0) * ratio,
                carbs = (nutritionInfo.carbs ?: 0.0) * ratio,
                fiber = (nutritionInfo.fiber ?: 0.0) * ratio,
                sodium = (nutritionInfo.sodium ?: 0) * ratio
            )
        } else {
            // 默认值（如果没有营养信息）
            MealNutrition(
                recipeId = recipeId,
                calories = 400 * servings,
                protein = 20.0 * servings,
                fat = 15.0 * servings,
                carbs = 50.0 * servings,
                fiber = 5.0 * servings,
                sodium = 800 * servings
            )
        }
    }

    /**
     * 获取营养建议
     */
    fun getNutritionAdvice(
        report: DailyNutritionReport,
        healthGoal: HealthGoal
    ): List<NutritionAdvice> {
        val advice = mutableListOf<NutritionAdvice>()

        when (healthGoal) {
            HealthGoal.WEIGHT_LOSS -> {
                if (report.totalNutrition.calories > 2000) {
                    advice.add(NutritionAdvice(
                        type = AdviceType.WARNING,
                        category = "热量",
                        message = "今日热量摄入较高，建议控制在1500-1800卡路里",
                        suggestion = "可以减少主食份量，增加蔬菜比例"
                    ))
                }
            }
            HealthGoal.MUSCLE_GAIN -> {
                if (report.totalNutrition.protein < 100) {
                    advice.add(NutritionAdvice(
                        type = AdviceType.SUGGESTION,
                        category = "蛋白质",
                        message = "蛋白质摄入不足，增肌期建议每日100-150g",
                        suggestion = "可以增加瘦肉、鸡蛋、豆制品等高蛋白食物"
                    ))
                }
            }
            HealthGoal.HEALTHY_EATING -> {
                if (report.totalNutrition.fiber < 25) {
                    advice.add(NutritionAdvice(
                        type = AdviceType.INFO,
                        category = "膳食纤维",
                        message = "膳食纤维偏低，建议每日摄入25-30g",
                        suggestion = "多吃全谷物、蔬菜和水果"
                    ))
                }
            }
            else -> {
                // 综合建议
                if (report.totalNutrition.sodium > 2300) {
                    advice.add(NutritionAdvice(
                        type = AdviceType.WARNING,
                        category = "钠",
                        message = "钠摄入量偏高，建议每日不超过2300mg",
                        suggestion = "减少盐和含钠调料的使用"
                    ))
                }
            }
        }

        return advice
    }

    // === 私有方法 ===

    private fun assessNutritionBalance(
        calories: Int,
        protein: Double,
        fat: Double,
        carbs: Double,
        fiber: Double
    ): NutritionAssessment {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 热量检查
        when {
            calories < 1200 -> warnings.add("热量摄入过低")
            calories > 2500 -> warnings.add("热量摄入偏高")
        }

        // 蛋白质检查
        when {
            protein < 50 -> issues.add("蛋白质摄入不足")
            protein > 150 -> warnings.add("蛋白质摄入过高")
        }

        // 脂肪检查
        when {
            fat < 30 -> issues.add("脂肪摄入过低")
            fat > 80 -> warnings.add("脂肪摄入偏高")
        }

        // 碳水检查
        when {
            carbs < 100 -> issues.add("碳水化合物摄入不足")
            carbs > 300 -> warnings.add("碳水化合物摄入偏高")
        }

        // 纤维检查
        if (fiber < 20) {
            issues.add("膳食纤维摄入不足")
        }

        // 计算营养评分
        val score = calculateNutritionScore(calories, protein, fat, carbs, fiber)

        return NutritionAssessment(
            score = score,
            level = when {
                score >= 80 -> NutritionLevel.EXCELLENT
                score >= 60 -> NutritionLevel.GOOD
                score >= 40 -> NutritionLevel.FAIR
                else -> NutritionLevel.POOR
            },
            issues = issues,
            warnings = warnings
        )
    }

    private fun calculateNutritionScore(
        calories: Int,
        protein: Double,
        fat: Double,
        carbs: Double,
        fiber: Double
    ): Int {
        var score = 100

        // 热量评分 (20分)
        when {
            calories in 1500..2200 -> score -= 0
            calories in 1200..1499 || calories in 2201..2500 -> score -= 5
            else -> score -= 10
        }

        // 蛋白质评分 (25分)
        when {
            protein in 60..100 -> score -= 0
            protein in 40..59 || protein in 101..150 -> score -= 5
            else -> score -= 15
        }

        // 脂肪评分 (20分)
        when {
            fat in 40..70 -> score -= 0
            fat in 30..39 || fat in 71..80 -> score -= 5
            else -> score -= 10
        }

        // 碳水评分 (20分)
        when {
            carbs in 150..250 -> score -= 0
            carbs in 100..149 || carbs in 251..300 -> score -= 5
            else -> score -= 10
        }

        // 纤维评分 (15分)
        when {
            fiber >= 25 -> score -= 0
            fiber in 20..24 -> score -= 5
            else -> score -= 15
        }

        return score.coerceAtLeast(0)
    }

    private fun analyzeTrends(dailyReports: List<DailyNutritionReport>): NutritionTrends {
        if (dailyReports.size < 2) {
            return NutritionTrends(
                calorieTrend = TrendType.STABLE,
                proteinTrend = TrendType.STABLE,
                summary = "数据不足以分析趋势"
            )
        }

        val calorieTrend = detectTrend(dailyReports.map { it.totalNutrition.calories })
        val proteinTrend = detectTrend(dailyReports.map { it.totalNutrition.protein })

        return NutritionTrends(
            calorieTrend = calorieTrend,
            proteinTrend = proteinTrend,
            summary = buildTrendSummary(calorieTrend, proteinTrend)
        )
    }

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

    private fun detectTrend(values: List<Int>): TrendType {
        return detectTrend(values.map { it.toDouble() })
    }

    private fun buildTrendSummary(
        calorieTrend: TrendType,
        proteinTrend: TrendType
    ): String {
        val calorieDesc = when (calorieTrend) {
            TrendType.INCREASING -> "热量摄入呈上升趋势"
            TrendType.DECREASING -> "热量摄入呈下降趋势"
            TrendType.STABLE -> "热量摄入保持稳定"
        }

        val proteinDesc = when (proteinTrend) {
            TrendType.INCREASING -> "蛋白质摄入呈上升趋势"
            TrendType.DECREASING -> "蛋白质摄入呈下降趋势"
            TrendType.STABLE -> "蛋白质摄入保持稳定"
        }

        return "$calorieDesc，$proteinDesc"
    }

    private fun generateWeeklySuggestions(reports: List<DailyNutritionReport>): List<String> {
        val suggestions = mutableListOf<String>()

        // 检查整体趋势
        val avgScore = reports.map { it.assessment.score }.average()
        if (avgScore < 60) {
            suggestions.add("整体营养质量有待提高，建议增加蔬菜和优质蛋白质")
        }

        // 检查波动
        val scores = reports.map { it.assessment.score }
        val variance = scores.variance()
        if (variance > 100) {
            suggestions.add("每日营养摄入波动较大，建议保持更稳定的饮食结构")
        }

        return suggestions
    }

    private fun getRecommendations(
        gender: Gender,
        age: Int,
        activityLevel: ActivityLevel
    ): RecommendedNutrition {
        // 基础代谢率计算 (Mifflin-St Jeor 公式)
        val bmr = when (gender) {
            Gender.MALE -> 10 * 70 + 6.25 * 175 - 5 * age + 5
            Gender.FEMALE -> 10 * 60 + 6.25 * 165 - 5 * age - 161
        }

        // 活动系数
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
            protein = (dailyCalories * 0.15 / 4).toInt(), // 15%热量
            fat = (dailyCalories * 0.25 / 9).toInt(),     // 25%热量
            carbs = (dailyCalories * 0.50 / 4).toInt(),   // 50%热量
            fiber = 25,
            sodium = 2300
        )
    }

    private fun calculateDifferences(
        actual: TotalNutrition,
        recommended: RecommendedNutrition
    ): NutritionDifferences {
        return NutritionDifferences(
            calories = actual.calories - recommended.calories,
            protein = actual.protein - recommended.protein,
            fat = actual.fat - recommended.fat,
            carbs = actual.carbs - recommended.carbs,
            fiber = actual.fiber - recommended.fiber,
            sodium = actual.sodium - recommended.sodium
        )
    }

    // === 数据类 ===

    data class MealNutrition(
        val recipeId: String,
        val calories: Int,
        val protein: Double,
        val fat: Double,
        val carbs: Double,
        val fiber: Double,
        val sodium: Int
    )

    data class DailyNutritionReport(
        val date: Long,
        val mealNutrition: Map<MealType, MealNutrition>,
        val totalNutrition: TotalNutrition,
        val assessment: NutritionAssessment
    )

    data class TotalNutrition(
        val calories: Int,
        val protein: Double,
        val fat: Double,
        val carbs: Double,
        val fiber: Double,
        val sodium: Int
    )

    data class NutritionAssessment(
        val score: Int,
        val level: NutritionLevel,
        val issues: List<String>,
        val warnings: List<String>
    )

    enum class NutritionLevel {
        EXCELLENT, GOOD, FAIR, POOR
    }

    data class WeeklyNutritionReport(
        val dailyReports: List<DailyNutritionReport>,
        val averageNutrition: AverageNutrition,
        val trends: NutritionTrends,
        val suggestions: List<String>
    )

    data class AverageNutrition(
        val avgCalories: Int,
        val avgProtein: Double
    )

    data class NutritionTrends(
        val calorieTrend: TrendType,
        val proteinTrend: TrendType,
        val summary: String
    )

    enum class TrendType {
        INCREASING, DECREASING, STABLE
    }

    data class NutritionComparison(
        val actual: TotalNutrition,
        val recommended: RecommendedNutrition,
        val differences: NutritionDifferences
    )

    data class RecommendedNutrition(
        val calories: Int,
        val protein: Int,
        val fat: Int,
        val carbs: Int,
        val fiber: Int,
        val sodium: Int
    )

    data class NutritionDifferences(
        val calories: Int,
        val protein: Double,
        val fat: Double,
        val carbs: Double,
        val fiber: Double,
        val sodium: Int
    )

    data class NutritionAdvice(
        val type: AdviceType,
        val category: String,
        val message: String,
        val suggestion: String
    )

    enum class AdviceType {
        INFO, SUGGESTION, WARNING
    }

    enum class Gender {
        MALE, FEMALE
    }

    enum class ActivityLevel {
        SEDENTARY,      // 久坐
        LIGHT,          // 轻度活动
        MODERATE,       // 中度活动
        ACTIVE,         // 活跃
        VERY_ACTIVE     // 非常活跃
    }

    enum class HealthGoal {
        WEIGHT_LOSS,       // 减肥
        MUSCLE_GAIN,       // 增肌
        MAINTENANCE,       // 保持
        HEALTHY_EATING     // 健康饮食
    }
}
