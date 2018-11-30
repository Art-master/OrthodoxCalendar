package com.artmaster.android.orthodoxcalendar.data.repository

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.artmaster.android.orthodoxcalendar.domain.HolidayEntity

@Dao
abstract class HolidayDao {
    @Insert
    abstract fun insertHoliday(holiday: HolidayEntity): Long

    @Insert
    abstract fun insertAllHolidays(holidays: List<HolidayEntity>): List<Long>

    @Query("SELECT * FROM holidays")
    abstract fun getAll(): List<HolidayEntity>

    @Query("SELECT * FROM holidays WHERE id = :id")
    abstract fun getForId(id: Long): HolidayEntity

    @Query("SELECT * FROM holidays WHERE id >= :initialId LIMIT :loadSize")
    abstract fun getSequence(initialId: Int, loadSize: Int): List<HolidayEntity>

    @Query("DELETE FROM holidays")
    abstract fun deleteTable()
}
