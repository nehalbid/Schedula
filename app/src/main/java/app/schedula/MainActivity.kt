package app.schedula

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.schedula.ui.navigation.AppNavigation
import app.schedula.ui.theme.SchedulaTheme
import app.schedula.workers.AppointmentStatusWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 Register Appointment Status Worker (15 min interval)
        val workRequest =
            PeriodicWorkRequestBuilder<AppointmentStatusWorker>(
                15, TimeUnit.MINUTES
            ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "AppointmentStatusWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

        setContent {
            SchedulaTheme {
                AppNavigation()
            }
        }
    }
}