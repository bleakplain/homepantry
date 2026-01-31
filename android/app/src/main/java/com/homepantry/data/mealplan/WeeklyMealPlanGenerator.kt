package com.homepantry.data.mealplan

import com.homepantry.data.dao.MealPlanDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.dao.UserProfileDao
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.MealPlan
import com.homepantry.data.entity.MealType
import com.homepantry.data.entity.Recipe
import kotlinx.coroutines.flow.first
import java.util.*

/**
 * 周菜单生成器 - AI 辅助生成一周菜单
 */
class WeeklyMealPlanGenerator(
    private val recipeDao: RecipeDao,
    private val mealPlanDao: MealPlanDao,
    private val userProfileDao: UserProfileDao
) {

    /**
     * 生成周菜单方案
     */
    suspend fun generateWeeklyMealPlans(
        startDate: Long,
        config: MealPlanConfig
    ): Result<List<MealPlanOption>> {
        return try {
            // 获取用户偏好
            val profile = userProfileDao.getProfileById("default")
            val userPrefs = UserPreferences.fromProfile(profile)

            // 获取可用菜谱
            val allRecipes = recipeDao.getAllRecipes().first()
            val availableRecipes = filterRecipesByPreferences(allRecipes, userPrefs, config)

            // 生成日期范围
            val dates = getDateRange(startDate, config.days)

            // 生成多个方案
            val options = mutableListOf<MealPlanOption>()

            // 方案1：营养均衡
            options.add(generateBalancedOption(dates, availableRecipes, config, userPrefs))

            // 方案2：快手省时
            if (config.includeQuickOption) {
                options.add(generateQuickOption(dates, availableRecipes, config, userPrefs))
            }

            // 方案3：多样性
            if (config.includeVarietyOption) {
                options.add(generateVarietyOption(dates, availableRecipes, config, userPrefs))
            }

            Result.success(options)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 保存选择的方案
     */
    suspend fun saveMealPlanOption(
        option: MealPlanOption
    ): Result<Unit> {
        return try {
            // 先清除该时间段内的现有计划
            option.mealPlans.forEach { mealPlan ->
                mealPlanDao.deleteMealPlansForDateRange(
                    startTime = getStartTimeOfDay(mealPlan.date),
                    endTime = getEndTimeOfDay(mealPlan.date)
                )
            }

            // 插入新计划
            option.mealPlans.forEach { mealPlan ->
                mealPlanDao.insertMealPlan(mealPlan)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 检查菜单是否有问题
     */
    fun analyzeMenu(mealPlans: List<MealPlan>): MenuAnalysis {
        val recipeIds = mealPlans.map { it.recipeId }.distinct()

        // 检查食材多样性
        val ingredientDiversity = checkIngredientDiversity(recipeIds)

        // 检查口味平衡
        val spiceBalance = checkSpiceBalance(recipeIds)

        // 检查营养均衡
        val nutritionBalance = checkNutritionBalance(mealPlans)

        return MenuAnalysis(
            ingredientDiversity = ingredientDiversity,
            spiceBalance = spiceBalance,
            nutritionBalance = nutritionBalance,
            suggestions = generateSuggestions(ingredientDiversity, spiceBalance, nutritionBalance)
        )
    }

    // === 私有方法 ===

    private fun filterRecipesByPreferences(
        recipes: List<Recipe>,
        prefs: UserPreferences,
        config: MealPlanConfig
    ): List<Recipe> {
        return recipes.filter { recipe ->
            // 时间限制
            if (config.maxCookingTime != null && recipe.cookingTime > config.maxCookingTime) {
                return@filter false
            }

            // 难度限制
            if (config.maxDifficulty != null && recipe.difficulty > config.maxDifficulty) {
                return@filter false
            }

            // 忌口过滤
            val disliked = prefs.dislikedIngredients.map { it.lowercase() }
            val recipeIngredients = getRecipeIngredients(recipe.id)
            if (recipeIngredients.any { it in disliked }) {
                return@filter false
            }

            true
        }
    }

    private suspend fun getRecipeIngredients(recipeId: String): List<String> {
        // 从数据库获取菜谱食材
        val ingredients = recipeDao.getRecipeIngredients(recipeId)
        return ingredients.map { it.ingredientId }
    }

    private fun generateBalancedOption(
        dates: List<DateInfo>,
        recipes: List<Recipe>,
        config: MealPlanConfig,
        prefs: UserPreferences
    ): MealPlanOption {
        val mealPlans = mutableListOf<MealPlan>()
        val usedRecipes = mutableSetOf<String>()

        dates.forEach { dateInfo ->
            config.mealTypes.forEach { mealType ->
                // 选择一个菜谱，确保多样性
                val recipe = selectRecipeForMeal(
                    recipes = recipes,
                    dateInfo = dateInfo,
                    mealType = mealType,
                    usedRecipes = usedRecipes,
                    prefs = prefs,
                    strategy = SelectionStrategy.BALANCED
                )

                if (recipe != null) {
                    mealPlans.add(createMealPlan(dateInfo, mealType, recipe, config.servings))
                    usedRecipes.add(recipe.id)
                }
            }
        }

        return MealPlanOption(
            name = "营养均衡",
            description = "注重荤素搭配和营养均衡",
            mealPlans = mealPlans
        )
    }

    private fun generateQuickOption(
        dates: List<DateInfo>,
        recipes: List<Recipe>,
        config: MealPlanConfig,
        prefs: UserPreferences
    ): MealPlanOption {
        val mealPlans = mutableListOf<MealPlan>()
        val usedRecipes = mutableSetOf<String>()

        // 筛选出快手菜
        val quickRecipes = recipes.filter { it.cookingTime <= 30 }

        dates.forEach { dateInfo ->
            config.mealTypes.forEach { mealType ->
                val recipe = selectRecipeForMeal(
                    recipes = quickRecipes,
                    dateInfo = dateInfo,
                    mealType = mealType,
                    usedRecipes = usedRecipes,
                    prefs = prefs,
                    strategy = SelectionStrategy.QUICK
                )

                if (recipe != null) {
                    mealPlans.add(createMealPlan(dateInfo, mealType, recipe, config.servings))
                    usedRecipes.add(recipe.id)
                }
            }
        }

        return MealPlanOption(
            name = "快手省时",
            description = "所有菜谱都在30分钟内完成",
            mealPlans = mealPlans
        )
    }

    private fun generateVarietyOption(
        dates: List<DateInfo>,
        recipes: List<Recipe>,
        config: MealPlanConfig,
        prefs: UserPreferences
    ): MealPlanOption {
        val mealPlans = mutableListOf<MealPlan>()
        val usedRecipes = mutableSetOf<String>()
        val usedCategories = mutableSetOf<String?>()

        dates.forEach { dateInfo ->
            config.mealTypes.forEach { mealType ->
                val recipe = selectRecipeForMeal(
                    recipes = recipes,
                    dateInfo = dateInfo,
                    mealType = mealType,
                    usedRecipes = usedRecipes,
                    prefs = prefs,
                    strategy = SelectionStrategy.VARIETY
                )

                if (recipe != null) {
                    mealPlans.add(createMealPlan(dateInfo, mealType, recipe, config.servings))
                    usedRecipes.add(recipe.id)
                    usedCategories.add(recipe.categoryId)
                }
            }
        }

        return MealPlanOption(
            name = "口味多样",
            description = "每天不同风格，丰富口感",
            mealPlans = mealPlans
        )
    }

    private fun selectRecipeForMeal(
        recipes: List<Recipe>,
        dateInfo: DateInfo,
        mealType: MealType,
        usedRecipes: Set<String>,
        prefs: UserPreferences,
        strategy: SelectionStrategy
    ): Recipe? {
        // 基于策略筛选候选菜谱
        val candidates = when (strategy) {
            SelectionStrategy.BALANCED -> {
                // 考虑营养均衡，避免重复主食材
                recipes.filter { it.id !in usedRecipes }
                    .sortedByDescending { it.averageRating }
            }
            SelectionStrategy.QUICK -> {
                // 优先选择最快的
                recipes.filter { it.id !in usedRecipes }
                    .sortedBy { it.cookingTime }
            }
            SelectionStrategy.VARIETY -> {
                // 优先选择不同类别
                recipes.filter { it.id !in usedRecipes }
                    .shuffled()
            }
        }

        // 根据餐次调整
        val mealTypeFiltered = when (mealType) {
            MealType.BREAKFAST -> candidates.filter { it.cookingTime <= 20 }
            MealType.LUNCH -> candidates
            MealType.DINNER -> candidates
            MealType.SNACK -> candidates.filter { it.cookingTime <= 15 }
        }

        return mealTypeFiltered.firstOrNull()
    }

    private fun createMealPlan(
        dateInfo: DateInfo,
        mealType: MealType,
        recipe: Recipe,
        servings: Int
    ): MealPlan {
        return MealPlan(
            date = dateInfo.timestamp,
            mealType = mealType,
            recipeId = recipe.id,
            servings = servings,
            notes = null
        )
    }

    private fun getDateRange(startDate: Long, days: Int): List<DateInfo> {
        val millisPerDay = 24 * 60 * 60 * 1000L
        return (0 until days).map { index ->
            val timestamp = startDate + index * millisPerDay
            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
            DateInfo(
                timestamp = timestamp,
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
                isWeekend = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                          calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            )
        }
    }

    private fun getStartTimeOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndTimeOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun checkIngredientDiversity(recipeIds: List<String>): DiversityScore {
        // 简化实现：检查是否有重复的主食材
        val uniqueRatio = if (recipeIds.isNotEmpty()) {
            recipeIds.distinct().size.toFloat() / recipeIds.size
        } else 0f

        return DiversityScore(
            score = uniqueRatio,
            level = when {
                uniqueRatio >= 0.8 -> DiversityLevel.EXCELLENT
                uniqueRatio >= 0.6 -> DiversityLevel.GOOD
                uniqueRatio >= 0.4 -> DiversityLevel.FAIR
                else -> DiversityLevel.POOR
            }
        )
    }

    private fun checkSpiceBalance(recipeIds: List<String>): SpiceBalance {
        // 简化实现：假设简单菜谱不太辣
        val recipes = recipeIds.mapNotNull { id ->
            // 这里需要从数据库获取，简化处理
            Recipe(id, "", cookingTime = 30, servings = 2, difficulty = DifficultyLevel.MEDIUM)
        }

        val spiceLevel = when {
            recipes.all { it.difficulty == DifficultyLevel.EASY } -> SpiceLevel.MILD
            recipes.count { it.difficulty == DifficultyLevel.HARD } > recipes.size / 2 -> SpiceLevel.HOT
            else -> SpiceLevel.MEDIUM
        }

        return SpiceBalance(spiceLevel, "整体口味${getSpiceLevelName(spiceLevel)}")
    }

    private fun checkNutritionBalance(mealPlans: List<MealPlan>): NutritionBalance {
        // 简化实现：基于餐次分布
        val breakfastCount = mealPlans.count { it.mealType == MealType.BREAKFAST }
        val lunchCount = mealPlans.count { it.mealType == MealType.LUNCH }
        val dinnerCount = mealPlans.count { it.mealType == MealType.DINNER }

        return NutritionBalance(
            isBalanced = breakfastCount > 0 && lunchCount > 0 && dinnerCount > 0,
            message = "包含${breakfastCount}次早餐，${lunchCount}次午餐，${dinnerCount}次晚餐"
        )
    }

    private fun generateSuggestions(
        diversity: DiversityScore,
        spice: SpiceBalance,
        nutrition: NutritionBalance
    ): List<String> {
        val suggestions = mutableListOf<String>()

        when (diversity.level) {
            DiversityLevel.POOR -> suggestions.add("菜品重复较多，建议增加多样性")
            DiversityLevel.FAIR -> suggestions.add("可以尝试更多不同类型的菜品")
        }

        if (!nutrition.isBalanced) {
            suggestions.add("建议保证早中晚餐的均衡")
        }

        return suggestions
    }

    private fun getSpiceLevelName(level: SpiceLevel): String {
        return when (level) {
            SpiceLevel.MILD -> "清淡"
            SpiceLevel.MEDIUM -> "适中"
            SpiceLevel.HOT -> "偏重"
        }
    }

    // === 数据类 ===

    data class MealPlanOption(
        val name: String,
        val description: String,
        val mealPlans: List<MealPlan>
    )

    data class MealPlanConfig(
        val days: Int = 7,                           // 天数
        val mealTypes: List<MealType> = listOf(      // 餐次
            MealType.BREAKFAST,
            MealType.LUNCH,
            MealType.DINNER
        ),
        val servings: Int = 2,                      // 份数
        val maxCookingTime: Int? = null,            // 最大烹饪时间（分钟）
        val maxDifficulty: DifficultyLevel? = null, // 最大难度
        val includeQuickOption: Boolean = true,      // 是否包含快手方案
        val includeVarietyOption: Boolean = true     // 是否包含多样性方案
    )

    data class DateInfo(
        val timestamp: Long,
        val dayOfWeek: Int,
        val isWeekend: Boolean
    )

    data class UserPreferences(
        val preferredCuisines: List<String>,
        val spiceLevel: String,
        val dislikedIngredients: List<String>
    ) {
        companion object {
            fun fromProfile(profile: com.homepantry.data.entity.UserProfile?): UserPreferences {
                return UserPreferences(
                    preferredCuisines = emptyList(), // 从 JSON 解析
                    spiceLevel = profile?.spiceLevel?.name ?: "MEDIUM",
                    dislikedIngredients = emptyList() // 从 JSON 解析
                )
            }
        }
    }

    data class MenuAnalysis(
        val ingredientDiversity: DiversityScore,
        val spiceBalance: SpiceBalance,
        val nutritionBalance: NutritionBalance,
        val suggestions: List<String>
    )

    data class DiversityScore(
        val score: Float,
        val level: DiversityLevel
    )

    enum class DiversityLevel {
        EXCELLENT, GOOD, FAIR, POOR
    }

    data class SpiceBalance(
        val level: SpiceLevel,
        val message: String
    )

    enum class SpiceLevel {
        MILD, MEDIUM, HOT
    }

    data class NutritionBalance(
        val isBalanced: Boolean,
        val message: String
    )

    enum class SelectionStrategy {
        BALANCED, QUICK, VARIETY
    }
}
