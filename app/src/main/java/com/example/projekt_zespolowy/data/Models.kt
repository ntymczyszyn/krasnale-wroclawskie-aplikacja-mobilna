package com.example.projekt_zespolowy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "dwarfs")
data class Dwarfs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val date_stamp: Date,
    val count: Int
)