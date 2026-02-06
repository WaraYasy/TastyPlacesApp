package es.didaktikapp.repasoapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import es.didaktikapp.repasoapp.R
import es.didaktikapp.repasoapp.database.LugaresSQLiteHelper
import es.didaktikapp.repasoapp.databinding.ActivityMapBinding
import es.didaktikapp.repasoapp.models.Lugar

/**
 * Actividad para mostrar lugares en el mapa de Google Maps.
 *
 * Visualiza todos los lugares guardados como marcadores en Google Maps,
 * con colores diferentes según la categoría. Permite interacción con
 * marcadores para ver detalles y navegar al formulario de edición.
 *
 * Características:
 * - Visualización de marcadores por categoría
 * - Colores personalizados según tipo de lugar
 * - Detección de ubicación actual del usuario
 * - Ajuste automático de cámara para mostrar todos los lugares
 * - InfoWindow con detalles al pulsar marcador
 * - Navegación a formulario de edición desde marcador
 * - FAB para agregar nuevo lugar desde ubicación actual
 * - Gestión de permisos de ubicación
 *
 * @property binding ViewBinding para acceso a vistas
 * @property mMap Instancia de GoogleMap
 * @property dbHelper Helper de base de datos SQLite
 * @property fusedLocationClient Cliente para servicios de ubicación
 * @property lugares Lista de lugares a mostrar en el mapa
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 1.0
 *
 * @see ActivityWithMenus
 * @see OnMapReadyCallback
 * @see Lugar
 * @see LugaresSQLiteHelper
 */
class MapActivity : ActivityWithMenus(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var mMap: GoogleMap
    private lateinit var dbHelper: LugaresSQLiteHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lugares = ArrayList<Lugar>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1002
        private const val DEFAULT_ZOOM = 12f
        private const val SINGLE_LUGAR_ZOOM = 15f
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_LUGAR_ID = "extra_lugar_id"

        // Ubicación por defecto: Araba Kalea, 39, 01006 Vitoria-Gasteiz, Araba
        private const val DEFAULT_LATITUDE = 42.837079
        private const val DEFAULT_LONGITUDE = -2.6771052
        private val DEFAULT_LOCATION = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Inicializar base de datos
        dbHelper = LugaresSQLiteHelper(this)

        // Inicializar cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Cargar lugares
        lugares = dbHelper.consultarTodos()

        // Inicializar mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar FAB
        binding.fabMyLocation.setOnClickListener {
            centrarEnMiUbicacionODefault()
        }
    }

    /**
     * Prepara el menú ocultando items que no tienen sentido en el mapa
     */
    override fun onPrepareOptionsMenu(menu: android.view.Menu?): Boolean {
        // Ocultar el botón de favoritos y el de mapa (ya estamos en el mapa)
        menu?.findItem(R.id.action_favoritos)?.isVisible = false
        menu?.findItem(R.id.action_view_map)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configurar mapa
        configurarMapa()

        // Mostrar marcadores de lugares
        mostrarLugares()

        // Solicitar permisos y mostrar ubicación
        habilitarMiUbicacion()

        // Configurar long click para crear lugar
        configurarLongClick()
    }

    private fun configurarMapa() {
        // Configurar UI del mapa
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false // Usamos nuestro FAB
            isMapToolbarEnabled = true
        }

        // Configurar InfoWindow personalizado
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null // Usa el frame predeterminado
            }

            override fun getInfoContents(marker: Marker): View {
                val view = LayoutInflater.from(this@MapActivity)
                    .inflate(R.layout.custom_info_window, null)

                view.findViewById<TextView>(R.id.tvTitle).text = marker.title
                view.findViewById<TextView>(R.id.tvSnippet).text = marker.snippet

                return view
            }
        })

        // Configurar click en InfoWindow para mostrar opciones
        mMap.setOnInfoWindowClickListener { marker ->
            mostrarOpcionesLugar(marker)
        }
    }

    private fun mostrarLugares() {
        if (lugares.isEmpty()) {
            // Si no hay lugares, centrar directamente en Vitoria-Gasteiz
            centrarEnUbicacionDefault()
            return
        }

        // Verificar si hay un lugar específico a mostrar
        val lugarIdAMostrar = intent.getIntExtra(EXTRA_LUGAR_ID, -1)
        var lugarSeleccionado: Lugar? = null

        val boundsBuilder = LatLngBounds.Builder()

        lugares.forEach { lugar ->
            val posicion = LatLng(lugar.latitud, lugar.longitud)
            val colorMarcador = obtenerColorPorCategoria(lugar.categoria)

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(posicion)
                    .title(lugar.nombre)
                    .snippet("${lugar.categoria} - ${lugar.descripcion}")
                    .icon(BitmapDescriptorFactory.defaultMarker(colorMarcador))
            )
            // Guardar el ID del lugar en el tag del marcador para poder identificarlo después
            marker?.tag = lugar.id
            boundsBuilder.include(posicion)

            // Identificar el lugar específico si existe
            if (lugar.id == lugarIdAMostrar) {
                lugarSeleccionado = lugar
                // Mostrar el InfoWindow del marcador seleccionado
                marker?.showInfoWindow()
            }
        }

        // Si hay un lugar específico seleccionado, centrar en él
        if (lugarSeleccionado != null) {
            val posicion = LatLng(lugarSeleccionado!!.latitud, lugarSeleccionado!!.longitud)
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(posicion, SINGLE_LUGAR_ZOOM)
            )
        } else {
            // Ajustar cámara para mostrar todos los marcadores
            try {
                val bounds = boundsBuilder.build()
                val padding = 150 // offset desde los bordes del mapa en píxeles
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            } catch (e: Exception) {
                // Si solo hay un marcador, centrar en él
                if (lugares.size == 1) {
                    val posicion = LatLng(lugares[0].latitud, lugares[0].longitud)
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(posicion, DEFAULT_ZOOM)
                    )
                }
            }
        }
    }

    /**
     * Obtiene el color del marcador según la categoría
     * Los colores coinciden con los de las cards (convertidos a HUE)
     */
    private fun obtenerColorPorCategoria(categoria: String): Float {
        return when (categoria.lowercase()) {
            getString(R.string.category_restaurant).lowercase(), "restaurante", "restaurant" ->
                BitmapDescriptorFactory.HUE_ORANGE // #F29441 - Naranja
            getString(R.string.category_cafe).lowercase(), "cafetería", "cafeteria", "cafe" ->
                BitmapDescriptorFactory.HUE_RED // #8D6E63 - Marrón (aproximado)
            getString(R.string.category_bar).lowercase(), "bar", "tapas" ->
                BitmapDescriptorFactory.HUE_YELLOW // #FFA726 - Amarillo/Naranja
            getString(R.string.category_bakery).lowercase(), "panadería", "panaderia", "bakery" ->
                BitmapDescriptorFactory.HUE_ROSE // #F48FB1 - Rosa
            else -> BitmapDescriptorFactory.HUE_ORANGE // Default: naranja
        }
    }

    private fun habilitarMiUbicacion() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Intenta centrar en la ubicación del usuario, si falla usa la ubicación por defecto
     */
    private fun centrarEnMiUbicacionODefault() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Sin permisos, usar ubicación por defecto
            centrarEnUbicacionDefault()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val miUbicacion = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(miUbicacion, DEFAULT_ZOOM)
                    )
                } else {
                    // No se pudo obtener ubicación, usar por defecto
                    centrarEnUbicacionDefault()
                }
            }
            .addOnFailureListener {
                // Error al obtener ubicación, usar por defecto
                centrarEnUbicacionDefault()
            }
    }

    /**
     * Centra el mapa en la ubicación por defecto (Vitoria-Gasteiz)
     */
    private fun centrarEnUbicacionDefault() {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                habilitarMiUbicacion()
                centrarEnMiUbicacionODefault()
            } else {
                // Permiso denegado, centrar en ubicación por defecto
                centrarEnUbicacionDefault()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Muestra un diálogo con opciones para el lugar seleccionado
     */
    private fun mostrarOpcionesLugar(marker: Marker) {
        val lugarId = marker.tag as? Int ?: return
        val lugar = lugares.find { it.id == lugarId } ?: return

        val opciones = arrayOf(
            getString(R.string.edit),
            getString(R.string.delete)
        )

        AlertDialog.Builder(this)
            .setTitle(lugar.nombre)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> abrirFormularioEditar(lugar)
                    1 -> confirmarEliminarLugar(lugar, marker)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * Abre el formulario para editar un lugar
     */
    private fun abrirFormularioEditar(lugar: Lugar) {
        val intent = Intent(this, FormActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_LUGAR, lugar)
        }
        startActivity(intent)
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar
     */
    private fun confirmarEliminarLugar(lugar: Lugar, marker: Marker) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.map_delete_place))
            .setMessage(getString(R.string.map_delete_confirmation, lugar.nombre))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                eliminarLugar(lugar, marker)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * Elimina un lugar de la base de datos y del mapa
     */
    private fun eliminarLugar(lugar: Lugar, marker: Marker) {
        val resultado = dbHelper.eliminarLugar(lugar.id)
        if (resultado > 0) {
            // Eliminar marcador del mapa
            marker.remove()

            // Eliminar de la lista local
            lugares.remove(lugar)

            Toast.makeText(this, getString(R.string.map_place_deleted), Toast.LENGTH_SHORT).show()

            // Si no quedan lugares, centrar en ubicación por defecto
            if (lugares.isEmpty()) {
                centrarEnUbicacionDefault()
            }
        } else {
            Toast.makeText(this, getString(R.string.map_error_deleting), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configura el long click en el mapa para crear un nuevo lugar
     */
    private fun configurarLongClick() {
        mMap.setOnMapLongClickListener { latLng ->
            mostrarDialogoCrearLugar(latLng)
        }
    }

    /**
     * Muestra un diálogo preguntando si quiere crear un lugar en las coordenadas tocadas
     */
    private fun mostrarDialogoCrearLugar(latLng: LatLng) {
        AlertDialog.Builder(this)
            .setTitle(R.string.map_create_here)
            .setMessage(R.string.map_create_here_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                abrirFormularioConCoordenadas(latLng)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    /**
     * Abre FormActivity con las coordenadas pre-rellenadas
     */
    private fun abrirFormularioConCoordenadas(latLng: LatLng) {
        val intent = Intent(this, FormActivity::class.java).apply {
            putExtra(EXTRA_LATITUDE, latLng.latitude)
            putExtra(EXTRA_LONGITUDE, latLng.longitude)
        }
        startActivity(intent)
    }

    /**
     * Recarga los lugares y actualiza el mapa
     */
    private fun recargarMapa() {
        // Recargar lugares de la base de datos
        lugares = dbHelper.consultarTodos()

        // Limpiar marcadores existentes
        mMap.clear()

        // Volver a mostrar lugares
        mostrarLugares()
    }

    override fun onResume() {
        super.onResume()
        // Recargar el mapa cuando la actividad vuelve a primer plano
        // Esto actualiza el mapa después de crear/editar/eliminar lugares
        if (::mMap.isInitialized) {
            recargarMapa()
        }
    }
}