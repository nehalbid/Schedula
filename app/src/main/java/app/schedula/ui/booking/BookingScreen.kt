package app.schedula.ui.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.R
import app.schedula.data.model.Doctor
import app.schedula.data.model.Slot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    doctorId: String,
    onBackClick: () -> Unit,
    onBookingSuccess: (String) -> Unit,
    onSlotUnavailable: () -> Unit,
    onAuthError: () -> Unit,
    onPatientDetailsClick: () -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {

    val doctor by bookingViewModel.doctor.collectAsState()
    val slots by bookingViewModel.slots.collectAsState()
    val isLoadingSlots by bookingViewModel.isLoadingSlots.collectAsState()
    val selectedDate by bookingViewModel.selectedDate.collectAsState()
    val selectedSlot by bookingViewModel.selectedSlot.collectAsState()
    val bookingState by bookingViewModel.bookingState.collectAsState()
    val selectedConsultingType by bookingViewModel.selectedConsultingType.collectAsState()


    LaunchedEffect(doctorId) {
        bookingViewModel.loadDoctorAndSlots(doctorId)
    }

    LaunchedEffect(bookingState) {
        val (status, appointmentId) = bookingState
        when {
            status == "success" && appointmentId != null -> {
                onBookingSuccess(appointmentId)
                bookingViewModel.resetBookingState() // Reset so it doesn't trigger again
            }
            status == "Slot already booked" || (status == "error" && bookingState.second?.contains("already booked") == true) -> {
                onSlotUnavailable()
            }
            status == "auth_error" -> onAuthError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Appointment", color = Color.Black, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F7FA)
                )
            )
        }
    ) { padding ->

        if (doctor == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FA))
                    .verticalScroll(rememberScrollState())
            ) {

                doctor?.let { DoctorInfoCard(it) }

                Spacer(modifier = Modifier.height(28.dp))

                DateSelection(selectedDate, bookingViewModel)

                Spacer(modifier = Modifier.height(30.dp))

                TimeSlotSelection(slots, isLoadingSlots, selectedSlot, bookingViewModel)

                Spacer(modifier = Modifier.height(30.dp))

                ConsultingType(doctor?.fee ?: 0, selectedConsultingType, bookingViewModel::selectConsultingType)

                Spacer(modifier = Modifier.height(24.dp))

                val isLoading = bookingState.first == "loading"
                val isSelectionValid = selectedSlot != null && selectedConsultingType != "Select Type"

                Button(
                    onClick = onPatientDetailsClick,
                    enabled = isSelectionValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBBDEFB),
                        contentColor = Color.Black,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.DarkGray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Add Patient Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorInfoCard(doctor: Doctor) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(
                    id = if (doctor.gender == "male")
                        R.drawable.doctor_male
                    else
                        R.drawable.doctor_female
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(85.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${doctor.rating} (120 reviews)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    doctor.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp,
                    color = Color.Black
                )

                Text(
                    doctor.specialty.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF1976D2)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "${doctor.experience} years experience • St. Mary's Hospital",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DateSelection(selectedDate: Calendar, viewModel: BookingViewModel) {

    val dates = remember {
        (0..6).map { i ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, i) }
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        Text(
            "Choose date",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dates) { date ->
                val isSelected =
                    selectedDate.get(Calendar.DAY_OF_YEAR) ==
                            date.get(Calendar.DAY_OF_YEAR)

                DateBox(date, isSelected) { viewModel.selectDate(it) }
            }
        }
    }
}

@Composable
fun DateBox(date: Calendar, isSelected: Boolean, onClick: (Calendar) -> Unit) {

    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) Color(0xFF1976D2) else Color.White)
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                dayFormat.format(date.time).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else Color.Gray
            )

            Text(
                dateFormat.format(date.time),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun TimeSlotSelection(
    slots: List<Slot>,
    isLoading: Boolean,
    selectedSlot: Slot?,
    viewModel: BookingViewModel
) {

    // Grouping slots by morning (before 2pm) and evening (after 2pm)
    // Also ensuring unique slots by time
    val uniqueSlots = slots.distinctBy { it.time }.sortedBy { parseHour(it.time) }
    
    val morningSlots = uniqueSlots.filter { 
        val hour = parseHour(it.time)
        hour < 14 
    }
    val eveningSlots = uniqueSlots.filter { 
        val hour = parseHour(it.time)
        hour >= 14 
    }

    var morningExpanded by remember { mutableStateOf(false) }
    var eveningExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        Text(
            "Choose time",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (slots.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No slots available for this date", color = Color.Gray)
            }
        } else {
            // Morning Section
            if (morningSlots.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { morningExpanded = !morningExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Morning Slots (10 AM - 1 PM)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    Icon(
                        imageVector = if (morningExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(visible = morningExpanded) {
                    SlotGrid(morningSlots, selectedSlot) { viewModel.selectSlot(it) }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Evening Section
            if (eveningSlots.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { eveningExpanded = !eveningExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Evening Slots (4 PM - 7 PM)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    Icon(
                        imageVector = if (eveningExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(visible = eveningExpanded) {
                    SlotGrid(eveningSlots, selectedSlot) { viewModel.selectSlot(it) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SlotGrid(slots: List<Slot>, selectedSlot: Slot?, onSelect: (Slot) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 3
    ) {
        slots.forEach { slot ->
            val isSelected = selectedSlot?.id == slot.id
            
            Button(
                onClick = { onSelect(slot) },
                enabled = !slot.isBooked,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(100.dp).height(44.dp), // Fixed width for consistent grid
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        slot.isBooked -> Color.LightGray.copy(alpha = 0.4f)
                        isSelected -> Color(0xFF1976D2)
                        else -> Color.White
                    },
                    contentColor = if (isSelected) Color.White else if (slot.isBooked) Color.Gray else Color.Black,
                    disabledContainerColor = Color.LightGray.copy(alpha = 0.4f),
                    disabledContentColor = Color.Gray
                ),
                border = if (!isSelected && !slot.isBooked)
                            androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) 
                         else null
            ) {
                Text(
                    slot.time,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helper to parse hour from "10:30 AM" or "04:30 PM"
private fun parseHour(time: String): Int {
    return try {
        val parts = time.split(":")
        var hour = parts[0].trim().toInt()
        val isPm = time.uppercase().contains("PM")
        if (isPm && hour != 12) hour += 12
        if (!isPm && hour == 12) hour = 0
        hour
    } catch (e: Exception) {
        0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultingType(fee: Int, selectedType: String, onTypeSelected: (String) -> Unit) {

    val consultingTypes = listOf("Regular (in-person)", "Online")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        Text(
            "Consulting type",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Consulting Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                textStyle = TextStyle(
                    color = if (selectedType == "Select Type") Color.DarkGray else Color.Black,
                    fontWeight = if (selectedType == "Select Type") FontWeight.Normal else FontWeight.Medium,
                    fontSize = 16.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
            ) {
                consultingTypes.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        onClick = {
                            onTypeSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Standard consultation fee: ₹$fee",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
