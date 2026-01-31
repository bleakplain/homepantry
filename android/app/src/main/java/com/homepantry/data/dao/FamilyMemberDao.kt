package com.homepantry.data.dao

import androidx.room.*
import com.homepantry.data.entity.FamilyMember
import kotlinx.coroutines.flow.Flow

/**
 * 家庭成员 DAO
 */
@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY createdAt ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberById(id: String): FamilyMember?

    @Query("SELECT * FROM family_members WHERE relationship = :relationship")
    fun getMembersByRelationship(relationship: String): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<FamilyMember>)

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Delete
    suspend fun deleteMember(member: FamilyMember)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteMemberById(id: String)

    @Query("DELETE FROM family_members")
    suspend fun deleteAllMembers()

    @Query("SELECT COUNT(*) FROM family_members")
    suspend fun getMemberCount(): Int
}
