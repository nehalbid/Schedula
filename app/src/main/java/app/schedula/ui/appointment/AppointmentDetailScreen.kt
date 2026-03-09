package app.schedula.ui.appointment

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.data.model.Slot
import app.schedula.ui.booking.PatientDetails
import app.schedula.ui.components.StatusBadge
import app.schedula.utils.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: String,
    viewModel: AppointmentDetailViewModel = viewModel(),
    onBack: () -> Unit,
    onCancelClick: (String) -> Unit,
    onRescheduleSuccess: (String) -> Unit
) {

    LaunchedEffect(appointmentId) {
        viewModel.loadAppointment(appointmentId)
    }

    val appointment by viewModel.appointment.collectAsState()
    val prescription by viewModel.prescription.collectAsState()
    
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var showEditPatientDialog by remember { mutableStateOf(false) }
    var showPrescriptionViewer by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (appointment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appointment Details", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF6F8FB)
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF6F8FB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // Top Status
            appointment?.let { 
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    StatusBadge(it.status)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔥 TELEMEDICINE ACTION (ONLY IF ONGOING AND ONLINE)
            if (appointment?.status == "ONGOING" && appointment?.meetingLink != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.Red)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Live Consultation Started", fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5), modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appointment?.meetingLink))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Join Now")
                        }
                    }
                }
            }

            // 🔥 VIEW PRESCRIPTION ACTION (ONLY IF COMPLETED AND EXISTS)
            if (appointment?.status == "COMPLETED" && prescription != null) {
                Button(
                    onClick = { showPrescriptionViewer = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Medication, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("View Digital Prescription", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // PART 1: BOOKING DETAILS
            SectionHeader("BOOKING DETAILS")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("Booking ID", appointment?.id ?: "", Icons.Default.Tag)
                    DetailRow("Type", appointment?.consultingType ?: "", Icons.Default.Videocam)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                    DetailRow("Date", appointment?.date ?: "", Icons.Default.CalendarMonth)
                    DetailRow("Time", appointment?.time ?: "", Icons.Default.Schedule)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Payment Info", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    appointment?.paymentDetails?.let { payment ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", color = Color.Gray, fontSize = 14.sp)
                            Text("₹${payment.totalAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("Status: ${payment.status}", color = if (payment.status == "PAID") Color(0xFF4CAF50) else Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PART 2: DOCTOR DETAILS
            SectionHeader("DOCTOR DETAILS")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("Doctor Name", appointment?.doctorName ?: "", Icons.Default.Person)
                    DetailRow("Specialization", appointment?.doctorSpecialty ?: "", Icons.Default.MedicalServices)
                    DetailRow("Location", "Medical Center, Room 402", Icons.Default.LocationOn)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PART 3: PATIENT DETAILS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("PATIENT DETAILS")
                if (appointment?.status == "UPCOMING") {
                    TextButton(onClick = { showEditPatientDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", fontSize = 13.sp)
                    }
                }
            }
            appointment?.patientDetails?.let { patient ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Patient Name", patient.fullName, Icons.Default.AccountCircle)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) { DetailRow("Gender", patient.gender, Icons.Default.Wc) }
                            Box(modifier = Modifier.weight(1f)) { DetailRow("Blood", patient.bloodType, Icons.Default.Bloodtype) }
                        }
                        DetailRow("Contact", patient.contactNumber, Icons.Default.Phone)
                        DetailRow("Complaint", patient.complaint, Icons.AutoMirrored.Filled.Notes)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (appointment?.status == "UPCOMING") {
                Button(
                    onClick = { showRescheduleDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                        contentColor = Color(0xFF311B92)
                    ),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.EditCalendar, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reschedule Appointment", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onCancelClick(appointmentId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cancel Appointment", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showRescheduleDialog) {
        RescheduleDialog(
            viewModel = viewModel,
            onDismiss = { showRescheduleDialog = false },
            onSuccess = { newAppointmentId ->
                showRescheduleDialog = false
                onRescheduleSuccess(newAppointmentId)
            }
        )
    }

    if (showEditPatientDialog && appointment?.patientDetails != null) {
        EditPatientDialog(
            currentDetails = appointment!!.patientDetails!!,
            viewModel = viewModel,
            onDismiss = { showEditPatientDialog = false },
            onSuccess = { showEditPatientDialog = false }
        )
    }

    if (showPrescriptionViewer && prescription != null) {
        PrescriptionViewerDialog(
            prescription = prescription!!,
            onDismiss = { showPrescriptionViewer = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPatientDialog(
    currentDetails: PatientDetails,
    viewModel: AppointmentDetailViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf(currentDetails.fullName) }
    var dob by remember { mutableStateOf(currentDetails.dob) }
    var contactNumber by remember { mutableStateOf(currentDetails.contactNumber) }
    var gender by remember { mutableStateOf(currentDetails.gender) }
    var bloodType by remember { mutableStateOf(currentDetails.bloodType) }
    var weight by remember { mutableStateOf(currentDetails.weight) }
    var knownAllergies by remember { mutableStateOf(currentDetails.knownAllergies) }
    var currentMedications by remember { mutableStateOf(currentDetails.currentMedications) }
    var complaint by remember { mutableStateOf(currentDetails.complaint) }
    
    val isUpdating by viewModel.isUpdatingPatient.collectAsState()

    var sexExpanded by remember { mutableStateOf(false) }
    var bloodExpanded by remember { mutableStateOf(false) }
    val sexOptions = listOf("Male", "Female", "Other")
    val bloodOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text("Edit Patient Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Contact Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                ExposedDropdownMenuBox(
                    expanded = sexExpanded,
                    onExpandedChange = { sexExpanded = !sexExpanded }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false }
                    ) {
                        sexOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    sexExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ExposedDropdownMenuBox(
                    expanded = bloodExpanded,
                    onExpandedChange = { bloodExpanded = !bloodExpanded }
                ) {
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Blood Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = bloodExpanded,
                        onDismissRequest = { bloodExpanded = false }
                    ) {
                        bloodOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    bloodType = option
                                    bloodExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = knownAllergies,
                    onValueChange = { knownAllergies = it },
                    label = { Text("Known Allergies") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = currentMedications,
                    onValueChange = { currentMedications = it },
                    label = { Text("Current Medications") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = complaint,
                    onValueChange = { complaint = it },
                    label = { Text("Complaint") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        viewModel.updatePatientDetails(
                            PatientDetails(
                                fullName = fullName,
                                dob = dob,
                                contactNumber = contactNumber,
                                gender = gender,
                                bloodType = bloodType,
                                weight = weight,
                                knownAllergies = knownAllergies,
                                currentMedications = currentMedications,
                                complaint = complaint
                            ),
                            onSuccess
                        )
                    },
                    enabled = !isUpdating && fullName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RescheduleDialog(
    viewModel: AppointmentDetailViewModel,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedSlot by remember { mutableStateOf<Slot?>(null) }
    val slots by viewModel.availableSlots.collectAsState()
    val isRescheduling by viewModel.isRescheduling.collectAsState()
    val appointment by viewModel.appointment.collectAsState()
    val context = LocalContext.current

    val dates = remember {
        (0..6).map { i ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, i) }
        }
    }

    LaunchedEffect(selectedDate) {
        viewModel.loadSlotsForReschedule(selectedDate)
    }

    // Filter out duplicates and sort
    val uniqueSlots = slots.distinctBy { it.time }.sortedBy { parseHour(it.time) }
    val morningSlots = uniqueSlots.filter { parseHour(it.time) < 14 }
    val eveningSlots = uniqueSlots.filter { parseHour(it.time) >= 14 }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text("Reschedule", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select Date", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dates) { date ->
                        val isSelected = selectedDate.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF5F7FA))
                                .clickable { 
                                    selectedDate = date
                                    selectedSlot = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = SimpleDateFormat("dd", Locale.ENGLISH).format(date.time),
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Select Time", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                
                if (slots.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No slots available", color = Color.LightGray, fontSize = 12.sp)
                    }
                } else {
                    if (morningSlots.isNotEmpty()) {
                        Text("Morning", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        RescheduleSlotGrid(morningSlots, selectedSlot, appointment?.slotId) { selectedSlot = it }
                        Spacer(Modifier.height(16.dp))
                    }

                    if (eveningSlots.isNotEmpty()) {
                        Text("Evening", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        RescheduleSlotGrid(eveningSlots, selectedSlot, appointment?.slotId) { selectedSlot = it }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        viewModel.rescheduleAppointment(selectedDate, selectedSlot!!) { newApptId ->
                            // 🔥 Reschedule alarms for new time
                            val cal = selectedDate.clone() as Calendar
                            val timeCal = parseTime(selectedSlot!!.time)
                            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                            
                            ReminderScheduler.scheduleReminders(context, newApptId, cal.time)
                            onSuccess(newApptId)
                        }
                    },
                    enabled = selectedSlot != null && !isRescheduling,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isRescheduling) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Update Appointment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RescheduleSlotGrid(slots: List<Slot>, selectedSlot: Slot?, currentSlotId: String?, onSelect: (Slot) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
    ) {
        slots.forEach { slot ->
            val isSelected = selectedSlot?.id == slot.id
            val isActuallyBooked = slot.isBooked && slot.id != currentSlotId
            
            Box(
                modifier = Modifier
                    .width(85.dp) // Fixed width for consistent grid look
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isActuallyBooked -> Color.LightGray.copy(alpha = 0.3f)
                            else -> Color(0xFFF5F7FA)
                        }
                    )
                    .clickable(enabled = !isActuallyBooked) { onSelect(slot) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    slot.time,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isSelected -> Color.White
                        isActuallyBooked -> Color.Gray
                        else -> Color.Black
                    }
                )
            }
        }
    }
}

private fun parseTime(timeString: String): Calendar {
    val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    val date = sdf.parse(timeString)
    val calendar = Calendar.getInstance()
    val timeCalendar = Calendar.getInstance()
    timeCalendar.time = date ?: java.util.Date()
    
    calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
    calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}

private fun parseHour(time: String): Int {
    return try {
        val parts = time.split(":")
        var hour = parts[0].trim().toInt()
        val isPm = time.uppercase().contains("PM")
        if (isPm && hour != 12) hour += 12
        if (!isPm && hour == 12) hour = 0
        hour
    } catch (e: Exception) { 0 }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = Color.Gray,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun DetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}
