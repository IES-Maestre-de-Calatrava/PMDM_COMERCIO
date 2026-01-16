package es.maestre.juntosjc.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Clase Evento que representa la entidad evento en la BBDD
 * en el apartado CalendarioActivity
 */
@Entity(tableName = "evento")
data class Evento(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_evento")
    val idEvento: Long = 0,

    @ColumnInfo(name = "titulo_evento")
    var tituloEvento: String,

    @ColumnInfo(name = "descripcion_evento")
    var descripcionEvento: String,

    @ColumnInfo(name = "fecha_evento")
    var fechaEvento: Long // Guardamos milisegundos para que sea compatible con DatePicker
) : Serializable {}