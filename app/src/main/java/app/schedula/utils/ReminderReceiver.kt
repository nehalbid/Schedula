package app.schedula.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val appointmentId = intent.getStringExtra("appointmentId") ?: return
        val type = intent.getStringExtra("type") ?: return

        val title: String
        val message: String

        when (type) {
            "1_HOUR" -> {
                title = "Appointment in 1 Hour"
                message = "You have an appointment coming up in 60 minutes."
            }
            "10_MIN" -> {
                title = "Arrived at Hospital?"
                message = "Your appointment is in 10 minutes. Please tap 'I Have Arrived' if you are at the location."
            }
            else -> {
                title = "Appointment Reminder"
                message = "You have an upcoming appointment."
            }
        }

        NotificationHelper.showNotification(
            context,
            appointmentId.hashCode(),
            title,
            message
        )
    }
}
