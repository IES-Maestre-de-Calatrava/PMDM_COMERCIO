package es.maestre.juntosjc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TareaItem(
    @SerialName("id_tarea")
    val id_tarea: Int? = null,
    @SerialName("titulo_tarea")
    val titulo_tarea: String,
    @SerialName("descripcion_tarea")
    val descripcion_tarea: String,
    @SerialName("fecha_entrega")
    val fecha_entrega: String,
    @SerialName("estado")
    val estado: Int,
    @SerialName("persona_encargada")
    val persona_encargada: String,
    @SerialName("hora")
    val hora: String)

