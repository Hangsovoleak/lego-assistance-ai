package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LegoDao {
    // Bricks DAO
    @Query("SELECT * FROM lego_bricks ORDER BY category ASC, size ASC, colorName ASC")
    fun getAllBricks(): Flow<List<LegoBrickEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrick(brick: LegoBrickEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBricks(bricks: List<LegoBrickEntity>)

    @Update
    suspend fun updateBrick(brick: LegoBrickEntity)

    @Delete
    suspend fun deleteBrick(brick: LegoBrickEntity)

    @Query("DELETE FROM lego_bricks")
    suspend fun clearAllBricks()

    @Query("SELECT * FROM lego_bricks WHERE id = :id LIMIT 1")
    suspend fun getBrickById(id: String): LegoBrickEntity?

    // Custom creations DAO
    @Query("SELECT * FROM custom_creations ORDER BY timestamp DESC")
    fun getAllCustomCreations(): Flow<List<CustomCreationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCreation(creation: CustomCreationEntity)

    @Delete
    suspend fun deleteCustomCreation(creation: CustomCreationEntity)

    // Saved Active Builds
    @Query("SELECT * FROM saved_builds")
    fun getSavedBuilds(): Flow<List<SavedBuildEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBuildProgress(build: SavedBuildEntity)

    @Query("SELECT * FROM saved_builds WHERE modelId = :modelId LIMIT 1")
    suspend fun getBuildProgress(modelId: String): SavedBuildEntity?

    @Query("DELETE FROM saved_builds WHERE modelId = :modelId")
    suspend fun deleteBuildProgress(modelId: String)
}
