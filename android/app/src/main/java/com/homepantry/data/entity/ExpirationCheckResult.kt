package com.homepantry.data.entity

import com.homepantry.data.constants.Constants

/**
 * 过期检查结果
 */
data class ExpirationCheckResult(
    val pantryItem: PantryItem,
    val expirationDate: Long,
    val daysUntilExpiration: Int,
    val status: ExpirationStatus
)

/**
 * 过期状态
 */
enum class ExpirationStatus {
    EXPIRED,       // 已过期
    EXPIRED_TODAY, // 今天过期
    EXPIRING_SOON, // 即将过期（N 天内）
    FRESH          // 新鲜（超过 N 天）
} {
    companion object {
        /**
         * 判断是否已过期
         */
        fun isExpired(item: PantryItem): Boolean {
            val today = System.currentTimeMillis()
            val msPerDay = 24 * 60 * 60 * 1000L
            return item.expiryDate < today
        }

        /**
         * 判断是否即将过期
         */
        fun isExpiringSoon(item: PantryItem, days: Int = Constants.Days.DEFAULT_REMINDER_DAYS): Boolean {
            val today = System.currentTimeMillis()
            val msPerDay = 24 * 60 * 60 * 1000L
            val expirationDate = today - (days * msPerDay)
            return item.expiryDate < expirationDate
        }

        /**
         * 获取距离过期的天数
         */
        fun getDaysUntilExpiration(item: PantryItem): Int {
            val today = System.currentTimeMillis()
            val msPerDay = 24 * 60 * 60 * 1000L
            return ((item.expiryDate - today) / msPerDay).toInt()
        }
    }
}

/**
 * 过期汇总
 */
data class ExpirationSummary(
    val totalCount: Int,
    val expiredCount: Int,
    val expiringSoonCount: Int,
    val freshCount: Int,
    val expiredItems: List<PantryItem>,
    val expiringSoonItems: List<PantryItem>
) {
    companion object {
        /**
         * 创建空的过期汇总
         */
        fun empty(): ExpirationSummary {
            return ExpirationSummary(
                totalCount = 0,
                expiredCount = 0,
                expiringSoonCount = 0,
                freshCount = 0,
                expiredItems = emptyList(),
                expiringSoonItems = emptyList()
            )
        }

        /**
         * 根据检查结果创建过期汇总
         */
        fun fromResults(
            results: List<ExpirationCheckResult>,
            expiringDays: Int = Constants.Days.DEFAULT_REMINDER_DAYS
        ): ExpirationSummary {
            return ExpirationSummary(
                totalCount = results.size,
                expiredCount = results.count { it.status == ExpirationStatus.EXPIRED || it.status == ExpirationStatus.EXPIRED_TODAY },
                expiringSoonCount = results.count { it.status == ExpirationStatus.EXPIRING_SOON },
                freshCount = results.count { it.status == ExpirationStatus.FRESH },
                expiredItems = results
                    .filter { it.status == ExpirationStatus.EXPIRED || it.status == ExpirationStatus.EXPIRED_TODAY }
                    .map { it.pantryItem },
                expiringSoonItems = results
                    .filter { it.status == ExpirationStatus.EXPIRING_SOON }
                    .map { it.pantryItem }
            )
        }
    }
}
