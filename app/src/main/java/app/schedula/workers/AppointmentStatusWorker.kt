package app.schedula.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.schedula.data.model.AppointmentStatus
import app.schedula.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await
import java.util.Date

class AppointmentStatusWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val firebaseService = FirebaseService()

    override suspend fun doWork(): Result {
        val firestore = firebaseService.firestore
        val now = Date()

        try {
            val snapshot = firestore.collection("appointments").get().await()

            snapshot.documents.forEach { doc ->

                val status = doc.getString("status") ?: return@forEach
                val appointmentDate = doc.getDate("appointmentDate") ?: return@forEach

                // CHECKED_IN → ONGOING
                if (status == AppointmentStatus.CHECKED_IN.name &&
                    now.after(appointmentDate)
                ) {
                    doc.reference.update(
                        mapOf(
                            "status" to AppointmentStatus.ONGOING.name,
                            "startedAt" to now
                        )
                    )
                }

                // ONGOING → COMPLETED (after 30 min)
                if (status == AppointmentStatus.ONGOING.name) {
                    val startedAt = doc.getDate("startedAt")
                    if (startedAt != null) {
                        val thirtyMinLater =
                            Date(startedAt.time + 30 * 60 * 1000)

                        if (now.after(thirtyMinLater)) {
                            doc.reference.update(
                                mapOf(
                                    "status" to AppointmentStatus.COMPLETED.name,
                                    "completedAt" to now
                                )
                            )
                        }
                    }
                }

                // UPCOMING → NO_SHOW (STRICT 5 min grace)
                if (status == AppointmentStatus.UPCOMING.name) {
                    val fiveMinAfter =
                        Date(appointmentDate.time + 5 * 60 * 1000)

                    if (now.after(fiveMinAfter)) {
                        // 1. Mark Appointment as NO_SHOW
                        doc.reference.update(
                            "status",
                            AppointmentStatus.NO_SHOW.name
                        )

                        // 2. Release the Doctor's Slot
                        val doctorId = doc.getString("doctorId") ?: ""
                        val slotId = doc.getString("slotId") ?: ""

                        if (doctorId.isNotEmpty() && slotId.isNotEmpty()) {
                            firestore.collection("doctors")
                                .document(doctorId)
                                .collection("slots")
                                .document(slotId)
                                .update("isBooked", false)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("StatusWorker", "Worker failed: ${e.message}")
            return Result.retry()
        }

        return Result.success()
    }
}
