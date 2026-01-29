package com.homepantry.data.search

import com.homepantry.data.entity.DifficultyLevel

data class RecipeSearchFilters(
    val query: String = "",
    val categories: Set<String> = emptySet(),
    val difficulties: Set<DifficultyLevel> = emptySet(),
    val maxCookingTime: Int? = null,
    val minServings: Int? = null,
    val maxServings: Int? = null,
    val onlyFavorites: Boolean = false,
    val availableIngredientsOnly: Boolean = false,
    val tags: Set<String> = emptySet()
) {
    val hasActiveFilters: Boolean
        get() = categories.isNotEmpty() ||
                difficulties.isNotEmpty() ||
                maxCookingTime != null ||
                minServings != null ||
                maxServings != null ||
                onlyFavorites ||
                availableIngredientsOnly ||
                tags.isNotEmpty()

    fun toDisplayString(): String {
        val parts = mutableListOf<String>()

        if (query.isNotBlank()) parts.add("搜索: $query")
        if (categories.isNotEmpty()) parts.add("分类: ${categories.joinToString()}")
        if (difficulties.isNotEmpty()) {
            val difficultyNames = difficulties.map {
                when (it) {
                    DifficultyLevel.EASY -> "简单"
                    DifficultyLevel.MEDIUM -> "中等"
                    DifficultyLevel.HARD -> "困难"
                }
            }
            parts.add("难度: ${difficultyNames.joinToString()}")
        }
        if (maxCookingTime != null) parts.add("时间: ≤${maxCookingTime}分钟")
        if (onlyFavorites) parts.add("仅收藏")
        if (availableIngredientsOnly) parts.add("现有食材")

        return if (parts.isEmpty()) "全部菜谱" else parts.joinToString(" | ")
    }

    fun copyWith(
        query: String = this.query,
        categories: Set<String> = this.categories,
        difficulties: Set<DifficultyLevel> = this.difficulties,
        maxCookingTime: Int? = this.maxCookingTime,
        minServings: Int? = this.minServings,
        maxServings: Int? = this.maxServings,
        onlyFavorites: Boolean = this.onlyFavorites,
        availableIngredientsOnly: Boolean = this.availableIngredientsOnly,
        tags: Set<String> = this.tags
    ): RecipeSearchFilters {
        return RecipeSearchFilters(
            query = query,
            categories = categories,
            difficulties = difficulties,
            maxCookingTime = maxCookingTime,
            minServings = minServings,
            maxServings = maxServings,
            onlyFavorites = onlyFavorites,
            availableIngredientsOnly = availableIngredientsOnly,
            tags = tags
        )
    }
}

data class RecipeSortOption(
    val id: String,
    val displayName: String,
    val description: String
) {
    companion object {
        val NEWEST_FIRST = RecipeSortOption("newest", "最新", "最新添加的菜谱")
        val OLDEST_FIRST = RecipeSortOption("oldest", "最早", "最早添加的菜谱")
        val COOKING_TIME_ASC = RecipeSortOption("quickest", "最快", "烹饪时间最短")
        val COOKING_TIME_DESC = RecipeSortOption("slowest", "最慢", "烹饪时间最长")
        val DIFFICULTY_ASC = RecipeSortOption("easiest", "最简单", "难度从低到高")
        val DIFFICULTY_DESC = RecipeSortOption("hardest", "最困难", "难度从高到低")
        val NAME_ASC = RecipeSortOption("name_az", "名称 A-Z", "按名称排序")
        val NAME_DESC = RecipeSortOption("name_za", "名称 Z-A", "按名称倒序")

        val ALL = listOf(
            NEWEST_FIRST,
            OLDEST_FIRST,
            COOKING_TIME_ASC,
            COOKING_TIME_DESC,
            DIFFICULTY_ASC,
            DIFFICULTY_DESC,
            NAME_ASC,
            NAME_DESC
        )
    }
}
