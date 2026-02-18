package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.PerfilRow
import es.maestre.juntosjc.model.ComentarioItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch


/**
 * ViewModel de los comentarios
 */
class ComentarioViewModel(application: Application) : AndroidViewModel(application) {

    val listaComentariosSupabase = mutableStateListOf<ComentarioItem>()

    // 1. Obtener los archivos de la tabla de supabase de comentario
    fun obtenerComentariosSupabase() {
        viewModelScope.launch {
            try {
                // Hacemos el SELECT a la tabla "comentario"
                val resultado = SupabaseClient.client.from("comentario")
                    .select().decodeList<ComentarioItem>()

                listaComentariosSupabase.clear()
                listaComentariosSupabase.addAll(resultado)

                Log.d("Supabase.Fetch", "Datos traídos: ${resultado.size}")
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error: ${e.message}")
            }
        }
    }

    // 2. Insertar en Supabase
    fun insertarComentarioSupabase(item: ComentarioItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("comentario").insert(item)
                obtenerComentariosSupabase() // Refrescamos la lista
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al insertar: ${e.message}")
            }
        }
    }

    // 3. Actualizar en Supabase
    fun actualizarComentarioSupabase(item: ComentarioItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("comentario").update(item) {
                    filter { eq("id_comentario", item.id_comentario ?: 0) }
                }
                obtenerComentariosSupabase()
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al actualizar: ${e.message}")
            }
        }
    }

    // 4. Borrar en Supabase
    fun borrarComentarioSupabase(id: Int?, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("comentario").delete {
                    filter { eq("id_comentario", id as Any) }
                }
                obtenerComentariosSupabase()
                onDone()
            } catch (e: Exception) {
                Log.e("Supabase", "Error al borrar: ${e.message}")
            }
        }
    }

    fun getEmailUsuario(): String? {
        val prefs = getApplication<Application>().getSharedPreferences("APP", 0)
        return prefs.getString("email_usuario", null)
    }


    suspend fun obtenerIconoDesdePerfiles(): String? {
        val email = getEmailUsuario() ?: return null

        return try {
            val perfiles = SupabaseClient.client.from("perfiles")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<PerfilRow>()

            perfiles.firstOrNull()?.icono
        } catch (e: Exception) {
            Log.e("Supabase.Perfiles", "Error obteniendo icono: ${e.message}")
            null
        }
    }


    suspend fun obtenernombreDesdePerfiles(): String? {
        val email = getEmailUsuario() ?: return null

        return try {
            val perfiles = SupabaseClient.client.from("perfiles")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<PerfilRow>()

            perfiles.firstOrNull()?.nombre
        } catch (e: Exception) {
            Log.e("Supabase.Perfiles", "Error obteniendo nombre: ${e.message}")
            null
        }
    }

}