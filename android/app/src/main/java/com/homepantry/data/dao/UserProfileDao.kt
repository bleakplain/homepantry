package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * 用户偏好 DAO
 */
@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfileById(id: String = "default"): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    fun getProfileFlow(id: String = "default"): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("UPDATE user_profiles SET updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(id: String = "default", timestamp: Long = System.currentTimeMillis())

    // 更新特定字段
    @Query("UPDATE user_profiles SET preferredCuisines = :cuisines WHERE id = :id")
    suspend fun updatePreferredCuisines(id: String, cuisines: String)

    @Query("UPDATE user_profiles SET spiceLevel = :level WHERE id = :id")
    suspend fun updateSpiceLevel(id: String, level: String)

    @Query("UPDATE user_profiles SET dislikedIngredients = :ingredients WHERE id = :id")
    suspend fun updateDislikedIngredients(id: String, ingredients: String)

    @Query("UPDATE user_profiles SET dietaryRestrictions = :restrictions WHERE id = :id")
    suspend fun updateDietaryRestrictions(id: String, restrictions: String)

    @Query("UPDATE user_profiles SET allergies = :allergies WHERE id = :id")
    suspend fun updateAllergies(id: String, allergies: String)

    @Query("UPDATE user_profiles SET healthGoal = :goal WHERE id = :id")
    suspend fun updateHealthGoal(id: String, goal: String?)

    @Query("UPDATE user_profiles SET theme = :theme WHERE id = :id")
    suspend fun updateTheme(id: String, theme: String)

    @Query("UPDATE user_profiles SET dailyRecommendationTime = :time WHERE id = :id")
    suspend fun updateDailyRecommendationTime(id: String, time: String)
}
