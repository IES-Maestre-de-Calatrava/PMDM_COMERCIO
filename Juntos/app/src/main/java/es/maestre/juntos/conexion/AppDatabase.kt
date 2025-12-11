package es.maestre.juntos.conexion

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import es.maestre.juntos.model.Comentario
import es.maestre.juntos.model.Documento

/**
 * Base de datos de la aplicación con Room, los comentarios se guardan en un archivo "comentarios.db3"
 */
@Database(version = 1, entities = [Comentario::class, Documento::class])
abstract class AppDatabase : RoomDatabase(){

    abstract fun comentarioDAO(): ComentarioDAO
    abstract fun documentoDAO(): DocumentoDAO
    companion object {
        @Volatile private var INSTANCE: es.maestre.juntos.conexion.AppDatabase? = null

        fun getDatabase(context: Context): es.maestre.juntos.conexion.AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder( context.applicationContext,
                    es.maestre.juntos.conexion.AppDatabase::class.java,
                    "JUNTOS.db3" ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}
