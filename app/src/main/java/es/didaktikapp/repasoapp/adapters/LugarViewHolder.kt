package es.didaktikapp.repasoapp.adapters

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import es.didaktikapp.repasoapp.R
import es.didaktikapp.repasoapp.models.Lugar

/**
 * ViewHolder para mostrar un lugar en el RecyclerView con diseño moderno.
 *
 * Gestiona las vistas de un card individual de lugar, incluyendo
 * icono de categoría, información del lugar y botones de acción.
 *
 * @property itemView Vista raíz del item del RecyclerView
 *
 * @constructor Crea un ViewHolder vinculando las vistas del layout
 * @param view Vista raíz del item (item_lugar_card.xml)
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 2.0
 *
 * @see LugarAdapter
 * @see Lugar
 * @see RecyclerView.ViewHolder
 */
class LugarViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val viewColorCategoria: View = view.findViewById(R.id.viewColorCategoria)
    private val ivLugarImagen: ShapeableImageView = view.findViewById(R.id.ivLugarImagen)
    private val tvNombre: TextView = view.findViewById(R.id.tvNombre)
    private val tvRating: TextView = view.findViewById(R.id.tvRating)
    private val tvTipoCocina: TextView = view.findViewById(R.id.tvTipoCocina)
    private val ivFavorito: ImageButton = view.findViewById(R.id.ivFavorito)
    private val ivVerMapa: ImageButton = view.findViewById(R.id.ivVerMapa)
    private val ivCompartir: ImageButton = view.findViewById(R.id.ivCompartir)

    /**
     * Vincula los datos de un lugar a las vistas del card
     * @param lugar Lugar a mostrar
     * @param onFavoritoClick Callback cuando se pulsa el botón de favorito
     * @param onVerMapaClick Callback cuando se pulsa el botón de ver en mapa
     * @param onCompartirClick Callback cuando se pulsa el botón de compartir
     * @param onItemClick Callback cuando se pulsa el card completo
     * @param onItemLongClick Callback cuando se mantiene presionado el card
     */
    fun bind(
        lugar: Lugar,
        onFavoritoClick: (Lugar) -> Unit,
        onVerMapaClick: (Lugar) -> Unit,
        onCompartirClick: (Lugar) -> Unit,
        onItemClick: (Lugar) -> Unit,
        onItemLongClick: (Lugar) -> Unit = {}
    ) {
        // Color de categoría
        val colorResId = lugar.obtenerColorCategoria()
        viewColorCategoria.setBackgroundResource(colorResId)

        // Nombre
        tvNombre.text = lugar.nombre

        // Rating
        tvRating.text = String.format("%.1f", lugar.rating)

        // Tipo de cocina (usar tipoCocina si existe, sino categoria)
        tvTipoCocina.text = lugar.tipoCocina.ifBlank { lugar.categoria }

        // Icono de categoría con color de fondo
        val iconoResId = lugar.obtenerIconoCategoria()
        val colorCategoria = ContextCompat.getColor(itemView.context, colorResId)
        ivLugarImagen.setImageResource(iconoResId)
        ivLugarImagen.setBackgroundColor(colorCategoria)
        ivLugarImagen.scaleType = android.widget.ImageView.ScaleType.CENTER

        // Favorito - cambiar icono según estado
        if (lugar.esFavorito) {
            ivFavorito.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            ivFavorito.setImageResource(R.drawable.ic_favorite_border)
        }

        // Click listeners
        itemView.setOnClickListener { onItemClick(lugar) }
        itemView.setOnLongClickListener {
            onItemLongClick(lugar)
            true // Consumir el evento
        }
        ivFavorito.setOnClickListener { onFavoritoClick(lugar) }
        ivVerMapa.setOnClickListener { onVerMapaClick(lugar) }
        ivCompartir.setOnClickListener { onCompartirClick(lugar) }
    }
}