package app.schedula.ui.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun RateAppDialog(
    onDismiss: () -> Unit,
    onRateClick: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onRateClick()
            }) {
                Text("Review on Google")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe later")
            }
        },
        title = { Text("Enjoying Schedula?") },
        text = {
            Text("Your feedback helps us improve. Would you mind rating us?")
        }
    )
}