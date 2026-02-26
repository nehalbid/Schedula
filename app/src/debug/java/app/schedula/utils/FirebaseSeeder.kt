package app.schedula.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference

object FirebaseSeeder {

    private val db = FirebaseFirestore.getInstance()

    // --------------------------------------------------
    // 🔥 SEED DOCTORS + SLOTS (RUN ONLY ONCE)
    // --------------------------------------------------
    fun seedDoctorsWithSlots() {

        Log.d("SEED", "Seeding started")

        val doctors = listOf(
            mapOf(
                "id" to "doc1",
                "name" to "Dr. Rajesh Kumar",
                "specialty" to "Cardiologist",
                "experience" to 10,
                "rating" to 4.8,
                "gender" to "male",
                "fee" to 1800
            ),
            mapOf(
                "id" to "doc2",
                "name" to "Dr. Priya Sharma",
                "specialty" to "Dermatologist",
                "experience" to 8,
                "rating" to 4.7,
                "gender" to "female",
                "fee" to 1500
            ),
            mapOf(
                "id" to "doc3",
                "name" to "Dr. Amit Verma",
                "specialty" to "Pediatrician",
                "experience" to 12,
                "rating" to 4.9,
                "gender" to "male",
                "fee" to 1200
            ),
            mapOf(
                "id" to "doc4",
                "name" to "Dr. Sneha Patel",
                "specialty" to "Dermatologist",
                "experience" to 7,
                "rating" to 4.6,
                "gender" to "female",
                "fee" to 1400
            ),
            mapOf(
                "id" to "doc5",
                "name" to "Dr. Vikram Singh",
                "specialty" to "Neurologist",
                "experience" to 15,
                "rating" to 4.9,
                "gender" to "male",
                "fee" to 2000
            ),
            mapOf(
                "id" to "doc6",
                "name" to "Dr. Anjali Gupta",
                "specialty" to "Orthopedic",
                "experience" to 9,
                "rating" to 4.7,
                "gender" to "female",
                "fee" to 1600
            ),
            mapOf(
                "id" to "doc7",
                "name" to "Dr. Rohan Joshi",
                "specialty" to "Cardiologist",
                "experience" to 6,
                "rating" to 4.5,
                "gender" to "male",
                "fee" to 1700
            ),
            mapOf(
                "id" to "doc8",
                "name" to "Dr. Sunita Reddy",
                "specialty" to "Pediatrician",
                "experience" to 10,
                "rating" to 4.8,
                "gender" to "female",
                "fee" to 1300
            ),
             mapOf(
                "id" to "doc9",
                "name" to "Dr. Sameer Desai",
                "specialty" to "Neurologist",
                "experience" to 11,
                "rating" to 4.8,
                "gender" to "male",
                "fee" to 2200
            ),
            mapOf(
                "id" to "doc10",
                "name" to "Dr. Pooja Mehta",
                "specialty" to "Orthopedic",
                "experience" to 7,
                "rating" to 4.6,
                "gender" to "female",
                "fee" to 1500
            ),
            mapOf(
                "id" to "doc11",
                "name" to "Dr. Arjun Sharma",
                "specialty" to "Cardiologist",
                "experience" to 14,
                "rating" to 4.9,
                "gender" to "male",
                "fee" to 2500
            ),
            mapOf(
                "id" to "doc12",
                "name" to "Dr. Nisha Agarwal",
                "specialty" to "Dermatologist",
                "experience" to 10,
                "rating" to 4.8,
                "gender" to "female",
                "fee" to 1800
            ),
            mapOf(
                "id" to "doc13",
                "name" to "Dr. Rahul Khanna",
                "specialty" to "Pediatrician",
                "experience" to 15,
                "rating" to 5.0,
                "gender" to "male",
                "fee" to 1500
            ),
            mapOf(
                "id" to "doc14",
                "name" to "Dr. Sonam Kapoor",
                "specialty" to "Neurologist",
                "experience" to 12,
                "rating" to 4.9,
                "gender" to "female",
                "fee" to 2400
            ),
            mapOf(
                "id" to "doc15",
                "name" to "Dr. Varun Malhotra",
                "specialty" to "Orthopedic",
                "experience" to 11,
                "rating" to 4.8,
                "gender" to "male",
                "fee" to 1900
            ),
            mapOf(
                "id" to "doc16",
                "name" to "Dr. Ishita Shah",
                "specialty" to "Cardiologist",
                "experience" to 9,
                "rating" to 4.7,
                "gender" to "female",
                "fee" to 2000
            ),
            mapOf(
                "id" to "doc17",
                "name" to "Dr. Kunal Bhandari",
                "specialty" to "Dermatologist",
                "experience" to 6,
                "rating" to 4.5,
                "gender" to "male",
                "fee" to 1200
            ),
            mapOf(
                "id" to "doc18",
                "name" to "Dr. Riya Sen",
                "specialty" to "Pediatrician",
                "experience" to 8,
                "rating" to 4.7,
                "gender" to "female",
                "fee" to 1100
            ),
            mapOf(
                "id" to "doc19",
                "name" to "Dr. Harshvardhan Rathore",
                "specialty" to "Neurologist",
                "experience" to 18,
                "rating" to 5.0,
                "gender" to "male",
                "fee" to 3000
            ),
            mapOf(
                "id" to "doc20",
                "name" to "Dr. Aditi Rao",
                "specialty" to "Orthopedic",
                "experience" to 10,
                "rating" to 4.8,
                "gender" to "female",
                "fee" to 1700
            )
        )

        val weekdays = listOf(
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday"
        )

        val weekends = listOf("Saturday", "Sunday")

        doctors.forEach { doctor ->

            val doctorId = doctor["id"] as String
            val doctorRef = db.collection("doctors").document(doctorId)

            doctorRef.set(doctor)
                .addOnSuccessListener {

                    Log.d("SEED", "Doctor created: $doctorId")

                    val slotsRef = doctorRef.collection("slots")

                    // Weekdays: Morning + Evening
                    weekdays.forEach { day ->
                        generateSlots(slotsRef, day, 10, 14) // 10:00 AM - 1:00 PM
                        generateSlots(slotsRef, day, 16, 20) // 4:00 PM - 7:00 PM
                    }

                    // Weekends: Morning only
                    weekends.forEach { day ->
                        generateSlots(slotsRef, day, 10, 14) // 10:00 AM - 1:00 PM
                    }
                }
        }
    }

    // --------------------------------------------------
    // 🔹 SLOT GENERATOR (30 MIN INTERVAL)
    // --------------------------------------------------
    private fun generateSlots(
        slotsRef: CollectionReference,
        day: String,
        startHour: Int,
        endHour: Int
    ) {

        var hour = startHour
        var minute = 0

        while (hour < endHour) {

            if (!((hour == 13 ) || (hour == 19 ))) {
                val displayHour =
                    if (hour > 12) hour - 12
                    else if (hour == 0) 12
                    else hour

                val period = if (hour >= 12) "PM" else "AM"

                val time = String.format("%02d:%02d %s", displayHour, minute, period)

                val slotId = "${day}_${hour}_${minute}"

                slotsRef.document(slotId).set(
                    mapOf(
                        "day" to day,
                        "time" to time,
                        "isBooked" to false,
                        "bookedBy" to null
                    )
                )
            }

            minute += 30

            if (minute == 60) {
                minute = 0
                hour++
            }
        }
    }
}
