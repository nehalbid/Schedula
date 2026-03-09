package app.schedula.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.ui.theme.SchedulaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBackClick: () -> Unit,
    onPaymentSuccess: (String) -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {

    val context = LocalContext.current  // ✅ Added

    val doctor by bookingViewModel.doctor.collectAsState()
    val bookingState by bookingViewModel.bookingState.collectAsState()
    val selectedConsultingType by bookingViewModel.selectedConsultingType.collectAsState()

    var selectedMethod by remember { mutableStateOf("UPI") }
    val isLoading = bookingState.first == "loading"

    LaunchedEffect(bookingState) {
        if (bookingState.first == "success" && bookingState.second != null) {
            onPaymentSuccess(bookingState.second!!)
        }
    }

    SchedulaTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Payment") },
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
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // Bill Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bill Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Consultation Fee", color = Color.Gray)
                            Text("₹${doctor?.fee ?: 0}", fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Service Charge", color = Color.Gray)
                            Text("₹50", fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount", fontWeight = FontWeight.Bold)
                            Text(
                                "₹${(doctor?.fee ?: 0) + 50}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                PaymentMethodItem(
                    "UPI",
                    "Pay via Google Pay, PhonePe, etc.",
                    selectedMethod == "UPI"
                ) { selectedMethod = "UPI" }

                PaymentMethodItem(
                    "Cards",
                    "Credit or Debit Cards",
                    selectedMethod == "Cards"
                ) { selectedMethod = "Cards" }

                val isRegularConsultation =
                    selectedConsultingType.lowercase().contains("regular") ||
                            selectedConsultingType.lowercase().contains("person")

                if (isRegularConsultation) {
                    PaymentMethodItem(
                        "Cash",
                        "Pay at the clinic desk",
                        selectedMethod == "Cash"
                    ) { selectedMethod = "Cash" }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        bookingViewModel.bookSlot(context)  // ✅ FIXED HERE
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        val buttonText =
                            if (selectedMethod == "Cash")
                                "Confirm & Book"
                            else
                                "Pay Now & Book Appointment"

                        Text(buttonText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = null)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}