package com.homepantry.data.repository

import com.homepantry.data.dao.CategoryDao
import com.homepantry.data.entity.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(categoryId: String): Category? =
        categoryDao.getCategoryById(categoryId)

    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)

    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
}
