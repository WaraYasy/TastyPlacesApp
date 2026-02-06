package es.didaktikapp.repasoapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.didaktikapp.repasoapp.R
import es.didaktikapp.repasoapp.models.Lugar

/**
 * Adapter para el RecyclerView de lugares con diseño moderno de cards.
 *
 * Gestiona la visualización de una lista de lugares en un RecyclerView,
 * con soporte para callbacks de interacción (favorito, ver mapa, compartir, click).
 *
 * @property lugares Lista mutable de lugares a mostrar
 * @property onFavoritoClick Callback ejecutado al pulsar el botón de favorito
 * @property onVerMapaClick Callback ejecutado al pulsar el botón de ver en mapa
 * @property onCompartirClick Callback ejecutado al pulsar el botón de compartir
 * @property onItemClick Callback ejecutado al pulsar el card completo
 * @property onItemLongClick Callback ejecutado al mantener presionado el card
 *
 * @constructor Crea un adapter con la lista de lugares y callbacks
 *
 * @author Wara Pacheco
 * @version 2.0
 * @since 1.0
 *
 * @see LugarViewHolder
 * @see Lugar
 * @see RecyclerView.Adapter
 */
class LugarAdapter(
    private var lugares: ArrayList<Lugar>,
    private val onFavoritoClick: (Lugar) -> Unit,
    private val onVerMapaClick: (Lugar) -> Unit,
    private val onCompartirClick: (Lugar) -> Unit,
    private val onItemClick: (Lugar) -> Unit,
    private val onItemLongClick: (Lugar) -> Unit = {}
) : RecyclerView.Adapter<LugarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        // Inflar el layout del card
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lugar_card, parent, false)
        return LugarViewHolder(view)
    }

    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {
        // Enlazar los datos del lugar con el ViewHolder
        holder.bind(
            lugares[position],
            onFavoritoClick,
            onVerMapaClick,
            onCompartirClick,
            onItemClick,
            onItemLongClick
        )
    }

    override fun getItemCount(): Int = lugares.size

    /**
     * Actualiza la lista de lugares y refresca el RecyclerView
     */
    fun actualizarLista(nuevaLista: ArrayList<Lugar>) {
        lugares = nuevaLista
        // Notificar cambios para refrescar toda la lista
        notifyDataSetChanged()
    }

    /**
     * Actualiza el estado de favorito de un lugar específico
     * Solo actualiza el item afectado para mejor rendimiento
     */
    fun actualizarFavorito(lugarId: Int, esFavorito: Boolean) {
        // Buscar el lugar por ID
        val index = lugares.indexOfFirst { it.id == lugarId }
        if (index != -1) {
            // Crear copia con el nuevo estado usando data class copy()
            lugares[index] = lugares[index].copy(esFavorito = esFavorito)
            // Notificar solo el item modificado
            notifyItemChanged(index)
        }
    }

    /**
     * Obtiene un lugar por su posición
     */
    fun obtenerLugar(position: Int): Lugar = lugares[position]
}