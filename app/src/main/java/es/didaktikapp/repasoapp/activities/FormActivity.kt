package es.didaktikapp.repasoapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import es.didaktikapp.repasoapp.R
import es.didaktikapp.repasoapp.database.LugaresSQLiteHelper
import es.didaktikapp.repasoapp.databinding.ActivityFormBinding
import es.didaktikapp.repasoapp.models.Categoria
import es.didaktikapp.repasoapp.models.Lugar

/**
 * Actividad para crear y editar lugares.
 *
 * Formulario completo para agregar nuevos lugares o editar existentes.
 * Incluye validación de campos, gestión de permisos de ubicación y
 * obtención de coordenadas GPS actuales.
 *
 * Características:
 * - Modo creación y modo edición
 * - Validación de campos obligatorios
 * - Solicitud de permisos de ubicación
 * - Obtención automática de coordenadas GPS
 * - Guardado de estado al rotar pantalla
 * - Spinners para categoría y tipo de cocina
 * - RatingBar para valoración
 *
 * @property binding ViewBinding para acceso a vistas
 * @property dbHelper Helper de base de datos SQLite
 * @property fusedLocationClient Cliente para servicios de ubicación
 * @property lugarAEditar Lugar a editar (null si es creación)
 * @property esEdicion Flag indicando si es modo edición
 *
 * @author
 * @version 2.0
 * @since 1.0
 *
 * @see ActivityWithMenus
 * @see Lugar
 * @see LugaresSQLiteHelper
 */
class FormActivity : ActivityWithMenus() {

    private lateinit var binding: ActivityFormBinding
    private lateinit var dbHelper: LugaresSQLiteHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lugarAEditar: Lugar? = null
    private var esEdicion = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar toolbar (solo con botón de cerrar, sin menú)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inicializar base de datos
        dbHelper = LugaresSQLiteHelper(this)

        // Inicializar cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar spinners
        configurarSpinners()

        // Verificar si es edición
        verificarModoEdicion()

        // Configurar listeners
        configurarListeners()
    }

    /**
     * FormActivity muestra el menú heredado de ActivityWithMenus
     */
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Oculta items del menú que no tienen sentido en el formulario
     */
    override fun onPrepareOptionsMenu(menu: android.view.Menu?): Boolean {
        // Ocultar favoritos, mapa y exportar en el formulario
        menu?.findItem(R.id.action_favoritos)?.isVisible = false
        menu?.findItem(R.id.action_view_map)?.isVisible = false
        menu?.findItem(R.id.action_export)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    private fun configurarSpinners() {
        // Spinner de categorías
        val adapterCategoria = ArrayAdapter.createFromResource(
            this,
            R.array.categorias,
            android.R.layout.simple_spinner_item
        )
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapterCategoria

        // Spinner de tipos de cocina
        val adapterTipoCocina = ArrayAdapter.createFromResource(
            this,
            R.array.tipos_cocina,
            android.R.layout.simple_spinner_item
        )
        adapterTipoCocina.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipoCocina.adapter = adapterTipoCocina
    }

    private fun verificarModoEdicion() {
        lugarAEditar = intent.getSerializableExtra(MainActivity.EXTRA_LUGAR) as? Lugar

        if (lugarAEditar != null) {
            esEdicion = true
            binding.toolbar.title = getString(R.string.form_title_edit)
            cargarDatosLugar(lugarAEditar!!)
        } else {
            binding.toolbar.title = getString(R.string.form_title_add)

            // Verificar si vienen coordenadas del mapa
            cargarCoordenadasDesdeMapa()
        }
    }

    /**
     * Carga las coordenadas si vienen del long click en el mapa
     */
    private fun cargarCoordenadasDesdeMapa() {
        val latitud = intent.getDoubleExtra(MapActivity.EXTRA_LATITUDE, Double.NaN)
        val longitud = intent.getDoubleExtra(MapActivity.EXTRA_LONGITUDE, Double.NaN)

        if (!latitud.isNaN() && !longitud.isNaN()) {
            binding.etLatitud.setText(String.format("%.6f", latitud))
            binding.etLongitud.setText(String.format("%.6f", longitud))

            Toast.makeText(
                this,
                R.string.map_coordinates_loaded,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun cargarDatosLugar(lugar: Lugar) {
        binding.etNombre.setText(lugar.nombre)
        binding.etDescripcion.setText(lugar.descripcion)
        binding.etLatitud.setText(lugar.latitud.toString())
        binding.etLongitud.setText(lugar.longitud.toString())

        // Seleccionar categoría en el spinner usando el enum
        val categoriaEnum = lugar.getCategoriaEnum()
        val posicion = categoriaEnum.ordinal
        binding.spinnerCategoria.setSelection(posicion)

        // Cargar rating
        binding.ratingBar.rating = lugar.rating

        // Seleccionar tipo de cocina
        if (lugar.tipoCocina.isNotBlank()) {
            val tiposCocina = resources.getStringArray(R.array.tipos_cocina)
            val posicionTipo = tiposCocina.indexOf(lugar.tipoCocina)
            if (posicionTipo >= 0) {
                binding.spinnerTipoCocina.setSelection(posicionTipo)
            }
        }
    }

    private fun configurarListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }

        binding.btnGuardar.setOnClickListener {
            guardarLugar()
        }

        binding.btnUsarUbicacion.setOnClickListener {
            obtenerUbicacionActual()
        }
    }

    private fun validarCampos(): Boolean {
        var esValido = true

        // Validar nombre
        val nombre = binding.etNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            binding.tilNombre.error = getString(R.string.form_error_name)
            esValido = false
        } else {
            binding.tilNombre.error = null
        }

        // Validar latitud
        val latitudStr = binding.etLatitud.text.toString().trim().replace(',', '.')
        if (latitudStr.isEmpty()) {
            binding.tilLatitud.error = getString(R.string.form_error_coords)
            esValido = false
        } else {
            try {
                val latitud = latitudStr.toDouble()
                if (latitud < -90 || latitud > 90) {
                    binding.tilLatitud.error = getString(R.string.error_latitude_range)
                    esValido = false
                } else {
                    binding.tilLatitud.error = null
                }
            } catch (e: NumberFormatException) {
                binding.tilLatitud.error = getString(R.string.form_error_coords)
                esValido = false
            }
        }

        // Validar longitud
        val longitudStr = binding.etLongitud.text.toString().trim().replace(',', '.')
        if (longitudStr.isEmpty()) {
            binding.tilLongitud.error = getString(R.string.form_error_coords)
            esValido = false
        } else {
            try {
                val longitud = longitudStr.toDouble()
                if (longitud < -180 || longitud > 180) {
                    binding.tilLongitud.error = getString(R.string.error_longitude_range)
                    esValido = false
                } else {
                    binding.tilLongitud.error = null
                }
            } catch (e: NumberFormatException) {
                binding.tilLongitud.error = getString(R.string.form_error_coords)
                esValido = false
            }
        }

        return esValido
    }

    private fun guardarLugar() {
        if (!validarCampos()) {
            return
        }

        val nombre = binding.etNombre.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val latitud = binding.etLatitud.text.toString().replace(',', '.').toDouble()
        val longitud = binding.etLongitud.text.toString().replace(',', '.').toDouble()

        // Obtener el código de categoría desde el enum según la posición seleccionada
        val categoriaSeleccionada = Categoria.getAllCategories()[binding.spinnerCategoria.selectedItemPosition]
        val categoria = categoriaSeleccionada.code

        // Nuevos campos
        val rating = binding.ratingBar.rating
        val tipoCocina = binding.spinnerTipoCocina.selectedItem.toString()

        val resultado: Long

        if (esEdicion && lugarAEditar != null) {
            // Actualizar lugar existente
            val lugarActualizado = Lugar(
                id = lugarAEditar!!.id,
                nombre = nombre,
                descripcion = descripcion,
                latitud = latitud,
                longitud = longitud,
                categoria = categoria,
                fechaCreacion = lugarAEditar!!.fechaCreacion,
                // Nuevos campos
                rating = rating,
                esFavorito = lugarAEditar!!.esFavorito, // Mantener estado favorito
                tipoCocina = tipoCocina
            )
            resultado = dbHelper.actualizarLugar(lugarActualizado).toLong()
        } else {
            // Crear nuevo lugar
            val nuevoLugar = Lugar(
                nombre = nombre,
                descripcion = descripcion,
                latitud = latitud,
                longitud = longitud,
                categoria = categoria,
                // Nuevos campos
                rating = rating,
                esFavorito = false,
                tipoCocina = tipoCocina
            )
            resultado = dbHelper.insertarLugar(nuevoLugar)
        }

        if (resultado > 0 || (esEdicion && resultado >= 0)) {
            Toast.makeText(this, R.string.form_saved, Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, R.string.form_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerUbicacionActual() {
        // Verificar permisos
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permiso
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Obtener ubicación
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    binding.etLatitud.setText(String.format("%.6f", location.latitude))
                    binding.etLongitud.setText(String.format("%.6f", location.longitude))
                    Toast.makeText(
                        this,
                        R.string.location_obtained,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        R.string.error_location_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.error_location_exception, it.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual()
            } else {
                Toast.makeText(
                    this,
                    R.string.map_permission_required,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Guarda el estado del formulario al girar la pantalla
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("nombre", binding.etNombre.text.toString())
        outState.putString("descripcion", binding.etDescripcion.text.toString())
        outState.putInt("categoria", binding.spinnerCategoria.selectedItemPosition)
        outState.putInt("tipoCocina", binding.spinnerTipoCocina.selectedItemPosition)
        outState.putFloat("rating", binding.ratingBar.rating)
        outState.putString("latitud", binding.etLatitud.text.toString())
        outState.putString("longitud", binding.etLongitud.text.toString())
    }

    /**
     * Restaura el estado del formulario al girar la pantalla
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Solo restaurar si no estamos editando un lugar existente
        if (lugarAEditar == null) {
            binding.etNombre.setText(savedInstanceState.getString("nombre", ""))
            binding.etDescripcion.setText(savedInstanceState.getString("descripcion", ""))
            binding.spinnerCategoria.setSelection(savedInstanceState.getInt("categoria", 0))
            binding.spinnerTipoCocina.setSelection(savedInstanceState.getInt("tipoCocina", 0))
            binding.ratingBar.rating = savedInstanceState.getFloat("rating", 0f)
            binding.etLatitud.setText(savedInstanceState.getString("latitud", ""))
            binding.etLongitud.setText(savedInstanceState.getString("longitud", ""))
        }
    }
}