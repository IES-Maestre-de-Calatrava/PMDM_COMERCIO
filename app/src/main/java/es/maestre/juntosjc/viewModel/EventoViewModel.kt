package es.maestre.juntosjc.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.maestre.juntos.conexion.AppDatabase
import es.maestre.juntosjc.conexion.EventoRepository
import es.maestre.juntosjc.model.Comentario
import es.maestre.juntosjc.model.Evento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventoRepository
    public val data: LiveData<List<Evento>>

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