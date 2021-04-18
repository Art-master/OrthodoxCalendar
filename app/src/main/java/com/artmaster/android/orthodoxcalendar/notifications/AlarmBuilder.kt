package com.artmaster.android.orthodoxcalendar.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.artmaster.android.orthodoxcalendar.App
import com.artmaster.android.orthodoxcalendar.common.Settings
import java.util.*

object AlarmBuilder {

    private val prefs = App.appComponent.getPreferences()

    fun build(context: Context) {
        if (isNotificationDisable()) return

        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr.setInexactRepeating(
                AlarmManager.RTC,
                buildCalendarByAppSettings().timeInMillis,
                AlarmManager.INTERVAL_DAY,
                createIntent(context)
        )
    }

    // Set the alarm to start at approximately by setting time
    private fun buildCalendarByAppSettings(): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, getHoursBySettings())
            //set(Calendar.SECOND, get(Calendar.SECOND) + 3) DEBUG
        }
    }

    private fun createIntent(context: Context): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }
    }

    private fun isNotificationDisable(): Boolean {
        val isEnableToday = prefs.get(Settings.Name.IS_ENABLE_NOTIFICATION_TODAY)
        val isEnableBefore = prefs.get(Settings.Name.IS_ENABLE_NOTIFICATION_TIME)
        return isEnableToday == Settings.FALSE || isEnableBefore == Settings.FALSE
    }

    private fun getHoursBySettings(): Int {
        return prefs.get(Settings.Name.HOURS_OF_NOTIFICATION).toInt()
    }

    /**
     * Enable boot receiver to persist alarms set for notifications across device reboots
     */
    fun enableBootReceiver(context: Context) {
        val receiver = ComponentName(context, AlarmReceiver::class.java)
        val pm = context.packageManager

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    /**
     * Disable boot receiver when user cancels/opt-out from notifications
     */
    fun disableBootReceiver(context: Context) {
        val receiver = ComponentName(context, AlarmReceiver::class.java)
        val pm = context.packageManager

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
    }
}