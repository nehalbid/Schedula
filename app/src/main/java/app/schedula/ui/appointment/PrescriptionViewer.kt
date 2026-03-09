package app.schedula.ui.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.schedula.data.model.Prescription
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionViewerDialog(
    prescription: Prescription,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Digital Prescription", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF6F8FB))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Doctor Info Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(prescription.doctorName, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Generated on ${SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(prescription.createdAt ?: java.util.Date())}", fontSize = 12.sp, color = Color.Gray)
                        
                        HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
                        
                        Row {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("DIAGNOSIS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                Text(prescription.diagnosis, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Medicines Section
                Text("MEDICATIONS", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.height(8.dp))
                
                prescription.medicines.forEach { medicine ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(medicine.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${medicine.dosage} • ${medicine.frequency}", fontSize = 13.sp, color = Color.Gray)
                            }
                            Text(medicine.duration, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Instructions Section
                if (prescription.instructions.isNotBlank()) {
                    Text("INSTRUCTIONS", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)) // Light yellow for focus
                    ) {
                        Text(
                            text = prescription.instructions,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    "This is a digitally generated prescription and does not require a physical signature.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
