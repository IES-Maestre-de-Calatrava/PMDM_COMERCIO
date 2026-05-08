package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.model.ArchivoItem
import es.maestre.juntosjc.model.CarpetaDocumentoItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel de los documentos
 */
class DocumentoViewModel(application: Application) : AndroidViewModel(application) {

    val listaArchivosSupabase = mutableStateListOf<ArchivoItem>()
    val listaCarpetasSupabase = mutableStateListOf<CarpetaDocumentoItem>()

    var mostrarDialogoSubida by mutableStateOf(false)
    var mostrarDialogoCrearCarpeta by mutableStateOf(false)

    fun obtenerArchivosSupabase() {
        viewModelScope.launch {
            try {
                val resultado = SupabaseClient.client.from("documento")
                    .select()
                    .decodeList<ArchivoItem>()
                    .sortedBy { it.nombre_archivo.lowercase() }

                listaArchivosSupabase.clear()
                listaArchivosSupabase.addAll(resultado)
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error obteniendo documentos: ${e.message}", e)
            }
        }
    }

    fun obtenerCarpetasSupabase() {
        viewModelScope.launch {
            try {
                val resultado = SupabaseClient.client.from("carpetas_documentos")
                    .select()
                    .decodeList<CarpetaDocumentoItem>()
                    .sortedBy { it.nombre_carpeta.lowercase() }

                listaCarpetasSupabase.clear()
                listaCarpetasSupabase.addAll(resultado)
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error obteniendo carpetas: ${e.message}", e)
            }
        }
    }

    fun crearCarpetaSupabase(
        nombreCarpeta: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val carpetaNueva = CarpetaDocumentoItem(
                    nombre_carpeta = nombreCarpeta.trim()
                )

                SupabaseClient.client.from("carpetas_documentos").insert(carpetaNueva)
                obtenerCarpetasSupabase()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun subirDocumento(
        byteArray: ByteArray,
        nombrePersonalizado: String,
        carpetaId: Long?,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bucketName = "AppJUNTOS"
                val bucket = SupabaseClient.client.storage.from(bucketName)

                val nombreSeguro = nombrePersonalizado
                    .replace(" ", "_")
                    .replace(Regex("[^a-zA-Z0-9._-]"), "")

                val storageFileName = "${UUID.randomUUID()}_$nombreSeguro"

                bucket.upload(storageFileName, byteArray)
                val publicUrl = bucket.publicUrl(storageFileName)

                val nuevoArchivo = ArchivoItem(
                    id_documento = null,
                    nombre_archivo = nombrePersonalizado,
                    ruta_archivo = publicUrl,
                    carpeta_id = carpetaId
                )

                SupabaseClient.client.from("documento").insert(nuevoArchivo)
                obtenerArchivosSupabase()
                onSuccess(publicUrl)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
