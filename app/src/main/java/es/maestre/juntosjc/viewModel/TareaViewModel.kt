package es.maestre.juntosjc.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.conexion.AppDatabase
import es.maestre.juntosjc.model.Tarea
import es.maestre.juntosjc.conexion.TareaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel de las tareas
 */
class TareaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TareaRepository
    public val data: LiveData<List<Tarea>>

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