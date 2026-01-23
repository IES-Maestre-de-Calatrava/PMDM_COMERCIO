package es.maestre.juntosjc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FotoCamaraItem(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("urlImagen")
    val urlImagen: String
)

