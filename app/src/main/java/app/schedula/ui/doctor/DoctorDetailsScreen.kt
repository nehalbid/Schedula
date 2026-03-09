package app.schedula.ui.doctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.R
import app.schedula.data.model.Doctor
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailsScreen(
    doctorId: String,
    onBackClick: () -> Unit,
    onBookAppointmentClick: (String) -> Unit
) {

    val viewModel: DoctorViewModel = viewModel()
    val doctor by viewModel.doctor.collectAsState()

    LaunchedEffect(doctorId) {
        viewModel.loadDoctor(doctorId)
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Doctor Profile", color = Color.Black, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (doctor == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            doctor?.let {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color(0xFFF5F7FA))
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    DoctorHeader(it)
                    Spacer(modifier = Modifier.height(24.dp))
                    StatsRow(it)
                    Spacer(modifier = Modifier.height(24.dp))
                    ServicesSection()
                    Spacer(modifier = Modifier.height(24.dp))
                    AvailabilitySection()
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            onBookAppointmentClick(doctorId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBBDEFB),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Book Appointment", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DoctorHeader(doctor: Doctor) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(
                id = if (doctor.gender == "male")
                    R.drawable.doctor_male
                else
                    R.drawable.doctor_female
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(doctor.name, style = MaterialTheme.typography.headlineSmall, color = Color.Black)
        Text(doctor.specialty, color = Color(0xFF1976D2), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${doctor.experience} years experience | MD, MBBS",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Highly recommended for prenatal care and postpartum recovery.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                Text("Follow")
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                Text("Message")
            }
        }
    }
}

@Composable
fun StatsRow(doctor: Doctor) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatItem("1.2k+", "Patients")
        StatItem(doctor.experience.toString(), "Exp. Years")
        StatItem(doctor.rating.toString(), "Rating", showStar = true)
    }
}

@Composable
fun StatItem(value: String, label: String, showStar: Boolean = false) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showStar) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            }
            Text(label, color = Color.DarkGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun ServicesSection() {
    Column {
        Text("Services", style = MaterialTheme.typography.titleMedium, color = Color.Black, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        ServiceItem("Pregnancy Care", "Full term prenatal support")
        ServiceItem("New Born", "Post-birth infant care guidance")
        ServiceItem("New Mother", "Postpartum health & wellness")
    }
}

@Composable
fun ServiceItem(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // You can add an icon here if you want
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun AvailabilitySection() {
    Column {
        Text(
            "Availability for consulting",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvailabilityItem("MON - FRI", "10:00 AM - 07:00 PM", modifier = Modifier.weight(1f))
            AvailabilityItem("SAT", "10:00 AM - 01:00 PM", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AvailabilityItem(day: String, time: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = time,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}
