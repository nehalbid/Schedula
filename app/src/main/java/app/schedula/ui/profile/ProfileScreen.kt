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
    onPersonalInfoClick: () -> Unit,
    onFamilyMembersClick: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {

    val authViewModel: AuthViewModel = viewModel()
    val user by profileViewModel.user.collectAsState()

    var showRateDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    Scaffold(containerColor = Color(0xFFF6F8FB)) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            // ---------------- PROFILE HEADER ----------------

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

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { showAvatarDialog = true },
                        contentAlignment = Alignment.Center
                    ) {

                        val avatarIcon = when (user?.avatarIcon) {
                            "face1" -> Icons.Default.Face
                            "face2" -> Icons.Default.Person
                            "face3" -> Icons.Default.AccountCircle
                            "face4" -> Icons.Default.SentimentSatisfied
                            else -> Icons.Default.Person
                        }

                        Icon(
                            imageVector = avatarIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = user?.name?.ifBlank { "No Name" } ?: "No Name",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user?.phone ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ---------------- PROFILE MANAGEMENT ----------------

            Text(
                text = "Profile Management",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuItem(Icons.Default.Badge, "Personal Details") {
                onPersonalInfoClick()
            }

            ProfileMenuItem(Icons.Default.Group, "Family Members") {
                onFamilyMembersClick()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- SETTINGS ----------------

            Text(
                text = "Settings",
                fontSize = 16.sp
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

    // ---------------- AVATAR DIALOG ----------------

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
            onSelect = { selectedIcon ->

                profileViewModel.updateAvatar(selectedIcon)

                showAvatarDialog = false
            }
        )
    }

    // ---------------- RATE DIALOG ----------------

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
fun AvatarSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Select Avatar") },
        text = {
            Column {

                AvatarOption("face1", Icons.Default.Face, onSelect)
                AvatarOption("face2", Icons.Default.Person, onSelect)
                AvatarOption("face3", Icons.Default.AccountCircle, onSelect)
                AvatarOption("face4", Icons.Default.SentimentSatisfied, onSelect)
            }
        }
    )
}

@Composable
fun AvatarOption(
    key: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(key) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text("Select")
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
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title)
        }
    }
}