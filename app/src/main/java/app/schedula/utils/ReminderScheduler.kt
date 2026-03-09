package app.schedula.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object ReminderScheduler {

    fun scheduleReminders(
        context: Context,
        appointmentId: String,
        appointmentDate: Date
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1 Hour Before
        val oneHourBefore = appointmentDate.time - (60 * 60 * 1000)

        // 10 Minutes Before (Updated from 5 to 10 for "I Have Arrived" prompt)
        val tenMinBefore = appointmentDate.time - (10 * 60 * 1000)

        if (oneHourBefore > System.currentTimeMillis()) {
            scheduleExactAlarm(
                context,
                alarmManager,
                appointmentId.hashCode(),
                appointmentId,
                oneHourBefore,
                "1_HOUR"
            )
        }

        if (tenMinBefore > System.currentTimeMillis()) {
            scheduleExactAlarm(
                context,
                alarmManager,
                appointmentId.hashCode() + 1,
                appointmentId,
                tenMinBefore,
                "10_MIN"
            )
        }
    }

    private fun scheduleExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        appointmentId: String,
        triggerTime: Long,
        type: String
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("appointmentId", appointmentId)
            putExtra("type", type)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}
