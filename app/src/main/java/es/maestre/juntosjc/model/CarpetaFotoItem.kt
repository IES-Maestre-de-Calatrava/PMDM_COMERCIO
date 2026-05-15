package es.maestre.juntosjc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CarpetaFotoItem(
    @SerialName("id_carpeta")
    val id_carpeta: Long? = null,
    @SerialName("nombre_carpeta")
    val nombre_carpeta: String,
    @SerialName("created_at")
    val created_at: String? = null
)
