package es.maestre.juntos.conexion

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import es.maestre.juntos.model.Documento

@Dao
interface DocumentoDAO {

    /**
     * Inserta un documento en la base de datos
     */
    @Insert
    suspend fun insert(documento: Documento)

    /**
     * Obtiene todos los documentos de la base de datos
     */
    @Query("SELECT * FROM documento")
    fun getAllDocumentos(): LiveData<List<Documento>>

    /**
     * Obtiene un documento por su id
     */
    @Query("SELECT * FROM documento WHERE id_documento = :id")
    fun getDocumentoById(id: Int): LiveData<Documento>

    /**
     * Obtiene un documento por su ruta
     */
    @Query("SELECT * FROM documento WHERE ruta_archivo = :ruta")
    fun getDocumentoByRuta(ruta: String): LiveData<Documento>

    /**
     * Actualiza un documento en la base de datos
     */
    @Update
    suspend fun update(documento: Documento)

    /**
     * Elimina un documento de la base de datos
     */
    @Delete
    suspend fun delete(documento: Documento)




    /* Ahora voy a crear los metodos que me comprueban si mi tabla esta vacia. Si lo esta inserta
     una lista de documentos iniciales para que al iniciar la app haya algo siempre en la BBDD*/

    /**
     * Comprueba si la tabla está vacía
     */
    @Query("SELECT COUNT(*) FROM documento")
    suspend fun contarDocumentos(): Int

    /**
     * Inserta una lista de documentos si la tabla está vacía
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDocumentosSiNoHay(documentos: List<Documento>)



}