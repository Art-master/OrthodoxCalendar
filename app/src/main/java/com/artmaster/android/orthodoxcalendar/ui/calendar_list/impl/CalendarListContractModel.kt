package com.artmaster.android.orthodoxcalendar.ui.calendar_list.impl

import com.artmaster.android.orthodoxcalendar.domain.Holiday

interface CalendarListContractModel {
    fun getDataSequence(start: Int, size: Int, year: Int): List<Holiday>
}