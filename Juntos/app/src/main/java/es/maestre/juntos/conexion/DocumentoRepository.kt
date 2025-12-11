package es.maestre.juntos.conexion

import androidx.lifecycle.LiveData
import es.maestre.juntos.model.Documento

class DocumentoRepository(private val documentoDAO: DocumentoDAO) {

    // Creo una lista con Documentos, para el caso de no haber ninguno al iniciar la app
    private val documentos = listOf(
        Documento(nombreArchivo = "imagen_de_muestra1", rutaArchivo = "https://lxmkwegowscwhgrfsqcw.supabase.co/storage/v1/object/public/AppJUNTOS/imagen_de_muestra1.jpg"),
        Documento(nombreArchivo = "imagen_de_muestra2", rutaArchivo = "https://lxmkwegowscwhgrfsqcw.supabase.co/storage/v1/object/public/AppJUNTOS/imagen_de_muestra2.jpg"),
        Documento(nombreArchivo = "JUNTOS", rutaArchivo = "https://lxmkwegowscwhgrfsqcw.supabase.co/storage/v1/object/public/AppJUNTOS/JUNTOS.pdf")
    )

    /**
     * Este es el metodo que me inserta la lista si no hay documentos
     * llamando a los metodos de documentoDAO creados previamente
     */
    suspend fun insertarDocumentosInicio(){
        if(documentoDAO.contarDocumentos()==0){
            documentoDAO.insertarDocumentosSiNoHay(documentos)
        }
    }

    fun getAllDocumentos(): LiveData<List<Documento>> {
        return documentoDAO.getAllDocumentos()
    }

    suspend fun insert(documento: Documento) {
        documentoDAO.insert(documento)
    }

    suspend fun update(documento: Documento) {
        documentoDAO.update(documento)
    }

    suspend fun delete(documento: Documento) {
        documentoDAO.delete(documento)
    }

    fun getDocumentoById(id: Int): LiveData<Documento> {
        return documentoDAO.getDocumentoById(id)
    }

    fun getDocumentoByRuta(ruta: String): LiveData<Documento> {
        return documentoDAO.getDocumentoByRuta(ruta)
    }
}

