package es.maestre.juntosjc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FotoCamaraItem(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("urlImagen")
    val urlImagen: String,
    @SerialName("nombre_foto")
    val nombre_foto: String? = null,
    @SerialName("carpeta_id")
    val carpeta_id: Long? = null,
    @SerialName("created_at")
    val created_at: String? = null
) {
    val nombreVisible: String
        get() = nombre_foto?.takeIf { it.isNotBlank() }
            ?: urlImagen.substringAfterLast('/').substringBefore('?')

    val nombreOrdenable: String
        get() = nombreVisible.lowercase()
}
