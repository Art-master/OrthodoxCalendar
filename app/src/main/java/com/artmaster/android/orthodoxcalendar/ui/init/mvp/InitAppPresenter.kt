package com.artmaster.android.orthodoxcalendar.ui.init.mvp

import com.artmaster.android.orthodoxcalendar.common.Constants
import com.artmaster.android.orthodoxcalendar.common.Settings
import com.artmaster.android.orthodoxcalendar.common.Settings.Name.*
import com.artmaster.android.orthodoxcalendar.domain.Holiday
import com.artmaster.android.orthodoxcalendar.impl.AppPreferences
import com.artmaster.android.orthodoxcalendar.impl.mvp.AbstractAppPresenter
import kotlinx.coroutines.*

class InitAppPresenter(
        private var appPreferences: AppPreferences,
        private val model: InitAppContract.Model) :
        AbstractAppPresenter<InitAppContract.View>(), InitAppContract.Presenter {

    private val isOffAnimation = appPreferences.get(OFF_START_ANIMATION)

    override fun viewIsReady() {
        val timeAnim = getLoadingAnimTime()
        if (isShowStartAnimation()) getView().showLoadingScreen(timeAnim)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                getData()
                getView().initNotifications()
                delay(timeAnim * 2)
                withContext(Dispatchers.Main) {
                    getView().nextScreen()
                }
            }
        }
    }

    private fun isShowStartAnimation(): Boolean {
        return isOffAnimation == Settings.FALSE
    }

    private fun getLoadingAnimTime(): Long {
        if (isShowStartAnimation().not()) return 0
        var time = Constants.LOADING_ANIMATION_DURATION.toLong()
        val isSpeedUpSettingsEnabled = appPreferences.get(SPEED_UP_START_ANIMATION)
        if (isSpeedUpSettingsEnabled == Settings.TRUE) {
            time = Constants.LOADING_ANIMATION_SPEED_UP.toLong()
        }
        return time
    }

    private fun getData(): List<Holiday> {
        var data: List<Holiday> = emptyList()
        if (isAppFirstLoad()) {
            data = model.getDataFromFile()
            model.fillDatabase(data)
            appPreferences.set(FIRST_LOAD_APP, Settings.TRUE)
        }
        return data
    }

    private fun isAppFirstLoad(): Boolean {
        val preference = appPreferences.get(FIRST_LOAD_APP)
        return preference == Settings.FALSE
    }
}