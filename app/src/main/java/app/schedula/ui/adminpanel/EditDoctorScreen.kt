package app.schedula.ui.adminpanel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun EditDoctorScreen(
    doctorId: String,
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {

    val doctors by viewModel.doctors.collectAsState()
    val doctor = doctors.find { it.id == doctorId }

    if (doctor == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Doctor not found")
        }
        return
    }

    var name by remember { mutableStateOf(doctor.name) }
    var specialty by remember { mutableStateOf(doctor.specialty) }
    var phone by remember { mutableStateOf(doctor.phone.filter { it.isDigit() }.takeLast(10)) }
    var experience by remember { mutableStateOf(doctor.experience.toString()) }
    var fee by remember { mutableStateOf(doctor.fee.toString()) }
    var status by remember { mutableStateOf(doctor.status) }
    var gender by remember { mutableStateOf(doctor.gender) }

    val error by viewModel.error.collectAsState()
    var localError by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedLabelColor = Color(0xFF9C6ADE),
        unfocusedLabelColor = Color.DarkGray,
        focusedBorderColor = Color(0xFF9C6ADE),
        unfocusedBorderColor = Color.Gray,
        cursorColor = Color(0xFF9C6ADE)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(20.dp)
        ) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Edit Doctor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C6ADE)
                )
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Doctor Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = specialty,
                onValueChange = { specialty = it },
                label = { Text("Specialty") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                label = { Text("Phone Number (10 digits)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = experience,
                onValueChange = { if (it.all { c -> c.isDigit() }) experience = it },
                label = { Text("Experience (years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = fee,
                onValueChange = { if (it.all { c -> c.isDigit() }) fee = it },
                label = { Text("Consultation Fee") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Gender",
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )

            Row(verticalAlignment = Alignment.CenterVertically) {

                RadioButton(
                    selected = gender == "Male",
                    onClick = { gender = "Male" }
                )
                Text("Male")

                Spacer(Modifier.width(16.dp))

                RadioButton(
                    selected = gender == "Female",
                    onClick = { gender = "Female" }
                )
                Text("Female")

                Spacer(Modifier.width(16.dp))

                RadioButton(
                    selected = gender == "Other",
                    onClick = { gender = "Other" }
                )
                Text("Other")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Status (ACTIVE / DISABLED)") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(Modifier.height(16.dp))

            val displayError = error ?: localError
            if (displayError.isNotEmpty()) {
                Text(displayError, color = Color.Red)
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                Button(
                    onClick = {
                        viewModel.clearError()
                        val expInt = experience.toIntOrNull()
                        val feeInt = fee.toIntOrNull()

                        if (name.isBlank() || specialty.isBlank() || phone.length != 10) {
                            localError = if (phone.length != 10) "Phone must be exactly 10 digits" 
                                         else "Please fill all required fields"
                            return@Button
                        }

                        if (expInt == null || feeInt == null) {
                            localError = "Experience and Fee must be numbers"
                            return@Button
                        }

                        localError = ""

                        viewModel.updateDoctor(
                            doctorId = doctor.id,
                            name = name.trim(),
                            specialty = specialty.trim(),
                            experience = expInt,
                            fee = feeInt,
                            phone = phone.trim(),
                            status = status.trim(),
                            gender = gender
                        ) { success ->
                            if (success) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Doctor updated successfully")
                                }
                                onBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBFA2DB)
                    )
                ) {
                    Text("Update Doctor", color = Color.Black)
                }

                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }

}
