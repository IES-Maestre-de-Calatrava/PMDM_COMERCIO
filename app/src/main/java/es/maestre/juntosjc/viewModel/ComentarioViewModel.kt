package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.conexion.AppDatabase
import es.maestre.juntosjc.conexion.ComentarioRepository
import es.maestre.juntosjc.model.ArchivoItem
import es.maestre.juntosjc.model.Comentario
import es.maestre.juntosjc.model.ComentarioItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel de los comentarios
 */
class ComentarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ComentarioRepository
    public val data: LiveData<List<Comentario>>

    val listaComentariosSupabase = mutableStateListOf<ComentarioItem>()

    // 1. Obtener los archivos de la tabla de supabase de comentario
    fun obtenerComentariosSupabase() {
        viewModelScope.launch {
            try {
                // Hacemos el SELECT a la tabla "comentario"
                val resultado = SupabaseClient.client.from("comentario")
                    .select().decodeList<ComentarioItem>()

                listaComentariosSupabase.clear()
                listaComentariosSupabase.addAll(resultado)

                Log.d("Supabase.Fetch", "Datos traídos: ${resultado.size}")
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error: ${e.message}")
            }
        }
    }

    // 2. Insertar en Supabase
    fun insertarComentarioSupabase(item: ComentarioItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("comentario").insert(item)
                obtenerComentariosSupabase() // Refrescamos la lista
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al insertar: ${e.message}")
            }
        }
    }

    // 3. Actualizar en Supabase
    fun actualizarComentarioSupabase(item: ComentarioItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("comentario").update(item) {
                    filter { eq("id_comentario", item.id_comentario ?: 0) }
                }
                obtenerComentariosSupabase()
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al actualizar: ${e.message}")
            }
        }
    }

    // 4. Borrar en Supabase
    fun borrarComentarioSupabase(id: Int?, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("comentario").delete {
                    filter { eq("id_comentario", id as Any) }
                }
                obtenerComentariosSupabase()
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al borrar: ${e.message}")
            }
        }
    }

    init {
        val comentarioDAO = AppDatabase.getDatabase(application.applicationContext).comentarioDAO()
        data = comentarioDAO.getAllComentarios()
        repository = ComentarioRepository(comentarioDAO)
    }


    private fun getAllComentarios(): LiveData<List<Comentario>> {
        return repository.getAllComentarios()
    }

    fun getComentarioById(id:Int):LiveData<Comentario> {
        return repository.getComentarioById(id)
    }

    fun insert(comentario: Comentario) = viewModelScope.launch {
        repository.insert(comentario)
    }

    fun update(comentario: Comentario) = viewModelScope.launch{
        repository.update(comentario)
    }

    fun delete(comentario: Comentario) = viewModelScope.launch{
        repository.delete(comentario)
    }


    // Ahora aqui añado la funcion de insertar comentarios si no hay
    fun insertarComentariosInicio(){
        viewModelScope.launch(Dispatchers.IO) { // Esto de dispatchers.IO es un elemento de coroutines de kotlin, le indica al sistema que ejecute esta tarea sin bloquear el hilo principal, lo redirige a otro mas delicado
        repository.insertarComentariosInicio()
        }
    }


}