package es.maestre.juntosjc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactoItem(
    @SerialName("id_contacto")
    val id_contacto: Int? = null,
    @SerialName("nombre_contacto")
    val nombre_contacto: String,
    @SerialName("telefono_contacto")
    val telefono_contacto: String,
    @SerialName("email_contacto")
    val email_contacto: String,
    @SerialName("empresa")
    val empresa: String? = null,
    @SerialName("direccion_contacto")
    val direccion_contacto: String
)