package com.gym.agenda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gym.agenda.di.AppContainer
import com.gym.agenda.di.navigation.GymNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = AppContainer(this)
        setContent {
            GymAgendaTheme {
                GymNavHost(repository = container.repo)
            }
        }
    }
}

@Composable
fun GymAgendaTheme (content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) darkColorScheme(
        primary = Color(0xFF8C7AE6), secondary = Color(0xFF00E676),
        tertiary = Color(0xFFFFAB91), surface = Color(0xFF121212),
        onSurface = Color(0xFFE0E0E0)
    ) else lightColorScheme(
        primary = Color(0xFF6C63FF), secondary = Color(0xFF00BFA5),
        tertiary = Color(0xFFFF9800), surface = Color(0xFFFAFAFA),
        onSurface = Color(0xFF212121)
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}