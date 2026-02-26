package app.schedula.ui.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onAuthError: () -> Unit
) {

    val viewModel: BookingViewModel = viewModel()

    val doctor by viewModel.doctor.collectAsState()
    val slots by viewModel.slots.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedSlot by viewModel.selectedSlot.collectAsState()
    val bookingState by viewModel.bookingState.collectAsState()
    val selectedConsultingType by viewModel.selectedConsultingType.collectAsState()


    LaunchedEffect(doctorId) {
        viewModel.loadDoctorAndSlots(doctorId)
    }

    LaunchedEffect(bookingState) {
        val (status, appointmentId) = bookingState
        when {
            status == "success" && appointmentId != null -> onBookingSuccess(appointmentId)
            status == "Slot already booked" -> onSlotUnavailable()
            status == "auth_error" -> onAuthError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .verticalScroll(rememberScrollState())
        ) {

            doctor?.let { DoctorInfoCard(it) }

            Spacer(modifier = Modifier.height(28.dp))

            DateSelection(selectedDate, viewModel)

            Spacer(modifier = Modifier.height(30.dp))

            TimeSlotSelection(slots, selectedSlot, viewModel)

            Spacer(modifier = Modifier.height(30.dp))

            ConsultingType(doctor?.fee ?: 0, selectedConsultingType, viewModel::selectConsultingType)

            Spacer(modifier = Modifier.height(24.dp))

            val isLoading = bookingState.first == "loading"
            val isSelectionValid = selectedSlot != null && selectedConsultingType != "Select Type"

            Button(
                onClick = { viewModel.bookSlot() },
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
                            "Confirm Appointment",
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
        (0..4).map { i ->
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

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            dates.forEach { date ->
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
    selectedSlot: Slot?,
    viewModel: BookingViewModel
) {

    val filteredSlots = slots.filter { it.time != "01:30 PM" && it.time != "07:30 PM" }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        Text(
            "Choose time",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(280.dp), // Increased height
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(filteredSlots) { slot ->

                Button(
                    onClick = { viewModel.selectSlot(slot) },
                    enabled = !slot.isBooked,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            slot.isBooked -> Color.LightGray
                            selectedSlot?.id == slot.id -> Color(0xFF2E7D32)
                            else -> Color(0xFFBBDEFB)
                        }
                    )
                ) {
                    Text(
                        slot.time,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
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
                    .background(Color.White)   // ✅ removes black background
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
