package com.artmaster.android.orthodoxcalendar.presentation.init.mvp

import com.artmaster.android.orthodoxcalendar.data.repository.CalendarPreferences
import com.artmaster.android.orthodoxcalendar.common.Message
import com.artmaster.android.orthodoxcalendar.common.Settings
import com.artmaster.android.orthodoxcalendar.common.Settings.Name.*
import com.artmaster.android.orthodoxcalendar.impl.mvp.AbstractAppPresenter
import com.artmaster.android.orthodoxcalendar.domain.HolidayEntity
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class InitAppPresenter(
        private var appPreferences: CalendarPreferences,
        private val model: InitAppContract.Model) :
        AbstractAppPresenter<InitAppContract.View>(), InitAppContract.Presenter {

    override fun viewIsReady() {
        Single.fromCallable { getData() }
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { getView().nextScreen() },
                        onError = {
                            it.printStackTrace()
                            getView().showErrorMassage(Message.ERROR.INIT_DATABASE)
                        })
    }

    override fun loadingScreenIsShowed() {
        getView().nextScreen()
    }

    private fun getData(): List<HolidayEntity> {
        var data: List<HolidayEntity> = emptyList()
        if (isAppFirstLoad()) {
            data = model.getDataFromFile()
            model.fillDatabase(data)
            appPreferences.set(FIRST_LOAD_APP, "FALSE")
        }
        return data
    }

    private fun isAppFirstLoad(): Boolean {
        val preference = appPreferences.get(FIRST_LOAD_APP)
        return preference.equals(Settings.EMPTY)
    }
}