package es.maestre.juntos.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import es.maestre.juntos.databinding.ItemListaBinding

/**
 * ViewHolder para la lista de comentarios basado en el ViewHolder de abogados de Santa Arevalo
 */
class ComentarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val binding = ItemListaBinding.bind(itemView)
    val nombre: TextView = binding.nombre
    val texto: TextView = binding.textoLista

    var isBackgroundColorChanged = false
}