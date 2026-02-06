package es.didaktikapp.repasoapp.models

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import es.didaktikapp.repasoapp.R

/**
 * Enum que representa las categorías de lugares de comida.
 *
 * Este enum proporciona una forma robusta y type-safe de manejar las categorías
 * de lugares en la aplicación. Cada categoría tiene asociados recursos visuales
 * (color e icono) y un string traducible, lo que facilita la internacionalización
 * y evita errores comunes con comparaciones de strings.
 *
 * **Ventajas sobre strings:**
 * - Comparaciones más rápidas y seguras (por referencia en vez de por contenido)
 * - Independiente del idioma (usa códigos únicos)
 * - Type-safe (errores de compilación en vez de runtime)
 * - Autocompletado en el IDE
 * - Refactorización segura
 *
 * **Ejemplo de uso:**
 * ```kotlin
 * val lugar = Lugar(
 *     nombre = "La Pizzería",
 *     categoria = Categoria.RESTAURANT.code,
 *     // ...
 * )
 *
 * // Obtener el color de la categoría
 * val color = lugar.getCategoriaEnum().colorRes
 *
 * // Obtener el icono de la categoría
 * val icono = lugar.getCategoriaEnum().iconRes
 * ```
 *
 * **Migración de datos:**
 * El método [fromString] permite convertir strings antiguos (en cualquier idioma)
 * a códigos de categoría, facilitando la migración de bases de datos existentes.
 *
 * @property code Código único e inmutable de la categoría. Se guarda en la base de datos.
 * @property stringRes Resource ID del nombre traducido de la categoría
 * @property colorRes Resource ID del color asociado a la categoría para visualización
 * @property iconRes Resource ID del icono drawable asociado a la categoría
 *
 * @see Lugar
 * @see LugaresSQLiteHelper
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 2.0
 */
enum class Categoria(
    val code: String,
    @StringRes val stringRes: Int,
    @ColorRes val colorRes: Int,
    @DrawableRes val iconRes: Int
) {
    /**
     * Categoría para restaurantes.
     * Color: Rojo/Rosa suave
     * Icono: Cubiertos
     */
    RESTAURANT(
        code = "RESTAURANT",
        stringRes = R.string.category_restaurant,
        colorRes = R.color.categoria_restaurante,
        iconRes = R.drawable.ic_restaurant_cutlery
    ),

    /**
     * Categoría para cafeterías y cafés.
     * Color: Marrón
     * Icono: Taza de café
     */
    CAFE(
        code = "CAFE",
        stringRes = R.string.category_cafe,
        colorRes = R.color.categoria_cafeteria,
        iconRes = R.drawable.ic_cafe_cup
    ),

    /**
     * Categoría para bares y tabernas.
     * Color: Azul
     * Icono: Copa de bar
     */
    BAR(
        code = "BAR",
        stringRes = R.string.category_bar,
        colorRes = R.color.categoria_bar,
        iconRes = R.drawable.ic_bar_glass
    ),

    /**
     * Categoría para panaderías y pastelerías.
     * Color: Rosa claro
     * Icono: Pan
     */
    BAKERY(
        code = "BAKERY",
        stringRes = R.string.category_bakery,
        colorRes = R.color.categoria_panaderia,
        iconRes = R.drawable.ic_bakery_bread
    );

    companion object {
        /**
         * Obtiene una categoría por su código único.
         *
         * Este método se utiliza principalmente al leer datos de la base de datos,
         * donde se almacenan los códigos de categoría en lugar de strings traducidos.
         *
         * @param code Código único de la categoría (ej: "RESTAURANT", "CAFE", "BAR", "BAKERY")
         * @return Categoría correspondiente al código, o [RESTAURANT] por defecto si no se encuentra
         *
         * @see fromString para convertir strings antiguos o traducidos
         */
        fun fromCode(code: String): Categoria {
            return values().find { it.code == code } ?: RESTAURANT
        }

        /**
         * Obtiene una categoría desde un string en cualquier idioma.
         *
         * Este método se utiliza principalmente para la migración de bases de datos
         * antiguas que guardaban nombres de categorías traducidos en lugar de códigos.
         * Soporta múltiples idiomas y variaciones de escritura.
         *
         * **Idiomas soportados:**
         * - Español: "Restaurante", "Cafetería", "Bar", "Panadería"
         * - Inglés: "Restaurant", "Cafe", "Bar", "Bakery"
         * - Euskera: "Okindegi" (panadería)
         *
         * **Ejemplo:**
         * ```kotlin
         * val categoria1 = Categoria.fromString("Restaurante") // → RESTAURANT
         * val categoria2 = Categoria.fromString("Restaurant")  // → RESTAURANT
         * val categoria3 = Categoria.fromString("Cafetería")   // → CAFE
         * val categoria4 = Categoria.fromString("Cafe")        // → CAFE
         * ```
         *
         * @param categoryString String de la categoría en cualquier idioma soportado
         * @return Categoría correspondiente, o [RESTAURANT] por defecto si no se reconoce
         *
         * @see fromCode para convertir códigos únicos
         * @see LugaresSQLiteHelper.migrarVersion2a3 para el uso en migración de BD
         */
        fun fromString(categoryString: String): Categoria {
            return when {
                categoryString.contains("Restaurant", ignoreCase = true) ||
                categoryString.contains("Restaurante", ignoreCase = true) -> RESTAURANT
                categoryString.contains("Caf", ignoreCase = true) -> CAFE
                categoryString.contains("Bar", ignoreCase = true) -> BAR
                categoryString.contains("Bakery", ignoreCase = true) ||
                categoryString.contains("Panad", ignoreCase = true) ||
                categoryString.contains("Okindegi", ignoreCase = true) -> BAKERY
                else -> RESTAURANT
            }
        }

        /**
         * Obtiene todas las categorías disponibles en orden de definición.
         *
         * Este método es útil para poblar spinners, listas o cualquier componente
         * que necesite mostrar todas las opciones de categoría disponibles.
         *
         * @return Array con todas las categorías en el orden: [RESTAURANT], [CAFE], [BAR], [BAKERY]
         *
         * @see values para acceder directamente al array de enum
         */
        fun getAllCategories(): Array<Categoria> {
            return values()
        }
    }
}