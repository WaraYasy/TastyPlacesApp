package es.didaktikapp.repasoapp.activities

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import es.didaktikapp.repasoapp.R
import es.didaktikapp.repasoapp.utils.LocaleHelper

/**
 * Actividad base abstracta con menú común para todas las actividades.
 *
 * Proporciona funcionalidad compartida de menú incluyendo cambio de idioma
 * y cambio de tema (modo claro/oscuro). Todas las actividades principales
 * de la app deben heredar de esta clase.
 *
 * Características:
 * - Aplicación automática del idioma seleccionado
 * - Menú con opción de cambio de idioma
 * - Menú con opción de cambio de tema
 * - Gestión persistente de preferencias de tema
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 1.0
 *
 * @see LocaleHelper
 * @see AppCompatActivity
 */
abstract class ActivityWithMenus : AppCompatActivity() {

    /**
     * Aplica el idioma seleccionado al contexto
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    /**
     * Infla el menú común para todas las actividades
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Configura el toolbar con el icono del helado a la izquierda
     */
    protected fun setupToolbarWithIcon() {
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_ice_cream)
        }
    }

    /**
     * Maneja las opciones del menú comunes a todas las actividades
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language -> {
                mostrarSelectorIdioma()
                true
            }
            R.id.action_theme_mode -> {
                mostrarSelectorTema()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Muestra el diálogo de selección de idioma
     */
    protected fun mostrarSelectorIdioma() {
        val languages = LocaleHelper.getAvailableLanguages()
        val languageNames = languages.map { it.displayName }.toTypedArray()
        val currentLanguage = LocaleHelper.getSavedLanguage(this)
        val currentIndex = languages.indexOfFirst { it.code == currentLanguage }

        AlertDialog.Builder(this)
            .setTitle(R.string.language_title)
            .setSingleChoiceItems(languageNames, currentIndex) { dialog, which ->
                val selectedLanguage = languages[which]
                LocaleHelper.saveLanguage(this, selectedLanguage.code)

                Toast.makeText(
                    this,
                    R.string.language_changed,
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()

                // Recrear la actividad para aplicar el nuevo idioma
                recreate()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Muestra el diálogo de selección de tema
     */
    private fun mostrarSelectorTema() {
        val prefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        val temas = arrayOf(
            getString(R.string.light_mode),
            getString(R.string.dark_mode)
        )

        val currentIndex = if (isDarkMode) 1 else 0

        AlertDialog.Builder(this)
            .setTitle(R.string.theme_mode)
            .setSingleChoiceItems(temas, currentIndex) { dialog, which ->
                val nuevoModoOscuro = (which == 1)

                // Guardar preferencia
                prefs.edit().putBoolean("dark_mode", nuevoModoOscuro).apply()

                // Aplicar nuevo modo
                if (nuevoModoOscuro) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        /**
         * Aplica el modo guardado al iniciar la app
         */
        fun applyTheme(context: Context) {
            val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val isDarkMode = prefs.getBoolean("dark_mode", false)

            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}
