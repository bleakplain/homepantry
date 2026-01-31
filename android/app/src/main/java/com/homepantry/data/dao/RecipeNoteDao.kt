package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.RecipeNote
import kotlinx.coroutines.flow.Flow

/**
 * 菜谱笔记 DAO
 */
@Dao
interface RecipeNoteDao {
    @Query("SELECT * FROM recipe_notes WHERE recipeId = :recipeId ORDER BY cookingDate DESC")
    fun getNotesByRecipe(recipeId: String): Flow<List<RecipeNote>>

    @Query("SELECT * FROM recipe_notes WHERE recipeId = :recipeId ORDER BY cookingDate DESC LIMIT :limit")
    suspend fun getRecentNotesByRecipe(recipeId: String, limit: Int = 10): List<RecipeNote>

    @Query("SELECT * FROM recipe_notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): RecipeNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: RecipeNote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<RecipeNote>)

    @Update
    suspend fun updateNote(note: RecipeNote)

    @Delete
    suspend fun deleteNote(note: RecipeNote)

    @Query("DELETE FROM recipe_notes WHERE recipeId = :recipeId")
    suspend fun deleteNotesByRecipe(recipeId: String)

    @Query("SELECT AVG(rating) FROM recipe_notes WHERE recipeId = :recipeId")
    suspend fun getAverageRating(recipeId: String): Double?

    @Query("SELECT COUNT(*) FROM recipe_notes WHERE recipeId = :recipeId")
    suspend fun getNoteCount(recipeId: String): Int

    // 获取用户所有笔记（按时间倒序）
    @Query("SELECT * FROM recipe_notes ORDER BY cookingDate DESC LIMIT :limit")
    fun getAllNotes(limit: Int = 50): Flow<List<RecipeNote>>

    // 获取高分笔记
    @Query("SELECT * FROM recipe_notes WHERE rating >= :minRating ORDER BY rating DESC, cookingDate DESC")
    fun getTopRatedNotes(minRating: Int = 4): Flow<List<RecipeNote>>
}
