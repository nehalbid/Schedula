package app.schedula.ui.doctorpanel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.schedula.data.model.Appointment
import app.schedula.data.model.Medicine
import app.schedula.data.model.Prescription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionFormScreen(
    appointment: Appointment,
    onBack: () -> Unit,
    onSubmit: (Prescription) -> Unit
) {
    var diagnosis by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    val medicines = remember { mutableStateListOf<Medicine>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Write Prescription", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1C1C1C))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Patient: ${appointment.patientDetails?.fullName}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = diagnosis,
                onValueChange = { diagnosis = it },
                label = { Text("Diagnosis") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Viral Fever") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Medicines", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Button(
                    onClick = { medicines.add(Medicine()) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            medicines.forEachIndexed { index, medicine ->
                MedicineItem(
                    medicine = medicine,
                    onUpdate = { updated: Medicine -> medicines[index] = updated },
                    onDelete = { medicines.removeAt(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (medicines.isEmpty()) {
                Text("No medicines added yet.", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Special Instructions") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("e.g. Bed rest for 3 days, drink plenty of fluids.") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val prescription = Prescription(
                        appointmentId = appointment.id,
                        doctorId = appointment.doctorId,
                        patientId = appointment.userId,
                        doctorName = appointment.doctorName,
                        diagnosis = diagnosis,
                        medicines = medicines.toList(),
                        instructions = instructions
                    )
                    onSubmit(prescription)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = diagnosis.isNotBlank() && medicines.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text("Submit Prescription", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MedicineItem(
    medicine: Medicine,
    onUpdate: (Medicine) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = medicine.name,
                    onValueChange = { onUpdate(medicine.copy(name = it)) },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = medicine.dosage,
                    onValueChange = { onUpdate(medicine.copy(dosage = it)) },
                    label = { Text("Dosage") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("500mg") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                OutlinedTextField(
                    value = medicine.frequency,
                    onValueChange = { onUpdate(medicine.copy(frequency = it)) },
                    label = { Text("Freq.") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("1-0-1") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                OutlinedTextField(
                    value = medicine.duration,
                    onValueChange = { onUpdate(medicine.copy(duration = it)) },
                    label = { Text("Dur.") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("5 days") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }
        }
    }
}
