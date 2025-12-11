package es.maestre.juntos.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.maestre.juntos.conexion.DocumentoRepository
import es.maestre.juntos.conexion.AppDatabase
import es.maestre.juntos.model.Documento
import kotlinx.coroutines.launch
import es.maestre.juntos.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import java.util.UUID

class DocumentoViewModel (application: Application) : AndroidViewModel(application){

    private val repository: DocumentoRepository

    public val data: LiveData<List<Documento>>

    init {
        val documentoDAO = AppDatabase.getDatabase(application.applicationContext).documentoDAO()
        data = documentoDAO.getAllDocumentos()
        repository = DocumentoRepository(documentoDAO)

    }

    private fun getAllDocumentos(): LiveData<List<Documento>> {
        return repository.getAllDocumentos()
    }

    fun getDocumentoById(id:Int):LiveData<Documento> {
        return repository.getDocumentoById(id)
    }

    fun getDocumentoByRuta(ruta:String):LiveData<Documento> {
        return repository.getDocumentoByRuta(ruta)
    }

    fun insert(documento: Documento) = viewModelScope.launch {
        repository.insert(documento)
    }

    fun update(documento: Documento) = viewModelScope.launch{
        repository.update(documento)
    }

    fun delete(documento: Documento) = viewModelScope.launch{
        repository.delete(documento)
    }


    // Aqui va la funcion de insertar documentos al iniciar la app
    fun insertarDocumentosInicio(){
        viewModelScope.launch(Dispatchers.IO) { // Esto de dispatchers.IO es un elemento de coroutines de kotlin, le indica al sistema que ejecute esta tarea sin bloquear el hilo principal, lo redirige a otro mas delicado
            repository.insertarDocumentosInicio()
        }
    }


    /**
     * Funcion de subida de imagenes a firebase
     */
    fun subirImagen(byteArray: ByteArray, extension: String, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {

        viewModelScope.launch {
            try {
                // Generar nombre único
                val fileName = "${UUID.randomUUID()}.$extension"
                val bucketName = "AppJUNTOS" // El nombre que pusiste en Supabase

                // Subir el archivo
                val bucket = SupabaseClient.client.storage.from(bucketName)
                bucket.upload(fileName, byteArray)

                // Obtener la URL pública (si el bucket es público), para guardarla en bbdd
                val publicUrl = bucket.publicUrl(fileName)

                // Tras la subida a supabase, guardamos el documento en nuestra base de datos
                val documento = Documento(nombreArchivo = fileName, rutaArchivo = publicUrl)
                insert(documento)

                // Devolver la URL para guardarla en tu base de datos
                onSuccess(publicUrl)

            } catch (e: Exception) {
                onError(e)
            }

        }


    }

    companion object


}

