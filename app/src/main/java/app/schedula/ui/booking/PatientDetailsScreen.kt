package app.schedula.ui.booking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.ui.profile.ProfileViewModel
import app.schedula.ui.theme.SchedulaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    onBackClick: () -> Unit,
    onContinueToPayment: () -> Unit,
    bookingViewModel: BookingViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val user by profileViewModel.user.collectAsState()
    val familyMembers by profileViewModel.familyMembers.collectAsState()

    val patientDetails by bookingViewModel.patientDetails.collectAsState()

    var fullName by remember { mutableStateOf(patientDetails.fullName) }
    var dob by remember { mutableStateOf(patientDetails.dob) }
    var weight by remember { mutableStateOf(patientDetails.weight) }
    var contactNumber by remember { mutableStateOf(patientDetails.contactNumber) }
    var knownAllergies by remember { mutableStateOf(patientDetails.knownAllergies) }
    var currentMedications by remember { mutableStateOf(patientDetails.currentMedications) }
    var complaint by remember { mutableStateOf(patientDetails.complaint) }
    var selectedSex by remember { mutableStateOf(patientDetails.gender) }
    var selectedBloodType by remember { mutableStateOf(patientDetails.bloodType) }

    var sexExpanded by remember { mutableStateOf(false) }
    var bloodExpanded by remember { mutableStateOf(false) }

    val sexOptions = listOf("Male", "Female", "Other")
    val bloodOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    var selectedPatientType by remember { mutableStateOf("Myself") }
    var familyMemberExpanded by remember { mutableStateOf(false) }

    val isFormValid = fullName.isNotBlank() && dob.isNotBlank() && contactNumber.isNotBlank() && complaint.isNotBlank()

    LaunchedEffect(patientDetails) {
        fullName = patientDetails.fullName
        dob = patientDetails.dob
        weight = patientDetails.weight
        contactNumber = patientDetails.contactNumber
        knownAllergies = patientDetails.knownAllergies
        currentMedications = patientDetails.currentMedications
        complaint = patientDetails.complaint
        selectedSex = patientDetails.gender
        selectedBloodType = patientDetails.bloodType
    }

    SchedulaTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Patient Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    text = "Select Patient",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPatientType == "Myself",
                        onClick = { 
                            selectedPatientType = "Myself"
                            user?.let { bookingViewModel.onPatientSelected(it) }
                        }
                    )
                    Text("Myself", modifier = Modifier.padding(start = 4.dp).clickable { 
                        selectedPatientType = "Myself"
                        user?.let { bookingViewModel.onPatientSelected(it) }
                    })
                    
                    RadioButton(
                        selected = selectedPatientType == "Family Member",
                        onClick = { 
                            selectedPatientType = "Family Member"
                            bookingViewModel.onOtherSelected()
                        }
                    )
                    Text("Family", modifier = Modifier.padding(start = 4.dp).clickable { 
                        selectedPatientType = "Family Member"
                        bookingViewModel.onOtherSelected()
                    })
                    
                    RadioButton(
                        selected = selectedPatientType == "Other",
                        onClick = { 
                            selectedPatientType = "Other"
                            bookingViewModel.onOtherSelected()
                        }
                    )
                    Text("Other", modifier = Modifier.padding(start = 4.dp).clickable { 
                        selectedPatientType = "Other"
                        bookingViewModel.onOtherSelected()
                    })
                }

                if (selectedPatientType == "Family Member") {
                    Spacer(Modifier.height(16.dp))
                    ExposedDropdownMenuBox(
                        expanded = familyMemberExpanded,
                        onExpandedChange = { familyMemberExpanded = !familyMemberExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (fullName.isNotEmpty()) fullName else "Select Member",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Family Member") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = familyMemberExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = familyMemberExpanded,
                            onDismissRequest = { familyMemberExpanded = false }
                        ) {
                            familyMembers.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.name ?: "") },
                                    onClick = {
                                        bookingViewModel.onPatientSelected(member)
                                        familyMemberExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(20.dp))

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Date of Birth
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Contact Number
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sex Dropdown
                ExposedDropdownMenuBox(
                    expanded = sexExpanded,
                    onExpandedChange = { sexExpanded = !sexExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSex,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false }
                    ) {
                        sexOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedSex = option
                                    sexExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weight
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Blood Group Dropdown
                ExposedDropdownMenuBox(
                    expanded = bloodExpanded,
                    onExpandedChange = { bloodExpanded = !bloodExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBloodType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Blood Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = bloodExpanded,
                        onDismissRequest = { bloodExpanded = false }
                    ) {
                        bloodOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedBloodType = option
                                    bloodExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Known Allergies
                OutlinedTextField(
                    value = knownAllergies,
                    onValueChange = { knownAllergies = it },
                    label = { Text("Known Allergies (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current Medications
                OutlinedTextField(
                    value = currentMedications,
                    onValueChange = { currentMedications = it },
                    label = { Text("Current Medications (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Complaint
                OutlinedTextField(
                    value = complaint,
                    onValueChange = { complaint = it },
                    label = { Text("Primary Complaint / Reason for Visit") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        bookingViewModel.updateDetails(
                            patientDetails.copy(
                                fullName = fullName,
                                dob = dob,
                                contactNumber = contactNumber,
                                gender = selectedSex,
                                bloodType = selectedBloodType,
                                weight = weight,
                                knownAllergies = knownAllergies,
                                currentMedications = currentMedications,
                                complaint = complaint
                            )
                        )
                        onContinueToPayment()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid
                ) {
                    Text("Continue to Payment")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
