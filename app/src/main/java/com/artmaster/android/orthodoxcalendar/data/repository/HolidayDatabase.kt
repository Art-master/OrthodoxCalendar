package com.artmaster.android.orthodoxcalendar.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.artmaster.android.orthodoxcalendar.common.Constants.Companion.DATABASE_FILE_NAME
import com.artmaster.android.orthodoxcalendar.domain.Holiday
import com.artmaster.android.orthodoxcalendar.impl.AppDatabase

@Database(entities = [Holiday::class], version = 1)
abstract class HolidayDatabase : RoomDatabase(), AppDatabase {
    abstract fun holidayDao(): HolidayDao

    companion object : AppDatabase {
        var instance: HolidayDatabase? = null

        override fun get(context: Context): HolidayDatabase {
            synchronized(HolidayDatabase::class) {
                instance = Room.databaseBuilder(
                        context.applicationContext,
                        HolidayDatabase::class.java,
                        DATABASE_FILE_NAME)
                        .build()
                return instance!!
            }
        }

        override fun close() {
            if (instance?.isOpen == true) {
                instance?.close()
            }

            instance = null
        }
    }
}