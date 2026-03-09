package app.schedula.ui.adminpanel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.data.model.Doctor

@Composable
fun ManageDoctorsScreen(
    onBack: () -> Unit,
    onEditDoctor: (Doctor) -> Unit,
    viewModel: AdminViewModel = viewModel()
) {

    val doctors by viewModel.doctors.collectAsState()

    var doctorToDelete by remember { mutableStateOf<Doctor?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Manage Doctors",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(onClick = onBack) {
                Text("Back")
            }
        }

        Spacer(Modifier.height(20.dp))

        LazyColumn {

            items(doctors) { doctor ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Dr. ${doctor.name}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(doctor.specialty)

                        Spacer(Modifier.height(4.dp))

                        Text("Phone: ${doctor.phone}")

                        Text("Status: ${doctor.status}")

                        Spacer(Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Button(
                                onClick = { onEditDoctor(doctor) }
                            ) {
                                Text("Edit")
                            }

                            Button(
                                onClick = {
                                    doctorToDelete = doctor
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    doctorToDelete?.let { doctor ->

        AlertDialog(
            onDismissRequest = { doctorToDelete = null },

            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDoctor(doctor.id)
                        doctorToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {
                OutlinedButton(
                    onClick = { doctorToDelete = null }
                ) {
                    Text("Cancel")
                }
            },

            title = { Text("Delete Doctor") },

            text = {
                Text("Are you sure you want to delete Dr. ${doctor.name}?")
            }
        )
    }
}