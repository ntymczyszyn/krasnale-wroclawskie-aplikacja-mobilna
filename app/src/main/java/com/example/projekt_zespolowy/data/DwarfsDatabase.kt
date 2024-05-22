package com.example.projekt_zespolowy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [Dwarfs::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(value = [DateConverter::class])
abstract class DwarfsDatabase : RoomDatabase() {
    abstract fun dwarfs(): DwarfDao

//    companion object {
//        @Volatile
//        var INSTANCE: DwarfsDatabase? = null
//
//        fun getDatabase(context: Context): DwarfsDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context,
//                    DwarfsDatabase::class.java,
//                    "dwarfs_database"
//                ).build()
//                INSTANCE = instance
//                return instance
//            }
//        }
//    }
}