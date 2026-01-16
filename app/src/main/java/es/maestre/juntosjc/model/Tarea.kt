package es.maestre.juntos.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Clase Tarea que representa la entidad tarea en la BBDD
 * en un lazyColumn en el apartado TareaActivity
 */
@Entity (tableName = "tarea")
data class Tarea(

    /**
     * Clave primaria autogenerada, que se autoincrementa, luego el titulo de la tarea, descripcion,
     * fecha de entrega, un booleano que diga si la tarea esta completado o no y la persona encargada de dicha tarea
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_tarea")
    val idTarea:Long = 0,

    @ColumnInfo(name = "titulo_tarea")
    var tituloTarea: String,

    @ColumnInfo(name = "descripcion_tarea")
    var descripcionTarea: String,

    @ColumnInfo(name = "fecha_entrega")
    var fechaEntrega: String,

    @ColumnInfo(name = "completa")
    var completada: Boolean,

    @ColumnInfo(name = "persona_encargada")
    var personaEncargada: String): java.io.Serializable {

    }

