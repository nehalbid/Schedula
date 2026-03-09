package app.schedula.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.schedula.R
import app.schedula.utils.findActivity
import kotlin.io.path.moveTo

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onCodeSent: () -> Unit
) {

    val state by viewModel.authState.collectAsState()
    var phone by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context.findActivity()

    LaunchedEffect(state) {

        when (state.first) {

            "code_sent" -> {
                onCodeSent()
            }

            "patient_success" -> {
                // navigate to patient home
            }

            "doctor_success" -> {
                // navigate to doctor panel
            }

            "admin_success" -> {
                // navigate to admin dashboard
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FB))
            .statusBarsPadding()
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentScale = ContentScale.Inside
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your Healthcare,\nSimplified.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Book appointments with top-rated specialists in seconds. Enter your mobile number to get started.",
                color = Color(0xFF444444),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Mobile Number",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = "+91",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.width(80.dp),
                    shape = RoundedCornerShape(14.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                            phone = it
                        }
                    },
                    placeholder = {
                        Text("Enter your phone number", color = Color.Gray)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isLoading = state.first == "loading"

            Button(
                onClick = {
                    if (phone.length == 10) {
                        activity?.let {
                            viewModel.sendOtp(it, phone)
                        }
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
                    ECGButtonLoader()
                } else {
                    Text(
                        text = "Sign-up / Sign-in",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.first == "error") {
                Text(
                    text = state.second ?: "An unknown error occurred",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ECGButtonLoader() {

    val infiniteTransition = rememberInfiniteTransition(label = "")

    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing)
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {

        // ❤️ Center Heart
        Text(
            text = "❤",
            fontSize = 22.sp,
            color = Color(0xFFD32F2F)
        )

        // 📈 ECG Line (drawn ABOVE heart)
        Canvas(
            modifier = Modifier
                .matchParentSize()
        ) { val midY = size.height / 2
            val startX = -shift % size.width

            var x = startX

            while (x < size.width) {

                val path = Path().apply {
                    moveTo(x, midY)

                    lineTo(x + 20f, midY)

                    // sharp spike
                    lineTo(x + 30f, midY - 25f)
                    lineTo(x + 40f, midY + 15f)
                    lineTo(x + 55f, midY)

                    // small bump
                    lineTo(x + 65f, midY - 10f)
                    lineTo(x + 75f, midY)

                    lineTo(x + 120f, midY)
                }

                drawPath(
                    path = path,
                    color = Color(0xFFFF1744),
                    style = Stroke(width = 4f)
                )

                x += 140f
            }
        }
    }
}
