package es.maestre.juntosjc.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Clase Documento que representa la entidad documento en la BBDD
 * se hará con lazyColumn en el apartado ArchivoActivity
 */
@Entity (tableName = "documento")
data class Documento(

    /**
     * Clave primaria autogenerada, que se autoincrementa, tiene el nombre del archivo y su ruta de supabase
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_documento")
    val idDocumento:Long = 0,

    @ColumnInfo(name = "nombre_archivo")
    val nombreArchivo:String,

    @ColumnInfo(name = "ruta_archivo")
    var rutaArchivo:String): java.io.Serializable {


    }

