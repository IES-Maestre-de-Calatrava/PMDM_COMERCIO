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

/**
 * ViewModel de los documentos
 */
class DocumentoViewModel (application: Application) : AndroidViewModel(application){

    // Creamos una lista observable para Compose
    val listaArchivosSupabase = mutableStateListOf<ArchivoItem>()

    fun obtenerArchivosSupabase() {
        viewModelScope.launch {
            try {
                // Hacemos el SELECT a la tabla "documento"
                val resultado = SupabaseClient.client.from("documento")
                    .select().decodeList<ArchivoItem>()

                listaArchivosSupabase.clear()
                listaArchivosSupabase.addAll(resultado)

                Log.d("Supabase.Fetch", "Datos traídos: ${resultado.size}")
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error: ${e.message}")
            }
        }
    }

    /**
     * Funcion de subida de imagenes a supabase
     */
    fun subirImagen(byteArray: ByteArray, extension: String, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {

        viewModelScope.launch {
            try {
                // Generar nombre único
                val fileName = "${UUID.randomUUID()}.$extension"
                val bucketName = "AppJUNTOS" // El nombre de Supabase

                // Subir el archivo
                val bucket = SupabaseClient.client.storage.from(bucketName)
                bucket.upload(fileName, byteArray)

                // Obtener la URL pública (si el bucket es público), para guardarla en bbdd
                val publicUrl = bucket.publicUrl(fileName)

                // INSERTAR EN LA TABLA DE SUPABASE

                val nuevoArchivo = ArchivoItem(
                    id_documento = null,
                    nombre_archivo = fileName,
                    ruta_archivo = publicUrl
                )

                // USAMOS 'await' o simplemente la llamada directa sin abrir otro launch
                SupabaseClient.client.from("documento").insert(nuevoArchivo)

                // Devolver la URL para guardarla en tu base de datos
                onSuccess(publicUrl)

            } catch (e: Exception) {
                onError(e)
            }

        }


    }

    companion object


}

