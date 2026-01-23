package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.conexion.AppDatabase
import es.maestre.juntosjc.model.Tarea
import es.maestre.juntosjc.conexion.TareaRepository
import es.maestre.juntosjc.model.ComentarioItem
import es.maestre.juntosjc.model.TareaItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel de las tareas
 */
class TareaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TareaRepository
    public val data: LiveData<List<Tarea>>


    val listaTareasSupabase = mutableStateListOf<TareaItem>()

    // 1. Obtener los archivos de la tabla de supabase de comentario
    fun obtenerTareasSupabase() {
        viewModelScope.launch {
            try {
                // Hacemos el SELECT a la tabla "tarea"
                val resultado = SupabaseClient.client.from("tarea")
                    .select().decodeList<TareaItem>()

                listaTareasSupabase.clear()
                listaTareasSupabase.addAll(resultado)

                Log.d("Supabase.Fetch", "Datos traídos: ${resultado.size}")
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error: ${e.message}")
            }
        }
    }

    // 2. Insertar en Supabase
    fun insertarTareaSupabase(item: TareaItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("tarea").insert(item)
                obtenerTareasSupabase() // Refrescamos la lista
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al insertar: ${e.message}")
            }
        }
    }

    // 3. Actualizar en Supabase
    fun actualizarTareaSupabase(item: TareaItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("tarea").update(item) {
                    filter { eq("id_tarea", item.id_tarea ?: 0) }
                }
                obtenerTareasSupabase()
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al actualizar: ${e.message}")
            }
        }
    }

    // 4. Borrar en Supabase
    fun borrarTareaSupabase(id: Int?, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("tarea").delete {
                    filter { eq("id_tarea", id as Any) }
                }
                obtenerTareasSupabase()
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al borrar: ${e.message}")
            }
        }
    }






    init {
        val tareaDAO = AppDatabase.getDatabase(application.applicationContext).tareaDAO()
        data = tareaDAO.getAllTareas()
        repository = TareaRepository(tareaDAO)
    }


    private fun getAllTareas(): LiveData<List<Tarea>> {
        return repository.getAllTareas()
    }

    fun getTareaById(id:Int):LiveData<Tarea> {
        return repository.getTareaById(id)
    }

    fun insert(tarea: Tarea) = viewModelScope.launch {
        repository.insert(tarea)
    }

    fun update(tarea: Tarea) = viewModelScope.launch{
        repository.update(tarea)
    }

    fun delete(tarea: Tarea) = viewModelScope.launch{
        repository.delete(tarea)
    }


    // Ahora aqui añado la funcion de insertar tareas si no hay
    fun insertarTareasInicio(){
        viewModelScope.launch(Dispatchers.IO) { // Esto de dispatchers.IO es un elemento de coroutines de kotlin, le indica al sistema que ejecute esta tarea sin bloquear el hilo principal, lo redirige a otro mas delicado
            repository.insertarTareasInicio()
        }
    }


}