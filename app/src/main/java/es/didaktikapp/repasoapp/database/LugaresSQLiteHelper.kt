package es.didaktikapp.repasoapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import es.didaktikapp.repasoapp.models.Categoria
import es.didaktikapp.repasoapp.models.Lugar

/**
 * Helper para gestionar la base de datos SQLite de lugares favoritos.
 *
 * Esta clase extiende SQLiteOpenHelper y proporciona métodos CRUD
 * para gestionar la tabla de lugares. Incluye migración segura de
 * datos entre versiones de la base de datos.
 *
 * @property context Contexto de la aplicación
 *
 * @constructor Crea una instancia del helper de base de datos
 * @param context Contexto de la aplicación Android
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 1.0
 *
 * @see Lugar
 * @see SQLiteOpenHelper
 */
class LugaresSQLiteHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "lugares.db"
        private const val DATABASE_VERSION = 3  // Incrementado para migración de categorías a códigos
        private const val TABLE_NAME = "lugares"

        // Columnas de la tabla (existentes)
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_DESCRIPCION = "descripcion"
        private const val COLUMN_LATITUD = "latitud"
        private const val COLUMN_LONGITUD = "longitud"
        private const val COLUMN_CATEGORIA = "categoria"
        private const val COLUMN_FECHA = "fecha_creacion"

        // Nuevas columnas (versión 2)
        private const val COLUMN_RATING = "rating"
        private const val COLUMN_ES_FAVORITO = "es_favorito"
        private const val COLUMN_TIPO_COCINA = "tipo_cocina"
    }

    // SQL para crear la tabla (con nuevas columnas)
    private val sqlCreate = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NOMBRE TEXT NOT NULL,
            $COLUMN_DESCRIPCION TEXT,
            $COLUMN_LATITUD REAL NOT NULL,
            $COLUMN_LONGITUD REAL NOT NULL,
            $COLUMN_CATEGORIA TEXT NOT NULL,
            $COLUMN_FECHA INTEGER NOT NULL,
            $COLUMN_RATING REAL DEFAULT 0.0,
            $COLUMN_ES_FAVORITO INTEGER DEFAULT 0,
            $COLUMN_TIPO_COCINA TEXT DEFAULT ''
        )
    """.trimIndent()

    private val sqlDelete = "DROP TABLE IF EXISTS $TABLE_NAME"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(sqlCreate)
        Log.i("SQLiteHelper", "Tabla $TABLE_NAME creada correctamente")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.w("SQLiteHelper", "Actualizando BD de versión $oldVersion a $newVersion")

        when (oldVersion) {
            1 -> {
                migrarVersion1a2(db)
                if (newVersion > 2) migrarVersion2a3(db)
            }
            2 -> migrarVersion2a3(db)
            // Futuras migraciones aquí
        }
    }

    /**
     * Migración segura de versión 1 a versión 2
     * Preserva todos los datos existentes y agrega nuevas columnas con valores por defecto
     */
    private fun migrarVersion1a2(db: SQLiteDatabase?) {
        db?.let {
            try {
                Log.i("SQLiteHelper", "Iniciando migración de versión 1 a 2...")

                // 1. Renombrar tabla antigua
                it.execSQL("ALTER TABLE $TABLE_NAME RENAME TO ${TABLE_NAME}_old")
                Log.i("SQLiteHelper", "Tabla renombrada a ${TABLE_NAME}_old")

                // 2. Crear nueva tabla con estructura actualizada
                it.execSQL(sqlCreate)
                Log.i("SQLiteHelper", "Nueva tabla creada")

                // 3. Copiar datos existentes
                it.execSQL("""
                    INSERT INTO $TABLE_NAME (
                        $COLUMN_ID, $COLUMN_NOMBRE, $COLUMN_DESCRIPCION,
                        $COLUMN_LATITUD, $COLUMN_LONGITUD, $COLUMN_CATEGORIA,
                        $COLUMN_FECHA
                    )
                    SELECT
                        id, nombre, descripcion,
                        latitud, longitud, categoria,
                        fecha_creacion
                    FROM ${TABLE_NAME}_old
                """)
                Log.i("SQLiteHelper", "Datos copiados")

                // 4. Eliminar tabla antigua
                it.execSQL("DROP TABLE ${TABLE_NAME}_old")
                Log.i("SQLiteHelper", "Tabla antigua eliminada")

                // 5. Asignar valores por defecto inteligentes
                asignarValoresPorDefecto(it)

                Log.i("SQLiteHelper", "Migración completada exitosamente")
            } catch (e: Exception) {
                Log.e("SQLiteHelper", "Error en migración: ${e.message}")
                // Intentar rollback
                try {
                    it.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
                    it.execSQL("ALTER TABLE ${TABLE_NAME}_old RENAME TO $TABLE_NAME")
                    Log.w("SQLiteHelper", "Rollback completado")
                } catch (rollbackException: Exception) {
                    Log.e("SQLiteHelper", "Error en rollback: ${rollbackException.message}")
                }
            }
        }
    }

    /**
     * Migración de versión 2 a versión 3
     * Convierte las categorías de strings traducidos a códigos del enum
     */
    private fun migrarVersion2a3(db: SQLiteDatabase?) {
        db?.let {
            try {
                Log.i("SQLiteHelper", "Iniciando migración de versión 2 a 3 (categorías a códigos)...")

                // Obtener todas las categorías únicas actuales
                val cursor = it.rawQuery("SELECT DISTINCT $COLUMN_CATEGORIA FROM $TABLE_NAME", null)
                val categoriasAConvertir = mutableSetOf<String>()

                cursor.use { c ->
                    while (c.moveToNext()) {
                        val categoria = c.getString(0)
                        if (categoria != null) {
                            categoriasAConvertir.add(categoria)
                        }
                    }
                }

                // Convertir cada categoría string a su código correspondiente
                categoriasAConvertir.forEach { categoriaString ->
                    val categoriaEnum = Categoria.fromString(categoriaString)
                    it.execSQL(
                        "UPDATE $TABLE_NAME SET $COLUMN_CATEGORIA = ? WHERE $COLUMN_CATEGORIA = ?",
                        arrayOf(categoriaEnum.code, categoriaString)
                    )
                    Log.i("SQLiteHelper", "Convertida categoría '$categoriaString' a '${categoriaEnum.code}'")
                }

                Log.i("SQLiteHelper", "Migración de categorías completada exitosamente")
            } catch (e: Exception) {
                Log.e("SQLiteHelper", "Error en migración v2 a v3: ${e.message}")
            }
        }
    }

    /**
     * Asigna valores por defecto inteligentes a los nuevos campos después de la migración
     */
    private fun asignarValoresPorDefecto(db: SQLiteDatabase) {
        // Asignar ratings aleatorios entre 3.5 y 5.0
        // Asignar tipo_cocina basado en categoria
        // Asignar horarios por defecto 09:00-22:00

        val categoriaToTipoCocina = mapOf(
            "Restaurant" to "Especialidad",
            "Café" to "Cafetería",
            "Bar" to "Tapas",
            "Pizzeria" to "Italiano",
            "Fast Food" to "Americana",
            "Bakery" to "Panadería"
        )

        try {
            categoriaToTipoCocina.forEach { (categoria, tipoCocina) ->
                // Generar rating aleatorio entre 3.5 y 5.0 usando RANDOM()
                db.execSQL("""
                    UPDATE $TABLE_NAME
                    SET $COLUMN_TIPO_COCINA = ?,
                        $COLUMN_RATING = (ABS(RANDOM() % 16) + 35) / 10.0
                    WHERE $COLUMN_CATEGORIA = ?
                """, arrayOf(tipoCocina, categoria))
            }

            // Para categorías no mapeadas, asignar valores por defecto
            db.execSQL("""
                UPDATE $TABLE_NAME
                SET $COLUMN_TIPO_COCINA = $COLUMN_CATEGORIA,
                    $COLUMN_RATING = (ABS(RANDOM() % 16) + 35) / 10.0
                WHERE $COLUMN_TIPO_COCINA = '' OR $COLUMN_TIPO_COCINA IS NULL
            """)

            Log.i("SQLiteHelper", "Valores por defecto asignados")
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error asignando valores por defecto: ${e.message}")
        }
    }

    /**
     * Inserta un nuevo lugar en la base de datos
     * @return ID del lugar insertado, o -1 si hay error
     */
    fun insertarLugar(lugar: Lugar): Long {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_NOMBRE, lugar.nombre)
                put(COLUMN_DESCRIPCION, lugar.descripcion)
                put(COLUMN_LATITUD, lugar.latitud)
                put(COLUMN_LONGITUD, lugar.longitud)
                put(COLUMN_CATEGORIA, lugar.categoria)
                put(COLUMN_FECHA, lugar.fechaCreacion)
                // Nuevos campos
                put(COLUMN_RATING, lugar.rating)
                put(COLUMN_ES_FAVORITO, if (lugar.esFavorito) 1 else 0)
                put(COLUMN_TIPO_COCINA, lugar.tipoCocina)
            }
            val id = db.insert(TABLE_NAME, null, values)
            Log.i("SQLiteHelper", "Lugar insertado con ID: $id")
            id
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error al insertar lugar: ${e.message}")
            -1
        } finally {
            db.close()
        }
    }

    /**
     * Consulta todos los lugares ordenados por fecha de creación (más recientes primero)
     * @return ArrayList de lugares
     */
    fun consultarTodos(): ArrayList<Lugar> {
        val lugares = ArrayList<Lugar>()
        val db = readableDatabase
        try {
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_FECHA DESC",
                null
            )
            cursor.use {
                while (it.moveToNext()) {
                    val lugar = Lugar(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                        nombre = it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                        descripcion = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPCION)) ?: "",
                        latitud = it.getDouble(it.getColumnIndexOrThrow(COLUMN_LATITUD)),
                        longitud = it.getDouble(it.getColumnIndexOrThrow(COLUMN_LONGITUD)),
                        categoria = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORIA)),
                        fechaCreacion = it.getLong(it.getColumnIndexOrThrow(COLUMN_FECHA)),
                        // Nuevos campos
                        rating = it.getFloat(it.getColumnIndexOrThrow(COLUMN_RATING)),
                        esFavorito = it.getInt(it.getColumnIndexOrThrow(COLUMN_ES_FAVORITO)) == 1,
                        tipoCocina = it.getString(it.getColumnIndexOrThrow(COLUMN_TIPO_COCINA)) ?: ""
                    )
                    lugares.add(lugar)
                }
            }
            Log.i("SQLiteHelper", "Consultados ${lugares.size} lugares")
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error al consultar lugares: ${e.message}")
        } finally {
            db.close()
        }
        return lugares
    }

    /**
     * Consulta un lugar por su ID
     * @return Lugar si existe, null si no
     */
    fun consultarPorId(id: Int): Lugar? {
        val db = readableDatabase
        var lugar: Lugar? = null
        try {
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?",
                arrayOf(id.toString())
            )
            cursor.use {
                if (it.moveToFirst()) {
                    lugar = Lugar(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                        nombre = it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                        descripcion = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPCION)) ?: "",
                        latitud = it.getDouble(it.getColumnIndexOrThrow(COLUMN_LATITUD)),
                        longitud = it.getDouble(it.getColumnIndexOrThrow(COLUMN_LONGITUD)),
                        categoria = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORIA)),
                        fechaCreacion = it.getLong(it.getColumnIndexOrThrow(COLUMN_FECHA)),
                        // Nuevos campos
                        rating = it.getFloat(it.getColumnIndexOrThrow(COLUMN_RATING)),
                        esFavorito = it.getInt(it.getColumnIndexOrThrow(COLUMN_ES_FAVORITO)) == 1,
                        tipoCocina = it.getString(it.getColumnIndexOrThrow(COLUMN_TIPO_COCINA)) ?: ""
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error al consultar lugar por ID: ${e.message}")
        } finally {
            db.close()
        }
        return lugar
    }

    /**
     * Actualiza un lugar existente
     * @return Número de filas afectadas (1 si éxito, 0 si fallo)
     */
    fun actualizarLugar(lugar: Lugar): Int {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_NOMBRE, lugar.nombre)
                put(COLUMN_DESCRIPCION, lugar.descripcion)
                put(COLUMN_LATITUD, lugar.latitud)
                put(COLUMN_LONGITUD, lugar.longitud)
                put(COLUMN_CATEGORIA, lugar.categoria)
                put(COLUMN_FECHA, lugar.fechaCreacion)
                // Nuevos campos
                put(COLUMN_RATING, lugar.rating)
                put(COLUMN_ES_FAVORITO, if (lugar.esFavorito) 1 else 0)
                put(COLUMN_TIPO_COCINA, lugar.tipoCocina)
            }
            val filasAfectadas = db.update(
                TABLE_NAME,
                values,
                "$COLUMN_ID = ?",
                arrayOf(lugar.id.toString())
            )
            Log.i("SQLiteHelper", "Lugar actualizado: $filasAfectadas fila(s)")
            filasAfectadas
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error al actualizar lugar: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    /**
     * Elimina un lugar por su ID
     * @return Número de filas eliminadas (1 si éxito, 0 si fallo)
     */
    fun eliminarLugar(id: Int): Int {
        val db = writableDatabase
        return try {
            val filasEliminadas = db.delete(
                TABLE_NAME,
                "$COLUMN_ID = ?",
                arrayOf(id.toString())
            )
            Log.i("SQLiteHelper", "Lugar eliminado: $filasEliminadas fila(s)")
            filasEliminadas
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error al eliminar lugar: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    /**
     * Verifica si existe un lugar con el ID especificado
     * @return true si existe, false si no
     */
    fun existe(id: Int): Boolean {
        val db = readableDatabase
        var existe = false
        try {
            val cursor = db.rawQuery(
                "SELECT 1 FROM $TABLE_NAME WHERE $COLUMN_ID = ? LIMIT 1",
                arrayOf(id.toString())
            )
            cursor.use {
                existe = it.count > 0
            }
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error al verificar existencia: ${e.message}")
        } finally {
            db.close()
        }
        return existe
    }

    /**
     * Consulta lugares por categoría
     * @return ArrayList de lugares de la categoría especificada
     */
    fun consultarPorCategoria(categoria: String): ArrayList<Lugar> {
        val lugares = ArrayList<Lugar>()
        val db = readableDatabase
        try {
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE $COLUMN_CATEGORIA = ? ORDER BY $COLUMN_FECHA DESC",
                arrayOf(categoria)
            )
            cursor.use {
                while (it.moveToNext()) {
                    val lugar = Lugar(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                        nombre = it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                        descripcion = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPCION)) ?: "",
                        latitud = it.getDouble(it.getColumnIndexOrThrow(COLUMN_LATITUD)),
                        longitud = it.getDouble(it.getColumnIndexOrThrow(COLUMN_LONGITUD)),
                        categoria = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORIA)),
                        fechaCreacion = it.getLong(it.getColumnIndexOrThrow(COLUMN_FECHA)),
                        // Nuevos campos
                        rating = it.getFloat(it.getColumnIndexOrThrow(COLUMN_RATING)),
                        esFavorito = it.getInt(it.getColumnIndexOrThrow(COLUMN_ES_FAVORITO)) == 1,
                        tipoCocina = it.getString(it.getColumnIndexOrThrow(COLUMN_TIPO_COCINA)) ?: ""
                    )
                    lugares.add(lugar)
                }
            }
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error al consultar por categoría: ${e.message}")
        } finally {
            db.close()
        }
        return lugares
    }

    /**
     * Elimina todos los lugares (útil para testing)
     * @return Número de filas eliminadas
     */
    fun eliminarTodos(): Int {
        val db = writableDatabase
        return try {
            val filasEliminadas = db.delete(TABLE_NAME, null, null)
            Log.i("SQLiteHelper", "Todos los lugares eliminados: $filasEliminadas fila(s)")
            filasEliminadas
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error al eliminar todos: ${e.message}")
            0
        } finally {
            db.close()
        }
    }
}