package com.example.projekt_zespolowy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DwarfDao {
    @Insert
    fun insert(dwarf: Dwarfs)

    @Query("UPDATE dwarfs SET Count = Count + 1 WHERE Name = :name")
    fun updateDwarfCount(name: String)

    @Query("SELECT * FROM dwarfs WHERE Name = :name")
    fun getDwarfIdByName(name: String): Dwarfs

    @Query("SELECT * FROM dwarfs ORDER BY Name ASC")
    fun getAllByName(): List<Dwarfs>

    @Query("SELECT * FROM dwarfs ORDER BY DateStamp DESC")
    fun getAllByDate(): List<Dwarfs>
}