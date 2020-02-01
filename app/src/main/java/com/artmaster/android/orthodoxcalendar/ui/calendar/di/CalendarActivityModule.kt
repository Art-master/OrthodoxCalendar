package com.artmaster.android.orthodoxcalendar.ui.calendar.di


import com.artmaster.android.orthodoxcalendar.data.repository.DataProvider
import com.artmaster.android.orthodoxcalendar.ui.calendar.fragments.FragmentAppInfo
import com.artmaster.android.orthodoxcalendar.ui.calendar.fragments.FragmentSettingsApp
import com.artmaster.android.orthodoxcalendar.ui.calendar.fragments.ListHolidayPager
import com.artmaster.android.orthodoxcalendar.ui.calendar.fragments.impl.AppInfoView
import com.artmaster.android.orthodoxcalendar.ui.calendar.fragments.impl.AppSettingView
import com.artmaster.android.orthodoxcalendar.ui.calendar.impl.CalendarListContractModel
import com.artmaster.android.orthodoxcalendar.ui.calendar.impl.ListViewContract
import com.artmaster.android.orthodoxcalendar.ui.tile_pager.impl.ContractTileView
import com.artmaster.android.orthodoxcalendar.ui.tile_pager.mvp.CalendarTileFragment
import dagger.Module
import dagger.Provides

@Module(includes = [FragmentInjectorModule::class])
class CalendarActivityModule {
    @CalendarActivityScope
    @Provides
    fun provideModel(): CalendarListContractModel {
        return DataProvider()
    }

    @CalendarActivityScope
    @Provides
    fun provideListHolidaysFragment(): ListViewContract.ViewListPager {
        return ListHolidayPager()
    }

    @CalendarActivityScope
    @Provides
    fun provideTileHolidaysFragment(): ContractTileView {
        return CalendarTileFragment()
    }

    @CalendarActivityScope
    @Provides
    fun provideAppInfoFragment(): AppInfoView {
        return FragmentAppInfo()
    }

    @CalendarActivityScope
    @Provides
    fun provideAppSettingsFragment(): AppSettingView {
        return FragmentSettingsApp()
    }
}