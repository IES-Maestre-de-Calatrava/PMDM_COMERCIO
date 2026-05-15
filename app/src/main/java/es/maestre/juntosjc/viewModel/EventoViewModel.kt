package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.model.ComentarioItem
import es.maestre.juntosjc.model.EventoItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.query.Columns


/**
 * ViewModel de los eventos
 */
class EventoViewModel(application: Application) : AndroidViewModel(application) {

    val fechasConEventos = mutableStateListOf<Long>()
    val listaEventosFiltrados = mutableStateListOf<EventoItem>()

    /**
     *   Obtener los archivos de la tabla de supabase de eventos
     */
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

    /**
     *   Insertar los eventos
     */
    fun insertarEventoSupabase(evento: EventoItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("evento").insert(evento)
                // Refrescamos la lista para la fecha del evento insertado
                cargarTodasLasFechasConEventos()
                obtenerEventosPorFechaSupabase(evento.fecha_evento)
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al insertar: ${e.message}")
            }
        }
    }

    /**
     *  Borrado de eventos
     */
    fun borrarEventoSupabase(id: Int, fechaActual: Long) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("evento").delete {
                    filter { eq("id_evento", id) }
                }
                cargarTodasLasFechasConEventos()
                // Refrescamos la lista de ese día
                obtenerEventosPorFechaSupabase(fechaActual)
            } catch (e: Exception) {
                Log.e("Supabase", "Error al borrar: ${e.message}")
            }
        }
    }


    /**
     *  Actualizar un evento existente
     */
    fun actualizarEventoSupabase(evento: EventoItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("evento").update(evento) {
                    filter { eq("id_evento", evento.id_evento ?: 0) }
                }
                // Refrescamos la lista para la fecha del evento actualizado
                obtenerEventosPorFechaSupabase(evento.fecha_evento)
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al actualizar: ${e.message}")
            }
        }
    }

    /**
     * Cargar todas las fechas con evento
     */
    fun cargarTodasLasFechasConEventos() {
        viewModelScope.launch {
            try {
                val resultado = SupabaseClient.client.from("evento")
                    .select(columns = Columns.list("fecha_evento"))
                    .decodeList<EventoItem>()

                fechasConEventos.clear()
                fechasConEventos.addAll(resultado.map { it.fecha_evento })

                Log.d("Supabase", "Fechas cargadas: ${fechasConEventos.size}")
            } catch (e: Exception) {
                Log.e("Supabase", "Error al cargar fechas: ${e.message}")
            }
        }
    }
}