package es.maestre.juntos.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.maestre.juntos.conexion.ComentarioRepository
import es.maestre.juntos.conexion.AppDatabase
import kotlinx.coroutines.launch
import es.maestre.juntos.model.Comentario
import kotlinx.coroutines.Dispatchers

class ComentarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ComentarioRepository
    public val data: LiveData<List<Comentario>>

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