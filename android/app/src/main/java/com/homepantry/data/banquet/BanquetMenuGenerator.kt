package com.homepantry.data.banquet

import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.MealType
import com.homepantry.data.entity.Recipe
import kotlinx.coroutines.flow.first
import kotlin.math.max
import kotlin.math.min

/**
 * 宴请模式 - 规划多人聚餐菜单
 */
class BanquetMenuGenerator(
    private val recipeDao: RecipeDao
) {

    /**
     * 生成宴请菜单
     */
    suspend fun generateBanquetMenu(config: BanquetConfig): Result<BanquetMenu> {
        return try {
            val allRecipes = recipeDao.getAllRecipes().first()

            // 筛选适合的菜谱
            val suitableRecipes = filterSuitableRecipes(allRecipes, config)

            // 生成菜单
            val menu = when (config.occasion) {
                BanquetOccasion.FAMILY_DINNER -> generateFamilyDinner(suitableRecipes, config)
                BanquetOccasion.FRIENDS_GATHERING -> generateFriendsGathering(suitableRecipes, config)
                BanquetOccasion.CELEBRATION -> generateCelebration(suitableRecipes, config)
                BanquetOccasion.CASUAL -> generateCasual(suitableRecipes, config)
            }

            // 生成时间规划
            val timePlan = generateTimePlan(menu, config.startTime)

            Result.success(BanquetMenu(
                config = config,
                courses = menu,
                timePlan = timePlan
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 分析菜单的食材搭配
     */
    fun analyzeMenuCompatibility(menu: List<BanquetCourse>): MenuAnalysis {
        val mainIngredients = mutableSetOf<String>()
        val cookingMethods = mutableSetOf<String>()
        val flavors = mutableSetOf<String>()

        menu.forEach { course ->
            mainIngredients.add(course.mainIngredient)
            cookingMethods.add(course.cookingMethod)
            flavors.add(course.flavorProfile)
        }

        val issues = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        // 检查主材重复
        if (mainIngredients.size < menu.size) {
            suggestions.add("主食材有重复，建议增加多样性")
        }

        // 检查烹饪方法
        if (cookingMethods.size < 2) {
            suggestions.add("烹饪方法单一，建议增加炒、蒸、煮等多种方式")
        }

        // 检查口味层次
        if (flavors.size < 2) {
            suggestions.add("口味单一，建议搭配酸甜、咸鲜等不同口味")
        }

        // 检查难度和时间
        val totalTime = menu.sumOf { course.estimatedTime }
        if (totalTime > 180) {
            issues.add("预计总用时超过3小时，建议简化部分菜品")
        }

        return MenuAnalysis(
            mainIngredients = mainIngredients.toList(),
            cookingMethods = cookingMethods.toList(),
            flavors = flavors.toList(),
            totalEstimatedTime = totalTime,
            difficultyLevel = calculateOverallDifficulty(menu),
            issues = issues,
            suggestions = suggestions
        )
    }

    // === 私有方法 ===

    private fun filterSuitableRecipes(
        recipes: List<Recipe>,
        config: BanquetConfig
    ): List<Recipe> {
        return recipes.filter { recipe ->
            // 排除不合适的菜
            if (config.dietaryRestrictions.isNotEmpty()) {
                // 这里可以添加忌口检查
            }

            // 时间限制
            if (config.maxTimePerDish != null && recipe.cookingTime > config.maxTimePerDish) {
                return@filter false
            }

            true
        }
    }

    private fun generateFamilyDinner(
        recipes: List<Recipe>,
        config: BanquetConfig
    ): List<BanquetCourse> {
        val courses = mutableListOf<BanquetCourse>()

        // 前菜/凉菜 (1-2道)
        val appetizers = recipes
            .filter { it.cookingTime <= 20 && it.difficulty == DifficultyLevel.EASY }
            .shuffled()
            .take(min(2, config.guestCount / 4))

        appetizers.forEachIndexed { index, recipe ->
            courses.add(BanquetCourse(
                courseType = CourseType.APPETIZER,
                order = courses.size + 1,
                recipe = recipe,
                servings = config.guestCount,
                estimatedTime = recipe.cookingTime
            ))
        }

        // 主菜 (2-3道)
        val mainCourses = recipes
            .filter { it.difficulty >= DifficultyLevel.MEDIUM }
            .sortedByDescending { it.averageRating }
            .take(3)

        mainCourses.forEach { recipe ->
            courses.add(BanquetCourse(
                courseType = CourseType.MAIN,
                order = courses.size + 1,
                recipe = recipe,
                servings = config.guestCount,
                estimatedTime = recipe.prepTime ?: 30 + recipe.cookingTime
            ))
        }

        // 汤 (1道)
        val soup = recipes
            .filter { it.name.contains("汤") }
            .shuffled()
            .firstOrNull()

        if (soup != null) {
            courses.add(BanquetCourse(
                courseType = CourseType.SOUP,
                order = courses.size,
                recipe = soup,
                servings = config.guestCount,
                estimatedTime = soup.cookingTime
            ))
        }

        // 重新排序
        reorderCourses(courses)

        return courses
    }

    private fun generateFriendsGathering(
        recipes: List<Recipe>,
        config: BanquetConfig
    ): List<BanquetCourse> {
        val courses = mutableListOf<BanquetCourse>()

        // 朋友聚会注重互动和分享
        // 前菜
        val appetizers = recipes
            .filter { it.cookingTime <= 15 }
            .shuffled()
            .take(2)

        // 主菜 - 选择容易分享的菜
        val mainCourses = recipes
            .filter { it.difficulty == DifficultyLevel.MEDIUM }
            .shuffled()
            .take(max(3, config.guestCount / 3))

        // 加上汤和甜品（如果有）

        appetizers.forEach { recipe ->
            courses.add(BanquetCourse(
                courseType = CourseType.APPETIZER,
                order = courses.size + 1,
                recipe = recipe,
                servings = config.guestCount,
                estimatedTime = recipe.cookingTime
            ))
        }

        mainCourses.forEach { recipe ->
            courses.add(BanquetCourse(
                courseType = CourseType.MAIN,
                order = courses.size + 1,
                recipe = recipe,
                servings = config.guestCount,
                estimatedTime = recipe.cookingTime
            ))
        }

        reorderCourses(courses)

        return courses
    }

    private fun generateCelebration(
        recipes: List<Recipe>,
        config: BanquetConfig
    ): List<BanquetCourse> {
        // 庆祝场合选择更精致的菜
        val courses = mutableListOf<BanquetCourse>()

        val fancyRecipes = recipes
            .filter { it.difficulty == DifficultyLevel.HARD || it.averageRating >= 4.0f }
            .sortedByDescending { it.averageRating }

        // 前菜
        fancyRecipes.take(1).forEach { recipe ->
            courses.add(BanquetCourse(
                courseType = CourseType.APPETIZER,
                order = courses.size + 1,
                recipe = recipe,
                servings = config.guestCount,
                estimatedTime = recipe.cookingTime
            ))
        }

        // 主菜
        fancyRecipes.drop(1).take(4).forEach { recipe ->
            courses.add(BanquetCourse(
                courseType = CourseType.MAIN,
                order = courses.size + 1,
                recipe = recipe,
                servings = config.guestCount,
                estimatedTime = recipe.cookingTime
            ))
        }

        reorderCourses(courses)

        return courses
    }

    private fun generateCasual(
        recipes: List<Recipe>,
        config: BanquetConfig
    ): List<BanquetCourse> {
        // 随意场合选择简单好吃的菜
        val courses = mutableListOf<BanquetCourse>()

        val simpleRecipes = recipes
            .filter { it.difficulty == DifficultyLevel.EASY }
            .shuffled()
            .take(config.guestCount / 2 + 1)

        simpleRecipes.forEach { recipe ->
            courses.add(BanquetCourse(
                courseType = CourseType.MAIN,
                order = courses.size + 1,
                recipe = recipe,
                servings = config.guestCount,
                estimatedTime = recipe.cookingTime
            ))
        }

        return courses
    }

    private fun reorderCourses(courses: MutableList<BanquetCourse>) {
        // 按照推荐的顺序重新排列
        courses.sortBy { it.order }
    }

    private fun generateTimePlan(
        menu: List<BanquetCourse>,
        startTime: Int
    ): TimePlan {
        val tasks = mutableListOf<CookingTask>()
        var currentTime = startTime

        // 预处理阶段（提前1-2小时）
        val prepTime = startTime - 120
        menu.forEach { course ->
            if (course.recipe.prepTime != null) {
                tasks.add(CookingTask(
                    time = prepTime,
                    action = "准备${course.recipe.name}",
                    description = "洗菜、切菜、腌制",
                    course = course,
                    duration = course.recipe.prepTime ?: 30,
                    priority = TaskPriority.HIGH
                ))
            }
        }

        // 烹饪阶段
        // 按烹饪时间排序，先做耗时长的
        val sortedMenu = menu.sortedByDescending { it.estimatedTime }

        sortedMenu.forEach { course ->
            val recipe = course.recipe
            val startCooking = currentTime
            val endCooking = startCooking + recipe.cookingTime

            tasks.add(CookingTask(
                time = startCooking,
                action = "开始做${recipe.name}",
                description = "按照菜谱步骤操作",
                course = course,
                duration = recipe.cookingTime,
                priority = when (course.courseType) {
                    CourseType.MAIN -> TaskPriority.HIGH
                    CourseType.SOUP -> TaskPriority.MEDIUM
                    else -> TaskPriority.LOW
                }
            ))

            // 更新时间
            if (endCooking > currentTime) {
                currentTime = endCooking
            }
        }

        // 最后完成时间
        val servingTime = currentTime + 10 // 预留10分钟整理摆盘

        return TimePlan(
            prepTime = prepTime,
            startTime = startTime,
            servingTime = servingTime,
            totalDuration = servingTime - prepTime,
            tasks = tasks.sortedBy { it.time }
        )
    }

    private fun calculateOverallDifficulty(menu: List<BanquetCourse>): DifficultyLevel {
        val hardCount = menu.count { it.recipe.difficulty == DifficultyLevel.HARD }
        val mediumCount = menu.count { it.recipe.difficulty == DifficultyLevel.MEDIUM }

        return when {
            hardCount >= 2 -> DifficultyLevel.HARD
            mediumCount >= 2 -> DifficultyLevel.MEDIUM
            else -> DifficultyLevel.EASY
        }
    }

    // === 数据类 ===

    data class BanquetMenu(
        val config: BanquetConfig,
        val courses: List<BanquetCourse>,
        val timePlan: TimePlan
    )

    data class BanquetConfig(
        val occasion: BanquetOccasion,
        val guestCount: Int,                   // 人数
        val startTime: Int,                   // 开始时间（分钟，从0:00开始）
        val budget: Double? = null,          // 预算
        val dietaryRestrictions: List<String> = emptyList(), // 饮食限制
        val maxTimePerDish: Int? = null,      // 单道菜最大时间
        val preferredCuisines: List<String> = emptyList()
    )

    data class BanquetCourse(
        val courseType: CourseType,
        val order: Int,
        val recipe: Recipe,
        val servings: Int,
        val estimatedTime: Int,
        val mainIngredient: String = "肉",      // 简化
        val cookingMethod: String = "炒",       // 简化
        val flavorProfile: String = "咸鲜"       // 简化
    )

    enum class CourseType {
        APPETIZER,   // 前菜
        SOUP,        // 汤
        MAIN,        // 主菜
        SIDE,        // 配菜
        DESSERT      // 甜品
    }

    data class TimePlan(
        val prepTime: Int,                    // 开始准备时间
        val startTime: Int,                   // 开始烹饪时间
        val servingTime: Int,                 // 上菜时间
        val totalDuration: Int,               // 总时长（分钟）
        val tasks: List<CookingTask>
    )

    data class CookingTask(
        val time: Int,                        // 开始时间
        val action: String,                   // 行动描述
        val description: String,              // 详细说明
        val course: BanquetCourse,
        val duration: Int,                    // 持续时间
        val priority: TaskPriority
    )

    enum class TaskPriority {
        HIGH,    // 重要（会影响其他任务）
        MEDIUM,  // 中等
        LOW      // 可延后
    }

    data class MenuAnalysis(
        val mainIngredients: List<String>,
        val cookingMethods: List<String>,
        val flavors: List<String>,
        val totalEstimatedTime: Int,
        val difficultyLevel: DifficultyLevel,
        val issues: List<String>,
        val suggestions: List<String>
    )

    enum class BanquetOccasion {
        FAMILY_DINNER,       // 家庭聚餐
        FRIENDS_GATHERING,   // 朋友聚会
        CELEBRATION,         // 庆祝场合
        CASUAL               // 随意场合
    }
}
