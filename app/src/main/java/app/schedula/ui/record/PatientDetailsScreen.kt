package app.schedula.ui.records

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {

    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var complaint by remember { mutableStateOf("") }

    var selectedSex by remember { mutableStateOf("Male") }
    var selectedBloodType by remember { mutableStateOf("A+") }

    var sexExpanded by remember { mutableStateOf(false) }
    var bloodExpanded by remember { mutableStateOf(false) }

    val sexOptions = listOf("Male", "Female", "Other")
    val bloodOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    val isFormValid = fullName.isNotBlank() && age.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Patient Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
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
                text = "Patient Information",
                style = MaterialTheme.typography.titleLarge
            )

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

            // Age
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { ch -> ch.isDigit() } },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    label = { Text("Sex") },
                    trailingIcon = {
                        Icon(Icons.Default.KeyboardArrowDown, null)
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
                        Icon(Icons.Default.KeyboardArrowDown, null)
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

            // Complaint
            OutlinedTextField(
                value = complaint,
                onValueChange = { complaint = it },
                label = { Text("Primary Complaint") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    onSaveClick()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Save Patient Profile")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}