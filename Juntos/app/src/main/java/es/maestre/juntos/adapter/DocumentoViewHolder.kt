package es.maestre.juntos.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.maestre.juntos.databinding.DescargasListaBinding

class DocumentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val binding = DescargasListaBinding.bind(itemView)
    val nombreDocumento: TextView = binding.nombreDocumento

    var isBackgroundColorChanged = false
}