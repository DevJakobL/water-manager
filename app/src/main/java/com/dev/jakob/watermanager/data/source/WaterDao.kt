package com.dev.jakob.watermanager.data.source

import androidx.room.*
import com.dev.jakob.watermanager.data.model.Water
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_intake ORDER BY timestamp DESC")
    fun getAllWaterIntake(): Flow<List<Water>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWater(water: Water)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWater(waterList: List<Water>)

    @Delete
    suspend fun deleteWater(water: Water)

    @Query("DELETE FROM water_intake")
    suspend fun deleteAllWater()
}
