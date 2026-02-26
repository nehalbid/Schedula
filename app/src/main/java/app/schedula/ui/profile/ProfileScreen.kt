package app.schedula.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.ui.auth.AuthViewModel
import app.schedula.ui.common.RateAppDialog

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onSupportClick: () -> Unit,
    onRateClick: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {

    val authViewModel: AuthViewModel = viewModel()
    val user by profileViewModel.user.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }

    // Automatically show the edit dialog if the user's name is blank.
    LaunchedEffect(user) {
        if (user != null && user?.name?.isBlank() == true) {
            showEditDialog = true
        }
    }

    Scaffold(containerColor = Color(0xFFF6F8FB)) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            // Profile Header Card
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.Black,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(6.dp)
                                .clickable { showEditDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = user?.name?.takeIf { it.isNotBlank() } ?: user?.phone ?: "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user?.phone ?: "",
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Account Information",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoItem(
                Icons.Default.Person, 
                "Full Name", 
                user?.name?.takeIf { it.isNotBlank() } ?: user?.phone ?: ""
            )
            ProfileInfoItem(Icons.Default.Email, "Email", user?.email ?: "")
            ProfileInfoItem(Icons.Default.LocationOn, "Location", user?.location ?: "")

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Settings",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuItem(Icons.Default.SupportAgent, "Support Center") {
                onSupportClick()
            }

            ProfileMenuItem(Icons.Default.StarRate, "Rate App") {
                showRateDialog = true
            }

            ProfileMenuItem(Icons.Default.PrivacyTip, "Privacy Policy") { }

            ProfileMenuItem(Icons.Default.Info, "About App") { }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            currentName = user?.name ?: "",
            currentEmail = user?.email ?: "",
            currentLocation = user?.location ?: "",
            onDismiss = { showEditDialog = false },
            onSave = { newName, newEmail, newLocation ->
                profileViewModel.updateProfile(newName, newEmail, newLocation)
                showEditDialog = false
            }
        )
    }

    if (showRateDialog) {
        RateAppDialog(
            onDismiss = { showRateDialog = false },
            onRateClick = {
                showRateDialog = false
                onRateClick()
            }
        )
    }
}

@Composable
fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 12.sp, color = Color.Gray)
                Text(value, fontWeight = FontWeight.Medium, color = Color.Black)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentEmail: String,
    currentLocation: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {

    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var location by remember { mutableStateOf(currentLocation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(name, email, location) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
            }
        }
    )
}
