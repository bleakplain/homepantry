package com.homepantry.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.homepantry.data.constants.Constants

/**
 * 收藏夹实体
 */
@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 系统默认收藏夹
         */
        val DEFAULT_FOLDER = Folder(
            id = "default",
            name = "我的收藏",
            icon = "star",
            color = Constants.Colors.DEFAULT_FOLDER,
            sortOrder = 0,
            isSystem = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
