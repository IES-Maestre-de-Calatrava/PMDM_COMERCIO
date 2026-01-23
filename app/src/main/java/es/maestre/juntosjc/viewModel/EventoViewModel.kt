package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.conexion.AppDatabase
import es.maestre.juntosjc.conexion.EventoRepository
import es.maestre.juntosjc.model.ComentarioItem
import es.maestre.juntosjc.model.Evento
import es.maestre.juntosjc.model.EventoItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel de los eventos
 */
class EventoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventoRepository
    public val data: LiveData<List<Evento>>

    val listaEventosFiltrados = mutableStateListOf<EventoItem>()

    // 1. Obtener los archivos de la tabla de supabase de evento
    fun obtenerEventosPorFechaSupabase(fechaMilis: Long) {
        viewModelScope.launch {
            try {
                // Dia exacto en milisegundos
                val resultado = SupabaseClient.client.from("evento").select {
                    filter {
                        eq("fecha_evento", fechaMilis)
                    }
                }.decodeList<EventoItem>()

                listaEventosFiltrados.clear()
                listaEventosFiltrados.addAll(resultado)
            } catch (e: Exception) {
                Log.e("Supabase", "Error al filtrar eventos: ${e.message}")
            }
        }
    }

    // 2. Insertar los eventos
    fun insertarEventoSupabase(evento: EventoItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("evento").insert(evento)
                // Refrescamos la lista para la fecha del evento insertado
                obtenerEventosPorFechaSupabase(evento.fecha_evento)
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al insertar: ${e.message}")
            }
        }
    }

    // 3. borrado de eventos
    fun borrarEventoSupabase(id: Int, fechaActual: Long) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("evento").delete {
                    filter { eq("id_evento", id) }
                }
                // Refrescamos la lista de ese día
                obtenerEventosPorFechaSupabase(fechaActual)
            } catch (e: Exception) {
                Log.e("Supabase", "Error al borrar: ${e.message}")
            }
        }
    }

    init {
        val eventoDAO = AppDatabase.getDatabase(application.applicationContext).eventoDAO()
        data = eventoDAO.getAllEventos()
        repository = EventoRepository(eventoDAO)
    }


    private fun getAllEventos(): LiveData<List<Evento>> {
        return repository.getAllEventos()
    }

    fun getEventosByFecha(fechaSeleccionada: Long):LiveData<List<Evento>> {
        return repository.getEventosByFecha(fechaSeleccionada)
    }

    fun insert(evento: Evento) = viewModelScope.launch {
        repository.insert(evento)
    }

    fun update(evento: Evento) = viewModelScope.launch{
        repository.update(evento)
    }

    fun delete(evento: Evento) = viewModelScope.launch{
        repository.delete(evento)
    }


    // Ahora aqui añado la funcion de insertar eventos si no hay
    fun insertarEventoInicio(){
        viewModelScope.launch(Dispatchers.IO) { // Esto de dispatchers.IO es un elemento de coroutines de kotlin, le indica al sistema que ejecute esta tarea sin bloquear el hilo principal, lo redirige a otro mas delicado
            repository.insertarEventosInicio()
        }
    }


}