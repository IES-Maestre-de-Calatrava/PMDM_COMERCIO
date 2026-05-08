package es.maestre.juntosjc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArchivoItem(
    @SerialName("id_documento")
    val id_documento: Int? = null,
    @SerialName("nombre_archivo")
    val nombre_archivo: String,
    @SerialName("ruta_archivo")
    val ruta_archivo: String,
    @SerialName("carpeta_id")
    val carpeta_id: Long? = null
)
