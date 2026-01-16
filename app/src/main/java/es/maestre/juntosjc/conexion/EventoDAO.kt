package es.maestre.juntosjc.conexion

import androidx.lifecycle.LiveData
import androidx.room.*
import es.maestre.juntosjc.model.Evento

@Dao
interface EventoDAO {

    /**
     * Insertar un Evento en la BBDD
     */
    @Insert
    suspend fun insert(evento: Evento)

    /**
     * Obtiene todos los Eventos de la BBDD
     */
    @Query("SELECT * FROM evento ORDER BY fecha_evento ASC")
    fun getAllEventos(): LiveData<List<Evento>>


    /**
     * Obtener un Evento por FECHA
     */
    @Query("SELECT * FROM evento WHERE fecha_evento = :fechaSeleccionada")
    fun getEventosByFecha(fechaSeleccionada: Long): LiveData<List<Evento>>

    /**
     * Actualiza una tarea en la BBDD
     */
    @Update
    suspend fun update(evento: Evento)

    /**
     * Elimina una tarea en la BBDD
     */
    @Delete
    suspend fun delete(evento: Evento)




    /* Ahora voy a crear los metodos que me comprueban si mi tabla esta vacia. Si lo esta inserta
     una lista de eventos iniciales para que al iniciar la app haya algo siempre en la BBDD*/

    /**
     * Comprueba si la tabla está vacía
     */
    @Query("SELECT COUNT(*) FROM evento")
    suspend fun contarEventos(): Int

    /**
     * Inserta una lista de eventos si la tabla está vacía
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEventosSiNoHay(evento: List<Evento>)
}