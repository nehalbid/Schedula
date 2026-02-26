package app.schedula.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.schedula.R
import kotlin.random.Random

@Composable
fun OtpScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit
) {

    val state by viewModel.authState.collectAsState()
    val isLoading = state.first == "loading"

    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = List(6) { FocusRequester() }

    val quotes = listOf(
        "Your health is our top priority.",
        "Care that puts you first.",
        "Healing begins with trust.",
        "Better doctors. Better care.",
        "Healthcare made simple."
    )

    val randomQuote = remember { quotes[Random.nextInt(quotes.size)] }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    LaunchedEffect(state) {
        if (state.first == "success") {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FB))
            .statusBarsPadding()
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1976D2)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Inside
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {

            // Highlighted Thought
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💭", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = randomQuote,
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1),
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Verify OTP",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1C)
            )

            Spacer(modifier = Modifier.height(4.dp)) // reduced spacing

            Text(
                text = "Enter the 6-digit code",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1C)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                otpValues.forEachIndexed { index, value ->

                    OutlinedTextField(
                        value = value,
                        onValueChange = { input ->
                            if (input.length <= 1 && input.all { it.isDigit() }) {
                                otpValues[index] = input

                                if (input.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }

                            if (input.isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        },
                        modifier = Modifier
                            .width(48.dp)
                            .focusRequester(focusRequesters[index]),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            color = Color.Black
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val otp = otpValues.joinToString("")
                    if (otp.length == 6) {
                        viewModel.verifyOtp(otp)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {

                if (isLoading) {
                    ECGButtonLoader() // 🔥 same loader as login
                } else {
                    Text(
                        text = "Verify & Continue",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Note: Resend OTP functionality can be added back here if implemented in the ViewModel

            if (state.first == "error") {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.second ?: "An unknown error occurred",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}