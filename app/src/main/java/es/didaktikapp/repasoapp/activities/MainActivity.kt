package es.didaktikapp.repasoapp.activities

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import es.didaktikapp.repasoapp.R
import es.didaktikapp.repasoapp.adapters.LugarAdapter
import es.didaktikapp.repasoapp.database.LugaresSQLiteHelper
import es.didaktikapp.repasoapp.databinding.ActivityMainBinding
import es.didaktikapp.repasoapp.models.Lugar
import es.didaktikapp.repasoapp.utils.ExportUtils

/**
 * Actividad principal que muestra la lista de lugares favoritos.
 *
 * Esta es la pantalla principal de la aplicación que presenta un RecyclerView
 * con todos los lugares guardados. Proporciona funcionalidad completa de CRUD
 * y permite filtrar por favoritos, compartir lugares y exportar datos.
 *
 * Características:
 * - Visualización de lugares en cards con diseño moderno
 * - Filtrado de favoritos
 * - Navegación al mapa con todos los lugares
 * - Exportación de datos a JSON
 * - Compartir lugares mediante Intent
 * - Toggle de estado favorito
 * - Navegación al formulario para agregar/editar lugares
 *
 * @property binding ViewBinding para acceso a vistas
 * @property dbHelper Helper de base de datos SQLite
 * @property adapter Adapter del RecyclerView
 * @property lugares Lista de lugares cargados
 * @property mostrarSoloFavoritos Flag para filtrar solo favoritos
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 1.0
 *
 * @see ActivityWithMenus
 * @see Lugar
 * @see LugarAdapter
 * @see LugaresSQLiteHelper
 */
class MainActivity : ActivityWithMenus() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: LugaresSQLiteHelper
    private lateinit var adapter: LugarAdapter
    private var lugares = ArrayList<Lugar>()
    private var mostrarSoloFavoritos = false

    companion object {
        const val EXTRA_LUGAR = "extra_lugar"
        const val REQUEST_CODE_ADD = 100
        const val REQUEST_CODE_EDIT = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar tema guardado
        ActivityWithMenus.applyTheme(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar toolbar
        setSupportActionBar(binding.toolbar)
        setupToolbarWithIcon()

        // Inicializar base de datos
        dbHelper = LugaresSQLiteHelper(this)

        // Verificar si se debe mostrar favoritos
        mostrarSoloFavoritos = intent.getBooleanExtra("mostrar_favoritos", false)

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar datos
        cargarLugares()

        // Configurar FAB
        binding.fabAddLugar.setOnClickListener {
            abrirFormulario(null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favoritos -> {
                mostrarSoloFavoritos = !mostrarSoloFavoritos
                cargarLugares()
                // Actualizar icono
                item.setIcon(
                    if (mostrarSoloFavoritos) R.drawable.ic_favorite_filled
                    else R.drawable.ic_favorite_border
                )
                true
            }
            R.id.action_view_map -> {
                abrirMapa()
                true
            }
            R.id.action_export -> {
                exportarDatos()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = LugarAdapter(
            lugares = lugares,
            onFavoritoClick = { lugar -> toggleFavorito(lugar) },
            onVerMapaClick = { lugar -> abrirMapaConLugar(lugar) },
            onCompartirClick = { lugar -> compartirLugar(lugar) },
            onItemClick = { lugar -> abrirFormulario(lugar) },
            onItemLongClick = { lugar -> confirmarEliminarLugar(lugar) }
        )

        binding.recyclerViewLugares.apply {
            // Usar grid de 2 columnas en landscape, lista en portrait
            layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                GridLayoutManager(this@MainActivity, 2)
            } else {
                LinearLayoutManager(this@MainActivity)
            }
            adapter = this@MainActivity.adapter
        }
    }

    private fun cargarLugares() {
        val todosLosLugares = dbHelper.consultarTodos()

        lugares = if (mostrarSoloFavoritos) {
            ArrayList(todosLosLugares.filter { it.esFavorito })
        } else {
            todosLosLugares
        }

        adapter.actualizarLista(lugares)
        actualizarEmptyState()
    }

    private fun actualizarEmptyState() {
        if (lugares.isEmpty()) {
            binding.recyclerViewLugares.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewLugares.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    private fun abrirFormulario(lugar: Lugar?) {
        val intent = Intent(this, FormActivity::class.java)
        if (lugar != null) {
            intent.putExtra(EXTRA_LUGAR, lugar)
            startActivityForResult(intent, REQUEST_CODE_EDIT)
        } else {
            startActivityForResult(intent, REQUEST_CODE_ADD)
        }
    }

    /**
     * Abre el mapa con todos los lugares
     */
    private fun abrirMapa() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    /**
     * Abre el mapa centrado en un lugar específico
     */
    private fun abrirMapaConLugar(lugar: Lugar) {
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra(MapActivity.EXTRA_LUGAR_ID, lugar.id)
        }
        startActivity(intent)
    }

    /**
     * Toggle del estado de favorito de un lugar
     */
    private fun toggleFavorito(lugar: Lugar) {
        val nuevoEstado = !lugar.esFavorito
        val lugarActualizado = lugar.copy(esFavorito = nuevoEstado)

        val resultado = dbHelper.actualizarLugar(lugarActualizado)
        if (resultado > 0) {
            adapter.actualizarFavorito(lugar.id, nuevoEstado)

            val mensaje = if (nuevoEstado) {
                R.string.added_to_favorites
            } else {
                R.string.removed_from_favorites
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

            // Si estamos en vista de favoritos y se desfavorea, recargar
            if (mostrarSoloFavoritos && !nuevoEstado) {
                cargarLugares()
            }
        }
    }

    /**
     * Comparte la información de un lugar
     */
    private fun compartirLugar(lugar: Lugar) {
        val mensaje = """
            ${lugar.nombre}
            ${getString(R.string.rating)}: ${String.format("%.1f", lugar.rating)} ⭐
            ${lugar.tipoCocina.ifBlank { lugar.categoria }}

            📍 ${lugar.latitud}, ${lugar.longitud}

            ${getString(R.string.share_via)} TastyPlaces
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, lugar.nombre)
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar un lugar
     */
    private fun confirmarEliminarLugar(lugar: Lugar) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.main_delete_confirm))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                eliminarLugar(lugar)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * Elimina un lugar de la base de datos y actualiza la lista
     */
    private fun eliminarLugar(lugar: Lugar) {
        val resultado = dbHelper.eliminarLugar(lugar.id)
        if (resultado > 0) {
            Toast.makeText(this, getString(R.string.main_deleted), Toast.LENGTH_SHORT).show()
            cargarLugares() // Recargar la lista
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportarDatos() {
        if (lugares.isEmpty()) {
            Toast.makeText(this, R.string.main_empty_list, Toast.LENGTH_SHORT).show()
            return
        }

        val resultado = ExportUtils.exportarAJSON(this, lugares)
        if (resultado) {
            Toast.makeText(this, R.string.export_success, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.export_error, Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            cargarLugares()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarLugares()
    }
}