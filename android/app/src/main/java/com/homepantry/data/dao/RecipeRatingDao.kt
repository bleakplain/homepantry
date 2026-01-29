package com.homepantry.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.homepantry.data.entity.RecipeRating
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeRatingDao {
    @Query("SELECT * FROM recipe_ratings WHERE recipeId = :recipeId ORDER BY cookedDate DESC")
    fun getRatingsForRecipe(recipeId: String): Flow<List<RecipeRating>>

    @Query("SELECT * FROM recipe_ratings WHERE id = :ratingId")
    suspend fun getRatingById(ratingId: String): RecipeRating?

    @Query("SELECT AVG(rating) FROM recipe_ratings WHERE recipeId = :recipeId")
    suspend fun getAverageRatingForRecipe(recipeId: String): Float?

    @Query("SELECT COUNT(*) FROM recipe_ratings WHERE recipeId = :recipeId")
    suspend fun getRatingCountForRecipe(recipeId: String): Int

    @Query("SELECT * FROM recipe_ratings ORDER BY cookedDate DESC LIMIT :limit")
    fun getRecentRatings(limit: Int = 10): Flow<List<RecipeRating>>

    @Query("SELECT * FROM recipe_ratings WHERE wouldCookAgain = 1 ORDER BY cookedDate DESC")
    fun getWouldCookAgain(): Flow<List<RecipeRating>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RecipeRating)

    @Delete
    suspend fun deleteRating(rating: RecipeRating)
}
