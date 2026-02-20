package com.homepantry.data.validation

/**
 * 菜谱筛选验证器
 */
object RecipeFilterValidator {

    private const val MIN_COOKING_TIME = 0
    private const val MAX_COOKING_TIME = 1440 // 24 小时（分钟）
    private const val MAX_INCLUDED_INGREDIENTS = 20
    private const val MAX_EXCLUDED_INGREDIENTS = 20

    /**
     * 验证烹饪时间范围
     */
    fun validateCookingTimeRange(min: Int?, max: Int?): ValidationResult {
        if (min == null && max == null) {
            return ValidationResult.Success
        }

        if (min != null) {
            if (min < MIN_COOKING_TIME) {
                return ValidationResult.Error("最短时间不能小于 $MIN_COOKING_TIME 分钟")
            }
            if (min > MAX_COOKING_TIME) {
                return ValidationResult.Error("最短时间不能超过 $MAX_COOKING_TIME 分钟")
            }
        }

        if (max != null) {
            if (max < MIN_COOKING_TIME) {
                return ValidationResult.Error("最长时间不能小于 $MIN_COOKING_TIME 分钟")
            }
            if (max > MAX_COOKING_TIME) {
                return ValidationResult.Error("最长时间不能超过 $MAX_COOKING_TIME 分钟")
            }
        }

        if (min != null && max != null && min > max) {
            return ValidationResult.Error("最短时间不能大于最长时间")
        }

        return ValidationResult.Success
    }

    /**
     * 验证难度范围
     */
    fun validateDifficultyRange(min: com.homepantry.data.entity.DifficultyLevel?, max: com.homepantry.data.entity.DifficultyLevel?): ValidationResult {
        if (min == null && max == null) {
            return ValidationResult.Success
        }

        if (min != null && max != null && min.ordinal > max.ordinal) {
            return ValidationResult.Error("最低难度不能高于最高难度")
        }

        return ValidationResult.Success
    }

    /**
     * 验证食材列表
     */
    fun validateIngredientList(ingredients: List<String>?): ValidationResult {
        if (ingredients == null) {
            return ValidationResult.Success
        }

        if (ingredients.size > MAX_INCLUDED_INGREDIENTS) {
            return ValidationResult.Error("包含食材数量不能超过 $MAX_INCLUDED_INGREDIENTS 个")
        }

        return ValidationResult.Success
    }

    /**
     * 验证排除食材列表
     */
    fun validateExcludedIngredientList(ingredients: List<String>?): ValidationResult {
        if (ingredients == null) {
            return ValidationResult.Success
        }

        if (ingredients.size > MAX_EXCLUDED_INGREDIENTS) {
            return ValidationResult.Error("排除食材数量不能超过 $MAX_EXCLUDED_INGREDIENTS 个")
        }

        return ValidationResult.Success
    }

    /**
     * 验证分类列表
     */
    fun validateCategoryList(categories: List<String>?): ValidationResult {
        if (categories == null) {
            return ValidationResult.Success
        }

        if (categories.isEmpty()) {
            return ValidationResult.Success
        }

        return ValidationResult.Success
    }

    /**
     * 验证筛选条件
     */
    fun validateCriteria(criteria: com.homepantry.data.entity.RecipeFilterCriteria): ValidationResult {
        return validateCookingTimeRange(criteria.cookingTimeMin, criteria.cookingTimeMax)
            .flatMap {
                validateDifficultyRange(criteria.difficultyMin, criteria.difficultyMax)
            }
            .flatMap {
                validateIngredientList(criteria.includedIngredients.toList())
            }
            .flatMap {
                validateExcludedIngredientList(criteria.excludedIngredients.toList())
            }
            .flatMap {
                validateCategoryList(criteria.categoryIds.toList())
            }
    }

    /**
     * 验证筛选条件是否为空
     */
    fun validateIsEmpty(criteria: com.homepantry.data.entity.RecipeFilterCriteria): ValidationResult {
        return if (criteria.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("筛选条件不为空")
        }
    }
}
