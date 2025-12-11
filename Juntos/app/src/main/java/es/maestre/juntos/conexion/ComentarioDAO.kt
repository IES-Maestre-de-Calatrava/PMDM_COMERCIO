package es.maestre.juntos.conexion


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import es.maestre.juntos.model.Comentario

@Dao
interface ComentarioDAO {

    /**
     * Inserta un comentario en la base de datos
     */
    @Insert
    suspend fun insert(comentario: Comentario)

    /**
     * Obtiene todos los comentarios de la base de datos
     */
    @Query("SELECT * FROM comentario")
    fun getAllComentarios(): LiveData<List<Comentario>>

    /**
     * Obtiene un comentario por su id
     */
    @Query("SELECT * FROM comentario WHERE id_comentario = :id")
    fun getComentarioById(id: Int): LiveData<Comentario>

    /**
     * Actualiza un comentario en la base de datos
     */
    @Update
    suspend fun update(comentario: Comentario)

    /**
     * Elimina un comentario de la base de datos
     */
    @Delete
    suspend fun delete(comentario: Comentario)


    /* Ahora voy a crear los metodos que me comprueban si mi tabla esta vacia. Si lo esta inserta
     una lista de comentarios iniciales para que al iniciar la app haya algo siempre en la BBDD*/

    /**
     * Comprueba si la tabla está vacía
     */
    @Query("SELECT COUNT(*) FROM comentario")
    suspend fun contarComentarios(): Int

    /**
     * Inserta una lista de comentarios si la tabla está vacía
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarComentariosSiNoHay(comentarios: List<Comentario>)



}