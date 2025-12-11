package es.maestre.juntos.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.maestre.juntos.R
import es.maestre.juntos.model.Comentario

/**
 * Adaptador para la lista de comentarios basado en el adapter de abogados de Santa Arevalo
 */
class ComentarioAdapter(private val comentarios: List<Comentario>): RecyclerView.Adapter<ComentarioViewHolder>() {

    private var posSelectedItem: Int = -1
    private var data: List<Comentario>
    init {
        data = comentarios
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        // Inflamos el layout de cada elemento
        val layoutInflater = LayoutInflater.from(parent.context)
        return ComentarioViewHolder(layoutInflater.inflate(R.layout.item_lista, parent, false))
    }

    fun updateData(newData: List<Comentario>){
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


    fun getSelectedItem(): Comentario? {
        var item: Comentario? = null
        if (posSelectedItem != -1) {
            item = data[posSelectedItem]
        }
        return item
    }


    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        // Inicializamos la lista
        val comentario = data[position]
        holder.nombre.text = comentario.nombre
        holder.texto.text = comentario.texto

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