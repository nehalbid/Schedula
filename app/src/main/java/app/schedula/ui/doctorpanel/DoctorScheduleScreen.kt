package app.schedula.ui.doctorpanel

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

private val DarkTextColor = Color(0xFF1E293B)
private val DarkSubtitleColor = Color(0xFF475569)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DoctorScheduleScreen(
    onBack: () -> Unit,
    viewModel: DoctorPanelViewModel = viewModel()
) {
    val doctor by viewModel.doctor.collectAsState()
    val context = LocalContext.current

    // -------- STATES --------
    var monS by remember { mutableStateOf("") }
    var monE by remember { mutableStateOf("") }
    var monS2 by remember { mutableStateOf("") }
    var monE2 by remember { mutableStateOf("") }

    var tueS by remember { mutableStateOf("") }
    var tueE by remember { mutableStateOf("") }
    var tueS2 by remember { mutableStateOf("") }
    var tueE2 by remember { mutableStateOf("") }

    var wedS by remember { mutableStateOf("") }
    var wedE by remember { mutableStateOf("") }
    var wedS2 by remember { mutableStateOf("") }
    var wedE2 by remember { mutableStateOf("") }

    var thuS by remember { mutableStateOf("") }
    var thuE by remember { mutableStateOf("") }
    var thuS2 by remember { mutableStateOf("") }
    var thuE2 by remember { mutableStateOf("") }

    var friS by remember { mutableStateOf("") }
    var friE by remember { mutableStateOf("") }
    var friS2 by remember { mutableStateOf("") }
    var friE2 by remember { mutableStateOf("") }

    var satS by remember { mutableStateOf("") }
    var satE by remember { mutableStateOf("") }
    var satS2 by remember { mutableStateOf("") }
    var satE2 by remember { mutableStateOf("") }

    var sunS by remember { mutableStateOf("") }
    var sunE by remember { mutableStateOf("") }
    var sunS2 by remember { mutableStateOf("") }
    var sunE2 by remember { mutableStateOf("") }

    var duration by remember { mutableStateOf("30") }
    var selectedDateForSlots by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // -------- LOAD DATA --------
    LaunchedEffect(doctor) {
        doctor?.let {
            monS = it.mondayStart; monE = it.mondayEnd; monS2 = it.mondayStart2; monE2 = it.mondayEnd2
            tueS = it.tuesdayStart; tueE = it.tuesdayEnd; tueS2 = it.tuesdayStart2; tueE2 = it.tuesdayEnd2
            wedS = it.wednesdayStart; wedE = it.wednesdayEnd; wedS2 = it.wednesdayStart2; wedE2 = it.wednesdayEnd2
            thuS = it.thursdayStart; thuE = it.thursdayEnd; thuS2 = it.thursdayStart2; thuE2 = it.thursdayEnd2
            friS = it.fridayStart; friE = it.fridayEnd; friS2 = it.fridayStart2; friE2 = it.fridayEnd2
            satS = it.saturdayStart; satE = it.saturdayEnd; satS2 = it.saturdayStart2; satE2 = it.saturdayEnd2
            sunS = it.sundayStart; sunE = it.sundayEnd; sunS2 = it.sundayStart2; sunE2 = it.sundayEnd2
            duration = it.slotDuration.toString()
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            selectedDateForSlots = sdf.format(cal.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val offDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            val date = sdf.format(cal.time)
            viewModel.toggleOffDay(date) {
                Toast.makeText(context, "Holiday updated", Toast.LENGTH_SHORT).show()
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Schedule Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DarkTextColor)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
        ) {
            // ---------- WEEKLY SCHEDULE ----------
            Column(modifier = Modifier.padding(16.dp)) {
                ScheduleSectionHeader(
                    "Weekly Availability",
                    "Configure your working hours for each day of the week.",
                    Icons.Default.Schedule,
                    MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                DayScheduleCard("Monday", monS, monE, monS2, monE2,
                    { monS = it }, { monE = it }, { monS2 = it }, { monE2 = it },
                    onApplyAll = {
                        tueS = monS; tueE = monE; tueS2 = monS2; tueE2 = monE2
                        wedS = monS; wedE = monE; wedS2 = monS2; wedE2 = monE2
                        thuS = monS; thuE = monE; thuS2 = monS2; thuE2 = monE2
                        friS = monS; friE = monE; friS2 = monS2; friE2 = monE2
                        satS = monS; satE = monE; satS2 = monS2; satE2 = monE2
                        Toast.makeText(context,"Applied Monday schedule to other weekdays",Toast.LENGTH_SHORT).show()
                    }
                )

                DayScheduleCard("Tuesday", tueS, tueE, tueS2, tueE2, { tueS = it }, { tueE = it }, { tueS2 = it }, { tueE2 = it })
                DayScheduleCard("Wednesday", wedS, wedE, wedS2, wedE2, { wedS = it }, { wedE = it }, { wedS2 = it }, { wedE2 = it })
                DayScheduleCard("Thursday", thuS, thuE, thuS2, thuE2, { thuS = it }, { thuE = it }, { thuS2 = it }, { thuE2 = it })
                DayScheduleCard("Friday", friS, friE, friS2, friE2, { friS = it }, { friE = it }, { friS2 = it }, { friE2 = it })
                DayScheduleCard("Saturday", satS, satE, satS2, satE2, { satS = it }, { satE = it }, { satS2 = it }, { satE2 = it })
                DayScheduleCard("Sunday", sunS, sunE, sunS2, sunE2, { sunS = it }, { sunE = it }, { sunS2 = it }, { sunE2 = it })

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = duration,
                    onValueChange = { if (it.all { char -> char.isDigit() }) duration = it },
                    label = { Text("Slot Duration (Minutes)", color = DarkSubtitleColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Timer, null, tint = DarkSubtitleColor) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = DarkTextColor),
                    suffix = { Text("mins", color = DarkSubtitleColor, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        isSaving = true
                        viewModel.updateWeeklySchedule(
                            Pair(monS, monE), Pair(monS2, monE2),
                            Pair(tueS, tueE), Pair(tueS2, tueE2),
                            Pair(wedS, wedE), Pair(wedS2, wedE2),
                            Pair(thuS, thuE), Pair(thuS2, thuE2),
                            Pair(friS, friE), Pair(friS2, friE2),
                            Pair(satS, satE), Pair(satS2, satE2),
                            Pair(sunS, sunE), Pair(sunS2, sunE2),
                            duration.toIntOrNull() ?: 30
                        ) {
                            isSaving = false
                            Toast.makeText(context,"Schedule updated successfully",Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Weekly Schedule", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ---------- HOLIDAYS ----------
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScheduleSectionHeader(
                            "Public Holidays",
                            "Dates where you'll be unavailable.",
                            Icons.Default.EventBusy,
                            Color(0xFFE53935)
                        )

                        TextButton(
                            onClick = { offDatePickerDialog.show() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Date", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (doctor?.offDates?.isEmpty() == true) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No holidays added yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkSubtitleColor
                            )
                        }
                    } else {
                        doctor?.offDates?.forEach { date ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(date, fontWeight = FontWeight.SemiBold, color = DarkTextColor)
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleOffDay(date) {} },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- SLOT GENERATOR ----------
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ScheduleSectionHeader(
                        "Force Slot Generator",
                        "Instantly generate slots for a specific date.",
                        Icons.Default.AutoFixHigh,
                        Color(0xFF2E7D32)
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedCard(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF8FAFC))
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (selectedDateForSlots.isEmpty()) "Select Generation Date" else selectedDateForSlots,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedDateForSlots.isEmpty()) DarkSubtitleColor else DarkTextColor
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedDateForSlots.isEmpty()) return@Button
                            isGenerating = true
                            viewModel.generateSlotsForDate(selectedDateForSlots) { success, msg ->
                                isGenerating = false
                                Toast.makeText(context, if(success) "Slots generated successfully!" else msg ?: "Error", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        if (isGenerating) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Generate Slots", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ScheduleSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextColor))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DarkSubtitleColor, lineHeight = 14.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayScheduleCard(
    day: String,
    s1: String, e1: String, s2: String, e2: String,
    onS1: (String) -> Unit, onE1: (String) -> Unit,
    onS2: (String) -> Unit, onE2: (String) -> Unit,
    onApplyAll: (() -> Unit)? = null
) {
    // All cards closed by default
    var isExpanded by remember { mutableStateOf(false) }
    val isWorking = s1.isNotBlank() || s2.isNotBlank()

    // Summary text for collapsed state
    val summaryText = if (isWorking) {
        val part1 = if (s1.isNotBlank() && e1.isNotBlank()) "$s1 - $e1" else ""
        val part2 = if (s2.isNotBlank() && e2.isNotBlank()) "$s2 - $e2" else ""
        if (part1.isNotBlank() && part2.isNotBlank()) "$part1 | $part2"
        else if (part1.isNotBlank()) part1
        else if (part2.isNotBlank()) part2
        else "Shift not set"
    } else "Unavailable"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWorking) Color.White else Color(0xFFF1F5F9).copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isWorking) 1.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).clickable { isExpanded = !isExpanded }
                ) {
                    Checkbox(
                        checked = isWorking,
                        onCheckedChange = { checked ->
                            if (!checked) {
                                onS1(""); onE1(""); onS2(""); onE2("")
                                isExpanded = false
                            } else {
                                if (s1.isEmpty()) onS1("09:00")
                                if (e1.isEmpty()) onE1("13:00")
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Column {
                        Text(
                            day,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isWorking) FontWeight.Bold else FontWeight.Medium,
                                color = if (isWorking) DarkTextColor else DarkSubtitleColor
                            )
                        )
                        if (!isExpanded) {
                            Text(
                                summaryText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isWorking) MaterialTheme.colorScheme.primary else DarkSubtitleColor
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onApplyAll != null && isWorking) {
                        TextButton(onClick = onApplyAll, contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text("Apply to All", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            null,
                            tint = DarkSubtitleColor
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

                    // Presets Row
                    Text("Quick Presets", style = MaterialTheme.typography.labelSmall, color = DarkSubtitleColor)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip("Morning Only") {
                            onS1("09:00"); onE1("13:00"); onS2(""); onE2("")
                        }
                        PresetChip("Evening Only") {
                            onS1("17:00"); onE1("21:00"); onS2(""); onE2("")
                        }
                        PresetChip("Full Day") {
                            onS1("09:00"); onE1("13:00"); onS2("17:00"); onE2("21:00")
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("First Shift", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModernTimePicker(s1, "Start", Modifier.weight(1f)) { onS1(it) }
                        ModernTimePicker(e1, "End", Modifier.weight(1f)) { onE1(it) }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Second Shift (Optional)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        if (s2.isNotBlank() || e2.isNotBlank()) {
                            Text(
                                "Clear",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red,
                                modifier = Modifier.clickable { onS2(""); onE2("") }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModernTimePicker(s2, "Start", Modifier.weight(1f)) { onS2(it) }
                        ModernTimePicker(e2, "End", Modifier.weight(1f)) { onE2(it) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp) },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun ModernTimePicker(
    time: String,
    label: String,
    modifier: Modifier,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current

    fun showPicker() {
        val cal = Calendar.getInstance()
        val initialHour = if(time.isNotEmpty() && time.contains(":")) time.split(":")[0].toIntOrNull() ?: 9 else 9
        val initialMin = if(time.isNotEmpty() && time.contains(":")) time.split(":")[1].toIntOrNull() ?: 0 else 0

        TimePickerDialog(context, { _, h, m ->
            onTimeSelected(String.format("%02d:%02d", h, m))
        }, initialHour, initialMin, true).show()
    }

    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable { showPicker() },
        shape = RoundedCornerShape(10.dp),
        color = if (time.isEmpty()) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = if (time.isNotEmpty()) null else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(
                    Icons.Default.AccessTime,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = if (time.isEmpty()) DarkSubtitleColor else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (time.isEmpty()) label else time,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (time.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                        color = if (time.isEmpty()) DarkSubtitleColor else DarkTextColor
                    )
                )
            }
        }
    }
}
