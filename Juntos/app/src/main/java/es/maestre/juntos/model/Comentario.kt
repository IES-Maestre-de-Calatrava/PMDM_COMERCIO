package es.maestre.juntos.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Clase Comentario que representa la entidad comentario en la BBDD
 * para esta primera entrega he usado mi base de datos para hacer el apartado
 * de los comentarios de la app con recyclerView
 */
@Entity (tableName = "comentario")
data class Comentario(

    /**
     * Clave primaria autogenerada, que se autoincrementa, luego el nombre de quien pone el comentario
     * y finalmente el texto del comentario, en un futuro cuando veamos los inicio de sesion, se podria coger
     * el nombre de usuario dependiendo de quien entre en la app y ponerlo automaticamente en los comentarios,
     * ademas de controlar el acceso de otros usuarios a la unidad familiar
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_comentario")
    val idComentario:Long = 0,

    @ColumnInfo(name = "nombre_usuario")
    var nombre:String,

    @ColumnInfo(name = "texto")
    var texto:String): java.io.Serializable {

    }

