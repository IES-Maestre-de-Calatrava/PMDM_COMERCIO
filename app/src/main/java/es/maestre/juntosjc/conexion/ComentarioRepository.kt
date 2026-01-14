package es.maestre.juntos.conexion

import androidx.lifecycle.LiveData
import es.maestre.juntos.model.Comentario

class ComentarioRepository(private val comentarioDAO: ComentarioDAO) {

    // Creo una lista con comentarios, para el caso de no haber ninguno al iniciar la app
    private val comentarios = listOf(
        Comentario(nombre="Javier", texto="Este es el primer mensaje generado en la base de datos en caso de no haber ninguno"),
        Comentario(nombre="Juan", texto="Segundo mensaje de la lista de comentarios"),
        Comentario(nombre="Pedro", texto="Tercer mensaje de la lista de comentarios")
    )

    /**
     * Este es el metodo que me inserta la lista si no hay comentarios
     * llamando a los metodos de comentarioDAO creados previamente
     */
    suspend fun insertarComentariosInicio(){
        if(comentarioDAO.contarComentarios()==0){
            comentarioDAO.insertarComentariosSiNoHay(comentarios)
        }
    }

    fun getAllComentarios(): LiveData<List<Comentario>> {
        return comentarioDAO.getAllComentarios()
    }
    suspend fun insert(comentario: Comentario) {
        comentarioDAO.insert(comentario)
    }

    suspend fun update(comentario: Comentario) {
        comentarioDAO.update(comentario)
    }

    suspend fun delete(comentario: Comentario) {
        comentarioDAO.delete(comentario)
    }

    fun getComentarioById(id: Int): LiveData<Comentario> {
        return comentarioDAO.getComentarioById(id)
    }
}