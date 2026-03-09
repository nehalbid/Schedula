package app.schedula.ui.adminpanel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onAddDoctor: () -> Unit,
    onManageDoctors: () -> Unit,
    onAppointments: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {

    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                Text(
                    "System Overview",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientStatCard(
                        title = "Doctors",
                        value = stats.doctors.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                    GradientStatCard(
                        title = "Patients",
                        value = stats.patients.toString(),
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientStatCard(
                        title = "Appointments",
                        value = stats.appointments.toString(),
                        icon = Icons.Default.Event,
                        modifier = Modifier.weight(1f)
                    )
                    GradientStatCard(
                        title = "Avg. Load",
                        value = "Normal",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- TOP SPECIALTIES SECTION ---
            if (stats.topSpecialties.isNotEmpty()) {
                item {
                    Text(
                        "Top Specialties",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            stats.topSpecialties.forEach { (name, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(name, fontWeight = FontWeight.Medium)
                                    }
                                    Text(
                                        "$count Doctors",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton("Add Doctor", Icons.Default.PersonAdd, onAddDoctor)
                    ActionButton("Manage Doctors", Icons.Default.MedicalServices, onManageDoctors)
                    ActionButton("View Appointments", Icons.Default.EventNote, onAppointments)
                }
            }
        }
    }
}

@Composable
fun GradientStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {

    val gradient = Brush.linearGradient(
        listOf(
            Color(0xFFB993D6),
            Color(0xFF8CA6DB)
        )
    )

    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(title, color = Color.White, fontSize = 14.sp)
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(4.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}
