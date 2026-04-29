package com.gym.agenda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gym.agenda.di.navigation.GymNavHost
import com.gym.agenda.ui.theme.GymAgendaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita modo edge-to-edge para Android 14+ (barras transparentes)
        enableEdgeToEdge()

        setContent {
            GymAgendaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Controlador de navegación único para toda la app
                    val navController = rememberNavController()

                    // Inicia el sistema de navegación con roles (Admin/Usuario)
                    GymNavHost(navController = navController)
                }
            }
        }
    }
}