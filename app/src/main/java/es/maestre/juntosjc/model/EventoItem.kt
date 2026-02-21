package es.maestre.juntosjc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventoItem(
    @SerialName("id_evento")
    val id_evento: Int? = null,

    @SerialName("titulo_evento")
    val titulo_evento: String = "", // El "" evita que la app pete si no encuentra la columna

    @SerialName("descripcion_evento")
    val descripcion_evento: String = "",

    @SerialName("fecha_evento")
    val fecha_evento: Long = 0L,

    @SerialName("asistentes")
    val asistentes: String = "",

    @SerialName("Hora")
    val Hora: String = ""
)