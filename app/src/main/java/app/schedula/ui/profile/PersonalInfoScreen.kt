package app.schedula.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.ui.theme.SchedulaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    onBackClick: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    SchedulaTheme(darkTheme = false) {
        val context = LocalContext.current
        Log.d("PersonalInfoScreen", "PersonalInfoScreen composable is being displayed.")

        val user by profileViewModel.user.collectAsState()
        var isLoading by remember { mutableStateOf(false) }

        var name by remember { mutableStateOf("") }
        var dob by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("") }
        var bloodGroup by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }

        // Sync state when user loads
        LaunchedEffect(user) {
            user?.let {
                name = it.name ?: ""
                dob = it.dob ?: ""
                gender = it.gender ?: ""
                bloodGroup = it.bloodGroup ?: ""
                email = it.email ?: ""
                location = it.location ?: ""
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Personal Details") },
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth") },
                    placeholder = { Text("DD/MM/YYYY") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { bloodGroup = it },
                    label = { Text("Blood Group") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = user?.phone ?: "",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        profileViewModel.updateProfile(
                            name = name,
                            dob = dob,
                            gender = gender,
                            bloodGroup = bloodGroup,
                            email = email,
                            location = location,
                            onResult = { success ->
                                isLoading = false
                                if (success) {
                                    Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                } else {
                                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
