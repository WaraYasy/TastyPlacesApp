package es.didaktikapp.repasoapp.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import es.didaktikapp.repasoapp.R
import es.didaktikapp.repasoapp.models.Lugar
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidades para exportar e importar datos a/desde archivos JSON.
 *
 * Proporciona funcionalidad completa para exportar la lista de lugares
 * a un archivo JSON con formato legible, y para importar datos desde
 * archivos JSON previamente exportados.
 *
 * El formato de exportación incluye metadata como versión, fecha de
 * exportación y total de lugares, además de la lista completa de
 * lugares con todos sus campos.
 *
 * Características:
 * - Exportación a JSON con formato pretty-print
 * - Importación desde JSON con validación
 * - Gestión de archivos en almacenamiento interno
 * - Metadata de exportación (versión, fecha, total)
 * - Utilidades para verificar existencia de archivos
 * - Obtención de información de archivos exportados
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 1.0
 *
 * @see Lugar
 * @see Gson
 */
object ExportUtils {

    private const val TAG = "ExportUtils"

    /**
     * Exporta la lista de lugares a un archivo TXT en la carpeta Downloads
     * El archivo será visible y accesible desde el explorador de archivos del dispositivo
     *
     * @param context Contexto de la aplicación
     * @param lugares Lista de lugares a exportar
     * @return true si la exportación fue exitosa, false en caso contrario
     */
    fun exportarAJSON(context: Context, lugares: ArrayList<Lugar>): Boolean {
        return try {
            // Crear objeto JSON con metadata
            val exportData = mapOf(
                "version" to "1.0",
                "fecha_exportacion" to SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date()),
                "total_lugares" to lugares.size,
                "lugares" to lugares
            )

            // Convertir a JSON con formato legible
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(exportData)

            // Obtener carpeta de Downloads pública
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            // Crear carpeta si no existe
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            // Generar nombre de archivo con timestamp para evitar sobrescribir
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nombreArchivo = "lugares_export_$timestamp.txt"

            // Crear archivo en Downloads
            val file = File(downloadsDir, nombreArchivo)
            FileOutputStream(file).use { fos ->
                fos.write(jsonString.toByteArray())
            }

            Log.i(TAG, "✅ Archivo exportado exitosamente: ${file.absolutePath}")
            Log.i(TAG, "📁 Ubicación: Downloads/$nombreArchivo")
            Log.i(TAG, "📊 Total lugares exportados: ${lugares.size}")

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al exportar JSON: ${e.message}", e)
            false
        }
    }

    /**
     * Importa lugares desde un archivo JSON
     * @param context Contexto de la aplicación
     * @param nombreArchivo Nombre del archivo a importar
     * @return Lista de lugares importados, o null si hay error
     */
    fun importarDesdeJSON(context: Context, nombreArchivo: String): ArrayList<Lugar>? {
        return try {
            val file = File(context.filesDir, nombreArchivo)

            if (!file.exists()) {
                Log.e(TAG, "El archivo no existe: ${file.absolutePath}")
                return null
            }

            // Leer archivo
            val jsonString = FileInputStream(file).use { fis ->
                fis.bufferedReader().readText()
            }

            // Parsear JSON
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val exportData: Map<String, Any> = gson.fromJson(jsonString, type)

            // Extraer lista de lugares
            val lugaresJson = gson.toJson(exportData["lugares"])
            val lugaresType = object : TypeToken<ArrayList<Lugar>>() {}.type
            val lugares: ArrayList<Lugar> = gson.fromJson(lugaresJson, lugaresType)

            Log.i(TAG, "Importados ${lugares.size} lugares desde $nombreArchivo")

            lugares
        } catch (e: Exception) {
            Log.e(TAG, "Error al importar JSON: ${e.message}", e)
            null
        }
    }

    /**
     * Obtiene la ruta del archivo exportado
     * @param context Contexto de la aplicación
     * @return Ruta absoluta del archivo
     */
    fun obtenerRutaArchivoExportado(context: Context): String {
        val nombreArchivo = context.getString(R.string.export_filename)
        val file = File(context.filesDir, nombreArchivo)
        return file.absolutePath
    }

    /**
     * Verifica si existe un archivo exportado
     * @param context Contexto de la aplicación
     * @return true si existe el archivo, false en caso contrario
     */
    fun existeArchivoExportado(context: Context): Boolean {
        val nombreArchivo = context.getString(R.string.export_filename)
        val file = File(context.filesDir, nombreArchivo)
        return file.exists()
    }

    /**
     * Elimina el archivo exportado
     * @param context Contexto de la aplicación
     * @return true si se eliminó correctamente, false en caso contrario
     */
    fun eliminarArchivoExportado(context: Context): Boolean {
        return try {
            val nombreArchivo = context.getString(R.string.export_filename)
            val file = File(context.filesDir, nombreArchivo)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar archivo: ${e.message}", e)
            false
        }
    }

    /**
     * Obtiene información sobre el archivo exportado
     * @param context Contexto de la aplicación
     * @return Map con información del archivo (tamaño, fecha modificación, etc.)
     */
    fun obtenerInfoArchivoExportado(context: Context): Map<String, String>? {
        return try {
            val nombreArchivo = context.getString(R.string.export_filename)
            val file = File(context.filesDir, nombreArchivo)

            if (!file.exists()) {
                return null
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            mapOf(
                "nombre" to file.name,
                "ruta" to file.absolutePath,
                "tamaño" to "${file.length() / 1024} KB",
                "ultima_modificacion" to dateFormat.format(Date(file.lastModified()))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener info del archivo: ${e.message}", e)
            null
        }
    }
}