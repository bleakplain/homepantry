package com.homepantry.data.validation

/**
 * 验证结果
 */
sealed class ValidationResult {
    data class Success(val message: String = "验证成功") : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

/**
 * 收藏夹验证器
 */
object FolderValidator {

    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 20
    private val NAME_PATTERN = Regex("^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$")

    /**
     * 验证文件夹名称
     */
    fun validateName(name: String): ValidationResult {
        return when {
            name.isEmpty() -> ValidationResult.Error("名称不能为空")
            name.length < MIN_NAME_LENGTH -> ValidationResult.Error("名称不能少于 $MIN_NAME_LENGTH 个字符")
            name.length > MAX_NAME_LENGTH -> ValidationResult.Error("名称不能超过 $MAX_NAME_LENGTH 个字符")
            !name.matches(NAME_PATTERN) -> ValidationResult.Error("名称只能包含中文、字母、数字和空格")
            else -> ValidationResult.Success()
        }
    }

    /**
     * 验证文件夹颜色
     */
    fun validateColor(color: String?): ValidationResult {
        return if (color == null) {
            ValidationResult.Success()
        } else {
            val hexColorPattern = Regex("^#[0-9A-Fa-f]{6}$")
            if (color.matches(hexColorPattern)) {
                ValidationResult.Success()
            } else {
                ValidationResult.Error("颜色格式不正确，应为 #RRGGBB")
            }
        }
    }

    /**
     * 验证排序号
     */
    fun validateSortOrder(sortOrder: Int): ValidationResult {
        return if (sortOrder < 0) {
            ValidationResult.Error("排序号不能为负数")
        } else if (sortOrder > 9999) {
            ValidationResult.Error("排序号不能超过 9999")
        } else {
            ValidationResult.Success()
        }
    }

    /**
     * 验证文件夹图标
     */
    fun validateIcon(icon: String?): ValidationResult {
        return if (icon == null) {
            ValidationResult.Success()
        } else if (icon.length > 50) {
            ValidationResult.Error("图标名称不能超过 50 个字符")
        } else {
            ValidationResult.Success()
        }
    }

    /**
     * 验证文件夹是否为系统文件夹
     */
    fun validateIsSystem(isSystem: Boolean): ValidationResult {
        return ValidationResult.Success()
    }
}
