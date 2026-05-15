package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.model.CarpetaFotoItem
import es.maestre.juntosjc.model.FotoCamaraItem
import es.maestre.juntosjc.supabase.SupabaseClient
import es.maestre.juntosjc.supabase.parseStoragePathOrNull
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class FotoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val BUCKET_NAME = "CameraPhotos"
    }

    val listaFotosSupabase = mutableStateListOf<FotoCamaraItem>()
    val listaCarpetasSupabase = mutableStateListOf<CarpetaFotoItem>()

    var mostrarDialogoGuardarFoto by mutableStateOf(false)
    var mostrarDialogoCrearCarpeta by mutableStateOf(false)

    /**
     * Select a todas las fotos del supabase
     */
    fun obtenerFotosSupabase() {
        viewModelScope.launch {
            try {
                val resultado = SupabaseClient.client.from("fotosCamara")
                    .select()
                    .decodeList<FotoCamaraItem>()
                    .sortedBy { it.nombreOrdenable }

                listaFotosSupabase.clear()
                listaFotosSupabase.addAll(resultado)
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error obteniendo fotos: ${e.message}", e)
            }
        }
    }

    /**
     * Select a las carpetas de la tabla carpetas_fotos de supabase
     */
    fun obtenerCarpetasSupabase() {
        viewModelScope.launch {
            try {
                val resultado = SupabaseClient.client.from("carpetas_fotos")
                    .select()
                    .decodeList<CarpetaFotoItem>()
                    .sortedBy { it.nombre_carpeta.lowercase() }

                listaCarpetasSupabase.clear()
                listaCarpetasSupabase.addAll(resultado)
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error obteniendo carpetas de fotos: ${e.message}", e)
            }
        }
    }

    /**
     * Crear una carpeta
     */
    fun crearCarpetaSupabase(
        nombreCarpeta: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val carpetaNueva = CarpetaFotoItem(nombre_carpeta = nombreCarpeta.trim())
                SupabaseClient.client.from("carpetas_fotos").insert(carpetaNueva)
                obtenerCarpetasSupabase()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Subir foto, tambien guarda la ruta y el id de la carpeta al que pertence en caso de elegir alguna
     * diferente a "Sin Carpeta"
     */
    fun subirFoto(
        byteArray: ByteArray,
        nombrePersonalizado: String,
        carpetaId: Long?,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bucket = SupabaseClient.client.storage.from(BUCKET_NAME)

                val nombreSeguro = nombrePersonalizado
                    .replace(" ", "_")
                    .replace(Regex("[^a-zA-Z0-9._-]"), "")

                val storageFileName = "${UUID.randomUUID()}_$nombreSeguro"

                bucket.upload(storageFileName, byteArray) {
                    upsert = true
                }
                val publicUrl = bucket.publicUrl(storageFileName)

                val nuevaFoto = FotoCamaraItem(
                    id = null,
                    urlImagen = publicUrl,
                    nombre_foto = nombrePersonalizado,
                    carpeta_id = carpetaId
                )

                SupabaseClient.client.from("fotosCamara").insert(nuevaFoto)
                obtenerFotosSupabase()
                onSuccess(publicUrl)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Cuando eliminamos la foto, se elimina del bucket y de la tabla fotos camara
     */
    fun eliminarFotoSupabase(
        foto: FotoCamaraItem,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fotoId = foto.id ?: throw IllegalStateException("La foto no tiene identificador")
                val storagePath = parseStoragePathOrNull(foto.urlImagen, BUCKET_NAME)

                if (storagePath != null) {
                    SupabaseClient.client.storage.from(BUCKET_NAME).delete(listOf(storagePath))
                }

                SupabaseClient.client.from("fotosCamara").delete {
                    filter { eq("id", fotoId) }
                }

                val fotoPersistente = SupabaseClient.client.from("fotosCamara")
                    .select {
                        filter { eq("id", fotoId) }
                    }
                    .decodeList<FotoCamaraItem>()
                    .isNotEmpty()

                if (fotoPersistente) {
                    throw IllegalStateException("Supabase no ha borrado la foto de la base de datos. Revisa la política DELETE/RLS de la tabla fotosCamara.")
                }

                withContext(Dispatchers.Main) {
                    listaFotosSupabase.removeAll { it.id == fotoId }
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    /**
     * Elimina la carpeta, solo si NO tiene elementos dentro
     */
    fun eliminarCarpetaSupabase(
        carpeta: CarpetaFotoItem,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val carpetaId = carpeta.id_carpeta ?: throw IllegalStateException("La carpeta no tiene identificador")
                val contieneFotos = listaFotosSupabase.any { it.carpeta_id == carpetaId }
                if (contieneFotos) {
                    throw IllegalStateException("La carpeta contiene fotos")
                }

                SupabaseClient.client.from("carpetas_fotos").delete {
                    filter { eq("id_carpeta", carpetaId) }
                }

                val carpetaPersistente = SupabaseClient.client.from("carpetas_fotos")
                    .select {
                        filter { eq("id_carpeta", carpetaId) }
                    }
                    .decodeList<CarpetaFotoItem>()
                    .isNotEmpty()

                if (carpetaPersistente) {
                    throw IllegalStateException("Supabase no ha borrado la carpeta de fotos de la base de datos. Revisa la política DELETE/RLS de la tabla carpetas_fotos.")
                }

                withContext(Dispatchers.Main) {
                    listaCarpetasSupabase.removeAll { it.id_carpeta == carpetaId }
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}
