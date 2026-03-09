package app.schedula.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.schedula.data.model.AppointmentStatus

@Composable
fun StatusBadge(status: String) {

    val color = when (status) {
        AppointmentStatus.UPCOMING.name -> Color(0xFF1976D2)
        AppointmentStatus.CHECKED_IN.name -> Color(0xFFFF9800)
        AppointmentStatus.ONGOING.name -> Color(0xFF4CAF50)
        AppointmentStatus.COMPLETED.name -> Color.Gray
        AppointmentStatus.NO_SHOW.name -> Color.Red
        AppointmentStatus.CANCELLED.name -> Color.Red
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = status.replace("_", " "),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}