package com.example.projekt_zespolowy.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DwarfDao {
    @Insert
    fun insert(dwarf: Dwarfs)

    @Query("UPDATE dwarfs SET count = count + 1 WHERE name = :name")
    fun updateDwarfCount(name: String)

    @Query("SELECT * FROM dwarfs WHERE name = :name")
    fun getDwarfByName(name: String): Dwarfs

    @Query("SELECT * FROM dwarfs ORDER BY name ASC")
    fun getAllByName(): Flow<List<Dwarfs>>

    @Query("SELECT * FROM dwarfs ORDER BY date_stamp DESC")
    fun getAllByDate(): List<Dwarfs>
}