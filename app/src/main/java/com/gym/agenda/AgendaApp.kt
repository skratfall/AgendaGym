package com.gym.agenda

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AgendaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 🔧 Inicializar Timber para logging
        if (BuildConfig.DEBUG) {
            // En desarrollo: mostrar logs con formato legible
            Timber.plant(Timber.DebugTree())
        } else {
            // En producción: solo logs de errores críticos
            Timber.plant(CrashReportingTree())
        }

        Timber.i("🚀 App iniciado - Versión: ${BuildConfig.VERSION_NAME}")
    }
}

/**
 * Árbol personalizado para Timber que solo registra errores críticos en producción
 * Útil para evitar llenar la consola en dispositivos de usuarios
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Solo registrar errores y advertencias en producción
        if (priority >= Log.WARN) {
            // Aquí podrías enviar a un servicio de crash reporting como Crashlytics
            if (t != null) {
                // Log.e("CrashReport", message, t)
            }
        }
    }
}
