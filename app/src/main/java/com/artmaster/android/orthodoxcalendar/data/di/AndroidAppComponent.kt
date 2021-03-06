package com.artmaster.android.orthodoxcalendar.data.di

import android.content.Context
import com.artmaster.android.orthodoxcalendar.App
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AndroidAppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AndroidAppComponent
    }

    fun inject(app: App)
}