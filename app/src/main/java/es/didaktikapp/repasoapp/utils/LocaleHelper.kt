package es.didaktikapp.repasoapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Helper para gestionar el cambio de idioma en la aplicación.
 *
 * Proporciona funcionalidad para cambiar el idioma de la aplicación
 * de forma persistente, soportando español, inglés, euskera y el
 * idioma del sistema.
 *
 * Las preferencias de idioma se guardan en SharedPreferences y se
 * aplican automáticamente al iniciar cualquier actividad.
 *
 * Idiomas soportados:
 * - Sistema (por defecto del dispositivo)
 * - Español (es)
 * - English (en)
 * - Euskera (eu)
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 1.0
 *
 * @see Language
 */
object LocaleHelper {

    private const val PREFS_NAME = "app_preferences"
    private const val KEY_LANGUAGE = "selected_language"
    private const val DEFAULT_LANGUAGE = "system" // system, es, en, eu

    /**
     * Idiomas disponibles
     */
    enum class Language(val code: String, val displayName: String) {
        SYSTEM("system", "Sistema / System"),
        SPANISH("es", "Español"),
        ENGLISH("en", "English"),
        BASQUE("eu", "Euskera")
    }

    /**
     * Obtiene las preferencias
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Guarda el idioma seleccionado
     */
    fun saveLanguage(context: Context, languageCode: String) {
        getPreferences(context).edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    /**
     * Obtiene el idioma guardado
     */
    fun getSavedLanguage(context: Context): String {
        return getPreferences(context).getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    /**
     * Aplica el idioma a la configuración del contexto
     */
    fun applyLanguage(context: Context, languageCode: String = getSavedLanguage(context)): Context {
        // Determinar el Locale según el código de idioma
        val locale = if (languageCode == "system") {
            Locale.getDefault()  // Usar idioma del sistema
        } else {
            Locale(languageCode)  // Crear Locale específico (es, en, eu)
        }

        // Establecer como Locale por defecto de la app
        Locale.setDefault(locale)

        // Crear configuración con el nuevo Locale
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        // Aplicar según versión de Android
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // API 24+: crear nuevo contexto con configuración
            context.createConfigurationContext(config)
        } else {
            // API < 24: actualizar recursos directamente (método deprecado)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    /**
     * Obtiene el nombre del idioma actual
     */
    fun getCurrentLanguageName(context: Context): String {
        val code = getSavedLanguage(context)
        return Language.values().find { it.code == code }?.displayName ?: Language.SYSTEM.displayName
    }

    /**
     * Obtiene todos los idiomas disponibles
     */
    fun getAvailableLanguages(): Array<Language> {
        return Language.values()
    }
}