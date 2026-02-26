package app.schedula

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.schedula.ui.navigation.AppNavigation
import app.schedula.ui.theme.SchedulaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SchedulaTheme {
                AppNavigation()
            }
        }
    }
}