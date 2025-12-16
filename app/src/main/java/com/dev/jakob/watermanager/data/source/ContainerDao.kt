package com.dev.jakob.watermanager.data.source

import androidx.room.*
import com.dev.jakob.watermanager.data.model.Container
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    @Query("SELECT * FROM containers ORDER BY name ASC")
    fun getAllContainers(): Flow<List<Container>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContainer(container: Container)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContainers(containers: List<Container>)

    @Delete
    suspend fun deleteContainer(container: Container)

    @Query("DELETE FROM containers")
    suspend fun deleteAllContainers()
}
