package com.dev.jakob.watermanager.data.source

import androidx.room.*
import com.dev.jakob.watermanager.data.model.Container
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) für die [Container]-Entität.
 * Bietet Methoden für den Datenbankzugriff auf Container-Objekte.
 */
@Dao
interface ContainerDao {
    /**
     * Ruft alle Container aus der Datenbank ab, sortiert nach Namen.
     *
     * @return Ein [Flow] einer Liste von [Container]-Objekten.
     */
    @Query("SELECT * FROM containers ORDER BY name ASC")
    fun getAllContainers(): Flow<List<Container>>

    /**
     * Fügt einen Container in die Datenbank ein oder aktualisiert ihn, wenn ein Konflikt auftritt.
     *
     * @param container Der einzufügende oder zu aktualisierende [Container].
     * @return Die Row-ID des eingefügten oder aktualisierten Containers.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContainer(container: Container): Long

    /**
     * Fügt eine Liste von Containern in die Datenbank ein oder aktualisiert sie, wenn Konflikte auftreten.
     *
     * @param containers Die Liste der einzufügenden oder zu aktualisierenden [Container].
     * @return Eine Liste der Row-IDs der eingefügten oder aktualisierten Container.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContainers(containers: List<Container>): List<Long>

    /**
     * Löscht einen Container aus der Datenbank.
     *
     * @param container Der zu löschende [Container].
     */
    @Delete
    suspend fun deleteContainer(container: Container)

    /**
     * Löscht alle Container aus der Datenbank.
     */
    @Query("DELETE FROM containers")
    suspend fun deleteAllContainers()
}
