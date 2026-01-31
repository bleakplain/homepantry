package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.NutritionInfo
import kotlinx.coroutines.flow.Flow

/**
 * 营养信息 DAO
 */
@Dao
interface NutritionInfoDao {
    @Query("SELECT * FROM nutrition_info WHERE recipeId = :recipeId")
    suspend fun getNutritionByRecipe(recipeId: String): NutritionInfo?

    @Query("SELECT * FROM nutrition_info WHERE recipeId = :recipeId")
    fun getNutritionByRecipeFlow(recipeId: String): Flow<NutritionInfo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: NutritionInfo)

    @Update
    suspend fun updateNutrition(nutrition: NutritionInfo)

    @Delete
    suspend fun deleteNutrition(nutrition: NutritionInfo)

    @Query("DELETE FROM nutrition_info WHERE recipeId = :recipeId")
    suspend fun deleteNutritionByRecipe(recipeId: String)
}
