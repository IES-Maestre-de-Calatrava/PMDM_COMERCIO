package es.maestre.juntos.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.maestre.juntos.R
import es.maestre.juntos.model.Documento

/**
 * Adaptador para la lista de documentos
 */
class DocumentoAdapter(private val documentos: List<Documento>): RecyclerView.Adapter<DocumentoViewHolder>() {

    private var posSelectedItem: Int = -1
    private var data: List<Documento>
    init {
        data = documentos
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentoViewHolder {
        // Inflamos el layout de cada elemento
        val layoutInflater = LayoutInflater.from(parent.context)
        return DocumentoViewHolder(layoutInflater.inflate(R.layout.descargas_lista, parent, false))
    }

    fun updateData(newData: List<Documento>){
        this.data = newData
        notifyItemChanged(0, data.size - 1)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getSelectedPosition(): Int {
        return posSelectedItem
    }

    fun getSelectedItem(): Documento? {
        var item: Documento? = null
        if (posSelectedItem != -1) {
            item = data[posSelectedItem]
        }
        return item
    }

    override fun onBindViewHolder(holder: DocumentoViewHolder, position: Int) {
        // Inicializamos la lista
        val documento = data[position]
        holder.nombreDocumento.text = documento.nombreArchivo

        // al hacer clic, el fondo del ítem cambia de color
        // seleccionamos el dato
        holder.itemView.setOnClickListener {
            if (holder.isBackgroundColorChanged) {
                //deseleccionamos el item
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                holder.isBackgroundColorChanged = false
                if (posSelectedItem == holder.absoluteAdapterPosition) {
                    posSelectedItem = -1
                }
            } else if (posSelectedItem == -1) {
                //seleccionamos el item
                posSelectedItem = holder.absoluteAdapterPosition
                holder.itemView.setBackgroundColor(Color.LTGRAY)
                holder.isBackgroundColorChanged = true
            }
        }

    }




}