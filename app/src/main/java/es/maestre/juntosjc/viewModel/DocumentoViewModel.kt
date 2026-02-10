package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.model.ArchivoItem
import kotlinx.coroutines.launch
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

/**
 * ViewModel de los documentos
 */
class DocumentoViewModel(application: Application) : AndroidViewModel(application) {

    val listaArchivosSupabase = mutableStateListOf<ArchivoItem>()

    // 1. ESTADO PARA EL DIÁLOGO
    // Usamos mutableStateOf para que Compose reaccione automáticamente
    var mostrarDialogoNombre by mutableStateOf(false)

    fun obtenerArchivosSupabase() {
        viewModelScope.launch {
            try {
                val resultado = SupabaseClient.client.from("documento")
                    .select().decodeList<ArchivoItem>()

                listaArchivosSupabase.clear()
                listaArchivosSupabase.addAll(resultado)
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error: ${e.message}")
            }
        }
    }

    /**
     * Función modificada para aceptar el nombre personalizado
     */
    fun subirImagen(
        byteArray: ByteArray,
        extension: String,
        nombrePersonalizado: String, // <--- Nuevo parámetro
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Usamos Dispatchers.IO porque subir archivos es una tarea pesada de entrada/salida
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 2. USAR EL NOMBRE PERSONALIZADO
                // El nombrePersonalizado ya trae la extensión desde la Activity
                val fileName = nombrePersonalizado
                val bucketName = "AppJUNTOS"

                val bucket = SupabaseClient.client.storage.from(bucketName)
                bucket.upload(fileName, byteArray)

                val publicUrl = bucket.publicUrl(fileName)

                val nuevoArchivo = ArchivoItem(
                    id_documento = null,
                    nombre_archivo = fileName, // Se guarda el nombre "bonito"
                    ruta_archivo = publicUrl
                )

                SupabaseClient.client.from("documento").insert(nuevoArchivo)

                onSuccess(publicUrl)

            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}