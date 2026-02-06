package es.didaktikapp.repasoapp.models

import java.io.Serializable

/**
 * Modelo de datos para representar un lugar favorito de comida.
 *
 * Esta clase data almacena toda la información relevante de un lugar,
 * incluyendo ubicación, categoría, rating y estado de favorito.
 *
 * @property id Identificador único del lugar
 * @property nombre Nombre del lugar
 * @property descripcion Descripción detallada del lugar
 * @property latitud Coordenada de latitud
 * @property longitud Coordenada de longitud
 * @property categoria Categoría del lugar (Restaurant, Café, Bar, Bakery)
 * @property fechaCreacion Timestamp de creación en milisegundos
 * @property rating Valoración del lugar (0.0 - 5.0)
 * @property esFavorito Indica si el lugar está marcado como favorito
 * @property tipoCocina Tipo de cocina o especialidad del lugar
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 1.0
 */
data class Lugar(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val categoria: String,
    val fechaCreacion: Long = System.currentTimeMillis(),

    // Nuevos campos para el rediseño
    val rating: Float = 0.0f,
    val esFavorito: Boolean = false,
    val tipoCocina: String = ""
) : Serializable {

    /**
     * Obtiene el enum de categoría desde el string guardado
     * @return Categoría correspondiente
     */
    fun getCategoriaEnum(): Categoria {
        return Categoria.fromCode(categoria)
    }

    /**
     * Obtiene el color asociado a la categoría del lugar
     * @return Resource ID del color
     */
    fun obtenerColorCategoria(): Int {
        return getCategoriaEnum().colorRes
    }

    /**
     * Obtiene el icono asociado a la categoría del lugar
     * @return Resource ID del drawable
     */
    fun obtenerIconoCategoria(): Int {
        return getCategoriaEnum().iconRes
    }
}