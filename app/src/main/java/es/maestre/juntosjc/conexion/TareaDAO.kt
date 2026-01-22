package es.maestre.juntosjc.conexion

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import es.maestre.juntosjc.model.Tarea


@Dao
interface TareaDAO {

    /**
     * Inserta una tarea en la base de datos
     */
    @Insert
    suspend fun insert(tarea: Tarea)

    /**
     * Obtiene todas los tareas de la base de datos
     */
    @Query("SELECT * FROM tarea")
    fun getAllTareas(): LiveData<List<Tarea>>

    /**
     * Obtiene una tarea por su id
     */
    @Query("SELECT * FROM tarea WHERE id_tarea = :id")
    fun getTareaById(id: Int): LiveData<Tarea>

    /**
     * Actualiza una tarea en la base de datos
     */
    @Update
    suspend fun update(tarea: Tarea)

    /**
     * Elimina una tarea de la base de datos
     */
    @Delete
    suspend fun delete(tarea: Tarea)




    /* Ahora voy a crear los metodos que me comprueban si mi tabla esta vacia. Si lo esta inserta
     una lista de tareas iniciales para que al iniciar la app haya algo siempre en la BBDD*/

    /**
     * Comprueba si la tabla está vacía
     */
    @Query("SELECT COUNT(*) FROM tarea")
    suspend fun contarTareas(): Int

    /**
     * Inserta una lista de tareas si la tabla está vacía
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTareasSiNoHay(tarea: List<Tarea>)



}