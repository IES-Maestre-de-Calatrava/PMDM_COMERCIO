package es.maestre.juntosjc.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Clase Comentario que representa la entidad comentario en la BBDD
 * con una lazyColumn en el apartado de RedSocial
 */
@Entity (tableName = "comentario")
data class Comentario(

    /**
     * Clave primaria autogenerada, que se autoincrementa, luego el nombre de quien pone el comentario
     * y finalmente el texto del comentario
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_comentario")
    val idComentario:Long = 0,

    @ColumnInfo(name = "nombre_usuario")
    var nombre:String,

    @ColumnInfo(name = "texto")
    var texto:String): java.io.Serializable {

}