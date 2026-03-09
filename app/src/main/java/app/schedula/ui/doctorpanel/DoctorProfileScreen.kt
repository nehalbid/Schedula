package app.schedula.ui.doctorpanel

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    onBack: () -> Unit,
    viewModel: DoctorPanelViewModel = viewModel()
) {
    val doctor by viewModel.doctor.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(doctor) {
        doctor?.let {
            name = it.name
            specialty = it.specialty
            gender = it.gender
            experience = it.experience.toString()
            fee = it.fee.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1C1E)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ProfileSectionTitle("Personal Details")

                ProfileTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    icon = Icons.Default.Badge
                )

                Column {
                    Text(
                        "Gender",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Male", "Female", "Other").forEach { option ->
                            val isSelected = gender.equals(option, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { gender = option },
                                label = { Text(option) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF6F8FB),
                                    labelColor = Color.Gray
                                ),
                                border = null
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F3F6))
                
                ProfileSectionTitle("Professional Details")

                ProfileTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = "Specialization",
                    icon = Icons.Default.MedicalServices
                )

                ProfileTextField(
                    value = experience,
                    onValueChange = { if (it.all { c -> c.isDigit() }) experience = it },
                    label = "Experience (Years)",
                    icon = Icons.Default.HistoryEdu,
                    keyboardType = KeyboardType.Number
                )

                ProfileTextField(
                    value = fee,
                    onValueChange = { if (it.all { c -> c.isDigit() }) fee = it },
                    label = "Consultation Fee (₹)",
                    icon = Icons.Default.CurrencyRupee,
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isLoading = true
                        viewModel.updateProfileFull(
                            name = name.trim(),
                            specialty = specialty.trim(),
                            gender = gender,
                            experience = experience.toIntOrNull() ?: 0,
                            fee = fee.toIntOrNull() ?: 0
                        ) { success ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && name.isNotBlank() && specialty.isNotBlank() && gender.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Update Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.Gray,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = Color.Gray
        )
    )
}
