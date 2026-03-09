# Schedula 🏥

**Schedula** is a comprehensive Doctor Appointment Management System built with **Kotlin** and **Jetpack Compose**. It features a three-tier role system (Patient, Doctor, Admin) and real-time synchronization using **Firebase Firestore**.

---

## 🌟 Key Features

### 👤 Patient App
- **Phone Auth:** Secure OTP-based login.
- **Doctor Discovery:** Browse doctors by specialty and experience.
- **Smart Booking:** Real-time slot selection with instant confirmation.
- **"I Have Arrived":** Check-in notification 10 minutes before appointments.
- **Digital Records:** Access prescriptions and consultation history anytime.
- **Family Management:** Book appointments for family members.

### 🩺 Doctor Panel
- **Queue Management:** Real-time dashboard to see arrived patients.
- **Dynamic Scheduling:** Set weekly hours and generate slots automatically.
- **Digital Prescriptions:** Create and send prescriptions instantly to patients.
- **Emergency Handling:** One-tap to request rescheduling for all today's appointments.
- **Auto-NoShow:** Automatic slot release for late patients (5-min grace period).

### ⚡ Admin Dashboard
- **System Overview:** Monitor total doctors, patients, and active appointments.
- **Doctor Management:** Add, edit, or disable doctor profiles.
- **Analytics:** View "Top Specialties" to understand clinic demand.
- **Appointment Audit:** View all system-wide bookings and their statuses.

---

## 🛠 Tech Stack
- **UI:** Jetpack Compose (100% Declarative UI)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase (Auth, Firestore)
- **Background Tasks:** WorkManager (for automated No-Show & Slot generation)
- **Notifications:** AlarmManager & NotificationManager for precise reminders.
- **Concurrency:** Kotlin Coroutines & Flow.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- A Firebase Project.

### Setup Instructions
1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/Schedula.git
   ```
2. **Add Firebase:**
   - Create a project at [Firebase Console](https://console.firebase.google.com/).
   - Add an Android App with package name `app.schedula`.
   - Download `google-services.json` and place it in the `app/` folder.
3. **Enable Firebase Services:**
   - **Authentication:** Enable Phone Provider.
   - **Firestore:** Create a database in "Start in production mode".
4. **Firestore Rules:**
   Copy the content of `firestore.rules` (included in the root) to your Firebase Console.
5. **Run the App:**
   Sync Gradle and run on a physical device or emulator.

---

## 🔒 Security Note
**DO NOT** commit your `google-services.json` or your release `keystore` to any public repository. These are excluded in the default `.gitignore`.

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.

---
*Built with ❤️ for modern healthcare management.*
