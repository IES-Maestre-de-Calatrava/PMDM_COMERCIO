package es.maestre.juntosjc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComentarioItem(
    @SerialName("id_comentario")
    val id_comentario: Int? = null,
    @SerialName("nombre_usuario")
    val nombre_usuario: String,
    @SerialName("texto")
    val texto: String,
    @SerialName("titulo")
    val titulo: String,
    @SerialName("icono_usuario")
    val icono_usuario: String? = null,
    @SerialName("hora")
    val hora: String,
    @SerialName("email_usuario")
    val email_usuario: String? = null)
